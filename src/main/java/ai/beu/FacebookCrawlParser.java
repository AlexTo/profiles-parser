package ai.beu;

import ai.beu.models.fbapi.FbPageCategories;
import ai.beu.models.fbapi.FbPageCategory;
import ai.beu.models.fbapi.Profile;
import ai.beu.vocab.DBO;
import ai.beu.vocab.Facebook;
import ai.beu.vocab.Semplify;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.WGS84;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import static ai.beu.Utils.iri;
import static ai.beu.Utils.pred;

public class FacebookCrawlParser {
    private static final boolean continueOnErrors = true;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValueFactory vf = SimpleValueFactory.getInstance();
    private static final int maxDocCount = Integer.MAX_VALUE;
    private static final String outputFile = "output/profiles_" + System.currentTimeMillis() + ".ttl.gz";
    private static final String errorsFile = "errors/errors_" + System.currentTimeMillis() + ".txt";
    private static final String fbPageCategoriesFile = "data/fb_page_categories.json";
    private static final ConcurrentHashMap<String, FbPageCategory> fbCategoriesFlatten = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        var inputFile = args[0];
        var inputFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        var fileOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile), 65536);
        var errorOutputWriter = new BufferedWriter(new FileWriter(errorsFile));

        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        var rdfWriter = Rio.createWriter(RDFFormat.TURTLE, fileOutputStream);
        rdfWriter.startRDF();
        rdfWriter.handleNamespace(Facebook.Prefix, Facebook.Namespace);
        rdfWriter.handleNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
        rdfWriter.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        rdfWriter.handleNamespace(DBO.Prefix, DBO.Namespace);
        rdfWriter.handleNamespace(WGS84.PREFIX, WGS84.NAMESPACE);
        rdfWriter.handleNamespace(Semplify.Prefix, Semplify.Namespace);

        var fbCategories = loadFbPageCategories();
        flattenFbCategories(fbCategoriesFlatten, fbCategories.getData());

        for (var fbCat : fbCategoriesFlatten.values()) {
            var model = new LinkedHashModel();
            var fbCatIri = iri(Facebook.Namespace + "pages/category/", fbCat.getApiEnum());
            model.add(fbCatIri, RDF.TYPE, Semplify.PageCategory);
            pred(model, fbCatIri, Semplify.id, fbCat.getId());
            pred(model, fbCatIri, FOAF.NAME, fbCat.getName());
            for (var st : model) {
                rdfWriter.handleStatement(st);
            }
        }

        String line;
        int docCount = 0;

        var startTime = Instant.now();
        try {
            while ((line = inputFileReader.readLine()) != null && docCount < maxDocCount) {
                var colon = line.indexOf(": ");
                var profileString = line.substring(colon + 2, line.length() - 1);
                writeRDF(profileString, rdfWriter, errorOutputWriter);
                docCount++;
                if (docCount % 1000 == 0) {
                    System.out.println(String.format("Processed: %d records.", docCount));
                }
            }
            if (docCount % 1000 != 0) {
                System.out.println(String.format("Processed: %d records.", docCount));
            }
        } finally {
            rdfWriter.endRDF();
            errorOutputWriter.close();
            inputFileReader.close();
            fileOutputStream.close();
            var endTime = Instant.now();
            var duration = Duration.between(startTime, endTime);
            System.out.println("Elapsed: " + duration.getSeconds() + "s.");
        }
    }

    public static void writeRDF(String doc, RDFWriter rdfWriter, BufferedWriter errorOutputWriter) throws Exception {
        try {
            var profile = objectMapper.readValue(doc, Profile.class);
            var model = new LinkedHashModel();

            var profileIri = iri(Facebook.Namespace, profile.getId());

            model.add(profileIri, RDF.TYPE, FOAF.PERSON);
            model.add(profileIri, FOAF.DEPICTION,
                    vf.createIRI(String.format("https://graph.facebook.com/%s/picture", profile.getId())));
            pred(model, profileIri, Semplify.id, profile.getId());
            pred(model, profileIri, FOAF.NAME, profile.getName());
            pred(model, profileIri, FOAF.GENDER, profile.getGender());

            pred(model, profileIri, Semplify.relationshipStatus, profile.getRelationshipStatus());

            var works = profile.getWork();
            if (works != null) {
                for (var work : works) {
                    var workIri = iri(Facebook.Namespace, work.getId());
                    model.add(workIri, RDF.TYPE, Semplify.Employment);
                    model.add(profileIri, Semplify.employment, workIri);
                    pred(model, workIri, Semplify.id, work.getId());
                    pred(model, workIri, RDFS.COMMENT, work.getDescription());

                    var employer = work.getEmployer();
                    if (employer != null) {
                        var employerIri = iri(Facebook.Namespace, employer.getId());
                        model.add(employerIri, RDF.TYPE, FOAF.ORGANIZATION);
                        model.add(workIri, Semplify.employer, employerIri);
                        pred(model, employerIri, Semplify.id, employer.getId());
                        pred(model, employerIri, FOAF.NAME, employer.getName());
                    }
                    var position = work.getPosition();
                    if (position != null) {
                        var positionIri = iri(Facebook.Namespace, position.getId());
                        model.add(workIri, Semplify.position, positionIri);
                        pred(model, positionIri, Semplify.id, position.getId());
                        pred(model, positionIri, FOAF.NAME, position.getName());
                    }
                }
            }

            var educations = profile.getEducation();
            if (educations != null) {
                for (var education : educations) {
                    var educationIri = iri(Facebook.Namespace, education.getId());
                    model.add(educationIri, RDF.TYPE, Semplify.Education);
                    model.add(profileIri, Semplify.education, educationIri);
                    pred(model, educationIri, Semplify.id, education.getId());
                    pred(model, educationIri, RDFS.COMMENT, education.getType());
                    var school = education.getSchool();
                    if (school != null) {
                        var schoolIri = iri(Facebook.Namespace, school.getId());
                        model.add(schoolIri, RDF.TYPE, DBO.School);
                        model.add(schoolIri, FOAF.DEPICTION,
                                vf.createIRI(String.format("https://graph.facebook.com/%s/picture", school.getId())));
                        model.add(educationIri, Semplify.school, schoolIri);
                        pred(model, schoolIri, Semplify.id, school.getId());
                        pred(model, schoolIri, FOAF.NAME, school.getName());
                    }
                    var classes = education.getClasses();
                    if (classes != null) {
                        for (var clazz : classes) {
                            var classIri = iri(Facebook.Namespace, clazz.getId());
                            model.add(classIri, RDF.TYPE, Semplify.Class);
                            model.add(educationIri, Semplify.schoolYear, classIri);
                            pred(model, classIri, Semplify.id, clazz.getId());
                            pred(model, classIri, FOAF.NAME, clazz.getName());
                        }
                    }
                }
            }

            var likes = profile.getLikes();
            if (likes != null && likes.getData() != null) {
                for (var fbPage : likes.getData()) {
                    var fbPageIri = iri(Facebook.Namespace, fbPage.getId());
                    model.add(fbPageIri, RDF.TYPE, Semplify.Page);
                    model.add(fbPageIri, FOAF.DEPICTION,
                            vf.createIRI(String.format("https://graph.facebook.com/%s/picture", fbPage.getId())));
                    model.add(profileIri, FOAF.INTEREST, fbPageIri);
                    pred(model, fbPageIri, Semplify.id, fbPage.getId());
                    pred(model, fbPageIri, FOAF.NAME, fbPage.getName());
                    var categories = fbPage.getCategoryList();
                    if (categories != null) {
                        for (var category : categories) {
                            if (!fbCategoriesFlatten.containsKey(category.getId())) {
                                continue;
                            }
                            var fbCat = fbCategoriesFlatten.get(category.getId());
                            var fbCatIri = iri(Facebook.Namespace + "pages/category/", fbCat.getApiEnum());
                            model.add(fbPageIri, FOAF.TOPIC, fbCatIri);
                        }
                    }
                }
            }

            var friends = profile.getFriends();
            if (friends != null && friends.getData() != null) {
                for (var friend : friends.getData()) {
                    var friendIri = iri(Facebook.Namespace, friend.getId());
                    model.add(friendIri, RDF.TYPE, FOAF.PERSON);
                    model.add(profileIri, FOAF.KNOWS, friendIri);
                    pred(model, friendIri, Semplify.id, friend.getId());
                    pred(model, friendIri, FOAF.NAME, friend.getName());
                    model.add(friendIri, FOAF.DEPICTION,
                            vf.createIRI(String.format("https://graph.facebook.com/%s/picture", friend.getId())));
                }
            }

            var albums = profile.getAlbums();
            if (albums != null && albums.getData() != null) {
                for (var album : albums.getData()) {
                    var albumIri = iri(Facebook.Namespace, album.getId());
                    model.add(albumIri, RDF.TYPE, DBO.Album);
                    model.add(profileIri, DBO.album, albumIri);
                    pred(model, albumIri, Semplify.id, album.getId());
                    pred(model, albumIri, FOAF.NAME, album.getName());
                    pred(model, albumIri, RDFS.COMMENT, album.getType());
                    pred(model, albumIri, FOAF.HOMEPAGE, album.getLink(), "iri");
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

    private static FbPageCategories loadFbPageCategories() throws IOException {
        var fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fbPageCategoriesFile)));
        String line;
        var doc = new StringBuilder();
        try {
            while ((line = fileReader.readLine()) != null) {
                doc.append(line);
            }
            return objectMapper.readValue(doc.toString(), FbPageCategories.class);
        } finally {
            fileReader.close();
        }
    }

    private static void flattenFbCategories(ConcurrentHashMap map, List<FbPageCategory> fbPageCategories) {
        if (fbPageCategories == null)
            return;

        for (var fbPageCategory : fbPageCategories) {
            map.putIfAbsent(fbPageCategory.getId(), fbPageCategory);
            flattenFbCategories(map, fbPageCategory.getFbPageCategories());
        }
    }
}
