package ai.beu;

import ai.beu.models.Node;
import ai.beu.models.Profile;
import ai.beu.vocab.DBO;
import ai.beu.vocab.Facebook;
import ai.beu.vocab.Semplify;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Main {

    private static int maxDocCount = Integer.MAX_VALUE;
    private static boolean continueOnErrors = true;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ValueFactory vf = SimpleValueFactory.getInstance();

    private static String inputFile = "data/profiles.tar.gz";
    private static String outputFile = "output/profiles_" + System.currentTimeMillis() + ".ttl.gz";
    private static String errorsFile = "errors/errors_" + System.currentTimeMillis() + ".txt";

    public static void main(String[] args) throws Exception {

        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        var fileInputStream = new TarArchiveInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(inputFile), 65536)));
        fileInputStream.getNextTarEntry();
        var inputStreamReader = new InputStreamReader(fileInputStream);
        var bufferedReader = new BufferedReader(inputStreamReader);

        var fileOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile), 65536);
        var errorOutputWriter = new BufferedWriter(new FileWriter(errorsFile));
        var docStringBuilder = new StringBuilder();

        var rdfWriter = Rio.createWriter(RDFFormat.TURTLE, fileOutputStream);
        rdfWriter.startRDF();
        rdfWriter.handleNamespace(Facebook.Prefix, Facebook.Namespace);
        rdfWriter.handleNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
        rdfWriter.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        rdfWriter.handleNamespace(DBO.Prefix, DBO.Namespace);
        rdfWriter.handleNamespace(WGS84.PREFIX, WGS84.NAMESPACE);
        rdfWriter.handleNamespace(Semplify.Prefix, Semplify.Namespace);

        int docCount = 0;
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null && docCount < maxDocCount) {
                if (line.equals("\t\"_id\": {")) {
                    var doc = docStringBuilder.substring(0, docStringBuilder.lastIndexOf("{"));
                    if (!"".equals(doc.trim())) {

                        writeRDF(doc, rdfWriter, errorOutputWriter); // replace your own function here

                        docCount++;
                        if (docCount % 1000 == 0) {
                            System.out.println("Processed " + docCount + " records.");
                        }
                    }
                    docStringBuilder = new StringBuilder();
                    docStringBuilder.append("{\n");
                }
                docStringBuilder.append(line + "\n");
            }

            if (docCount < maxDocCount)
                process(docStringBuilder.toString(), fileOutputStream, errorOutputWriter);

            rdfWriter.endRDF();
        } finally {
            errorOutputWriter.close();
            bufferedReader.close();
            fileOutputStream.close();
        }
    }

    public static void process(String doc, OutputStream outputStream, BufferedWriter errorOutputWriter) throws Exception {
        try {
            var profile = objectMapper.readValue(doc, Profile.class);
            // do stuff with profile here
        } catch (Exception e) {
            errorOutputWriter.append(doc);
            errorOutputWriter.append("\n\n");
            errorOutputWriter.append(e.getMessage());
            errorOutputWriter.append("\n\n");
            if (!continueOnErrors)
                throw e;
        }
    }

    public static void writeRDF(String doc, RDFWriter rdfWriter, BufferedWriter errorOutputWriter) throws Exception {
        try {
            var profile = objectMapper.readValue(doc, Profile.class);
            var model = new LinkedHashModel();

            var profileIri = iri(Facebook.Namespace, profile.getUid());

            model.add(profileIri, RDF.TYPE, FOAF.PERSON);
            model.add(profileIri, FOAF.DEPICTION,
                    vf.createIRI(String.format("https://graph.facebook.com/%s/picture", profile.getUid())));
            pred(model, profileIri, FOAF.ACCOUNT_NAME, profile.getUid());
            pred(model, profileIri, FOAF.NAME, profile.getName());
            pred(model, profileIri, FOAF.GENDER, profile.getGender().toLowerCase());

            var fbData = profile.getFbData();

            if (fbData != null) {
                pred(model, profileIri, FOAF.FIRST_NAME, fbData.getFirstName());
                pred(model, profileIri, FOAF.LAST_NAME, fbData.getLastName());
                pred(model, profileIri, FOAF.GENDER, fbData.getGender().toLowerCase());

                var hometown = fbData.getHometown();
                if (hometown != null) {

                    var homeTownIri = iri(Facebook.Namespace, hometown.getId());
                    model.add(profileIri, DBO.hometown, homeTownIri);
                    model.add(homeTownIri, RDF.TYPE, DBO.Place);

                    pred(model, homeTownIri, FOAF.NAME, hometown.getName());
                    pred(model, homeTownIri, FOAF.ACCOUNT_NAME, hometown.getId());
                    pred(model, homeTownIri, FOAF.HOMEPAGE, hometown.getUrl(), "iri");

                    if (hometown.getLocation() != null) {
                        pred(model, homeTownIri, WGS84.LAT, hometown.getLocation().getLatitude());
                        pred(model, homeTownIri, WGS84.LONG, hometown.getLocation().getLongitude());
                    }
                }

                var groups = fbData.getGroups();
                if (groups != null) {
                    var groupNodes = groups.getNodes();
                    if (groupNodes != null) {
                        for (var groupNode : groupNodes) {
                            var group = node(model, groupNode, "group");
                            model.add(profileIri, FOAF.MEMBER, group);
                        }
                    }
                }
                var adminGroups = fbData.getAdminGroups();
                if (adminGroups != null) {
                    var adminGroupNodes = adminGroups.getNodes();
                    if (adminGroupNodes != null) {
                        for (var adminGroupNode : adminGroupNodes) {
                            var adminGroup = node(model, adminGroupNode, "group");
                            model.add(profileIri, Semplify.administer, adminGroup);
                        }
                    }
                }

                var albums = fbData.getAlbums();
                if (albums != null) {
                    var albumNodes = albums.getNodes();
                    if (albumNodes != null) {
                        for (var albumNode : albumNodes) {
                            var album = node(model, albumNode, "album");
                            model.add(profileIri, DBO.album, album);
                        }
                    }
                }

                var friends = fbData.getFriends();
                if (friends != null) {
                    var friendNodes = friends.getNodes();
                    if (friendNodes != null) {
                        for (var friendNode : friendNodes) {
                            var friend = node(model, friendNode, "friend");
                            model.add(profileIri, FOAF.KNOWS, friend);
                        }
                    }
                }

                var videos = fbData.getLiveVideos();
                if (videos != null) {
                    var videoNodes = videos.getNodes();
                    if (videoNodes != null) {
                        for (var videoNode : videoNodes) {
                            var video = node(model, videoNode, "video");
                            model.add(profileIri, DBO.video, video);
                        }
                    }
                }
            }

            for (var st : model) {
                rdfWriter.handleStatement(st);
            }

        } catch (Exception e) {
            errorOutputWriter.append(doc);
            errorOutputWriter.append("\n\n");
            errorOutputWriter.append(e.getMessage());
            errorOutputWriter.append("\n\n");
            if (!continueOnErrors)
                throw e;
        }
    }

    private static IRI node(Model model, Node node, String type) {
        var nodeIri = iri(Facebook.Namespace, node.getId());
        if ("group".equals(type)) {
            model.add(nodeIri, RDF.TYPE, FOAF.GROUP);
        } else if ("album".equals(type)) {
            model.add(nodeIri, RDF.TYPE, DBO.Album);
        } else if ("friend".equals(type)) {
            model.add(nodeIri, FOAF.DEPICTION,
                    vf.createIRI(String.format("https://graph.facebook.com/%s/picture", node.getId())));
            model.add(nodeIri, RDF.TYPE, FOAF.PERSON);
        } else if ("video".equals(type)) {
            model.add(nodeIri, RDF.TYPE, DBO.Video);
        }
        pred(model, nodeIri, FOAF.ACCOUNT_NAME, node.getId());
        pred(model, nodeIri, FOAF.HOMEPAGE, node.getUrl(), "iri");
        pred(model, nodeIri, FOAF.NAME, node.getName());
        if (node.getTitle() != null) {
            pred(model, nodeIri, FOAF.TITLE, node.getTitle().getText());
        }
        if (node.getMessage() != null) {
            pred(model, nodeIri, FOAF.TITLE, node.getMessage().getText());
        }
        if (node.getImage() != null) {
            pred(model, nodeIri, FOAF.DEPICTION, node.getImage().getUrl());
        }

        return nodeIri;
    }

    private static void pred(Model model, Resource resource, IRI pred, String value) {
        pred(model, resource, pred, value, "literal");
    }

    private static void pred(Model model, Resource resource, IRI pred, String value, String objType) {
        if (value == null || "".equals(value))
            return;
        if ("literal".equals(objType))
            model.add(resource, pred, literal(value));
        else if ("iri".equals(objType))
            model.add(resource, pred, iri(value));

    }

    private static void pred(Model model, Resource resource, IRI pred, Float value) {
        if (value == null)
            return;
        model.add(resource, pred, literal(value));
    }

    private static IRI iri(String value) {
        return vf.createIRI(value);
    }

    private static IRI iri(String namespace, String value) {
        return vf.createIRI(namespace, value);
    }

    private static Literal literal(String value) {
        return vf.createLiteral(value);
    }

    private static Literal literal(Float value) {
        return vf.createLiteral(value);
    }
}
