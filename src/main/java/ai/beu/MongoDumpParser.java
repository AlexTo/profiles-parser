package ai.beu;

import ai.beu.models.mongo.Profile;
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
import java.time.Duration;
import java.time.Instant;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static ai.beu.Utils.*;

public class MongoDumpParser {

    private static final boolean continueOnErrors = true;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValueFactory vf = SimpleValueFactory.getInstance();
    private static final int maxDocCount = Integer.MAX_VALUE;
    private static final String inputFile = "data/profiles1.tar.gz";
    private static final String outputFile = "output/profiles_" + System.currentTimeMillis() + ".ttl.gz";
    private static final String errorsFile = "errors/errors_" + System.currentTimeMillis() + ".txt";

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

        var startTime = Instant.now();
        int docCount = 0;
        String line;
        try {

            while ((line = bufferedReader.readLine()) != null && docCount < maxDocCount) {
                if (line.equals("\t\"_id\": {")) {
                    var doc = docStringBuilder.substring(0, docStringBuilder.lastIndexOf("{"));
                    if (!"".equals(doc.trim())) {

                        //writeRDF(doc, rdfWriter, errorOutputWriter); // replace your own function here

                        docCount++;
                        if (docCount % 1000 == 0) {
                            System.out.println("Processed " + docCount + " records.");
                        }
                    }
                    docStringBuilder = new StringBuilder();
                    docStringBuilder.append("{\n");
                }
                docStringBuilder.append(line).append("\n");
            }

            if (docCount < maxDocCount)
                process(docStringBuilder.toString(), fileOutputStream, errorOutputWriter);

            rdfWriter.endRDF();
        } finally {
            errorOutputWriter.close();
            bufferedReader.close();
            fileOutputStream.close();
            var endTime = Instant.now();
            var duration = Duration.between(startTime, endTime);
            System.out.println("Elapsed: " + duration.getSeconds() + "s.");
        }
    }

    public static void process(String doc, OutputStream outputStream, BufferedWriter errorOutputWriter) throws Exception {
        try {
            var profile = objectMapper.readValue(doc, Profile.class);

        } catch (Exception e) {
            errorOutputWriter.append(doc);
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
            if (!continueOnErrors)
                throw e;
        }
    }


}
