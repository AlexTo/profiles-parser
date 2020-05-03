package ai.beu.vocab;

import lombok.var;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class DBO {
    public static final String Namespace = "http://dbpedia.org/ontology/";
    public static final String Prefix = "dbo";

    public static final Namespace NS = new SimpleNamespace(Prefix, Namespace);

    public static final IRI Place;
    public static final IRI Album;
    public static final IRI Video;

    public static final IRI album;
    public static final IRI hometown;
    public static final IRI video;
    public static final IRI picture;


    static {
        var valueFactory = SimpleValueFactory.getInstance();
        Place = valueFactory.createIRI(Namespace, "Place");
        Album = valueFactory.createIRI(Namespace, "Album");
        Video = valueFactory.createIRI(Namespace, "Video");


        hometown = valueFactory.createIRI(Namespace, "hometown");
        album = valueFactory.createIRI(Namespace, "album");
        video = valueFactory.createIRI(Namespace, "video");
        picture = valueFactory.createIRI(Namespace, "picture");

    }
}
