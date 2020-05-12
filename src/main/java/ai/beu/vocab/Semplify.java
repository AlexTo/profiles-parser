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

    public static final IRI Education;
    public static final IRI Employment;
    public static final IRI Position;
    public static final IRI Class;
    public static final IRI Page;
    public static final IRI PageCategory;

    public static final IRI id;
    public static final IRI employment;
    public static final IRI employer;
    public static final IRI education;
    public static final IRI position;
    public static final IRI schoolYear;
    public static final IRI school;

    public static final IRI administer;
    public static final IRI relationshipStatus;


    static {
        var valueFactory = SimpleValueFactory.getInstance();
        Employment = valueFactory.createIRI(Namespace, "Employment");
        Education = valueFactory.createIRI(Namespace, "Education");
        Position = valueFactory.createIRI(Namespace, "Position");
        Class = valueFactory.createIRI(Namespace, "Class");
        Page = valueFactory.createIRI(Namespace, "Page");
        PageCategory = valueFactory.createIRI(Namespace, "PageCategory");

        id = valueFactory.createIRI(Namespace, "id");
        employment = valueFactory.createIRI(Namespace, "employment");
        education = valueFactory.createIRI(Namespace, "education");
        employer = valueFactory.createIRI(Namespace, "employer");
        schoolYear = valueFactory.createIRI(Namespace, "schoolYear");
        school = valueFactory.createIRI(Namespace, "school");

        position = valueFactory.createIRI(Namespace, "position");
        administer = valueFactory.createIRI(Namespace, "administer");
        relationshipStatus = valueFactory.createIRI(Namespace, "relationshipStatus");
    }
}
