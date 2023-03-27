import com.validatingevolvingrdf.Action;
import com.validatingevolvingrdf.ActionUtil;
import com.validatingevolvingrdf.Transformer;
import com.validatingevolvingrdf.Util;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSparql {
    private final static String path = "src/test/resources/simple/sparql/";
    @Test
    void testSparql() throws FileNotFoundException {
        Graph sparqlShapesGraph = RDFDataMgr.loadGraph(path + "sparql.ttl");

        Util.debugPrint(sparqlShapesGraph, sparqlShapesGraph,null, null, null, null);
        assertTrue(true);
    }
}
