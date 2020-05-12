package ai.beu;

import ai.beu.models.mongo.Node;
import ai.beu.vocab.DBO;
import ai.beu.vocab.Facebook;
import lombok.var;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class Utils {
    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    public static IRI node(Model model, Node node, String type) {
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

    public static void pred(Model model, Resource resource, IRI pred, String value) {
        pred(model, resource, pred, value, "literal");
    }

    public static void pred(Model model, Resource resource, IRI pred, String value, String objType) {
        if (value == null || "".equals(value))
            return;
        if ("literal".equals(objType))
            model.add(resource, pred, literal(value));
        else if ("iri".equals(objType))
            model.add(resource, pred, iri(value));

    }

    public static void pred(Model model, Resource resource, IRI pred, Float value) {
        if (value == null)
            return;
        model.add(resource, pred, literal(value));
    }

    public static IRI iri(String value) {
        return vf.createIRI(value);
    }

    public static IRI iri(String namespace, String value) {
        return vf.createIRI(namespace, value);
    }

    public static Literal literal(String value) {
        return vf.createLiteral(value);
    }

    public static Literal literal(Float value) {
        return vf.createLiteral(value);
    }
}
