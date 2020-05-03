package ai.beu.vocab;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;

public class Facebook {

    public static final String Prefix = "fb";
    public static final String Namespace = "https://facebook.com/";

    public static final Namespace NS = new SimpleNamespace(Prefix, Namespace);
}
