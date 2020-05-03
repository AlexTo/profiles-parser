package ai.beu.vocab;

import lombok.var;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class Semplify {

    public static final String Namespace = "http://semplify.ai/ontology/";
    public static final String Prefix = "sp";

    public static final Namespace NS = new SimpleNamespace(Prefix, Namespace);

    public static final IRI id;
    public static final IRI administer;

    static {
        var valueFactory = SimpleValueFactory.getInstance();
        id = valueFactory.createIRI(Namespace, "id");
        administer = valueFactory.createIRI(Namespace, "administer");

    }
}
