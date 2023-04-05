import com.validatingevolvingrdf.Util;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestValidatorUsage {
    private final static String pathRecommendation = "src/test/resources/validatorUsage/shaclRecommendation/";

    /** One example from the SHACL recommendation */
    @Test
    void testNegative() {
        Graph shapesGraph = RDFDataMgr.loadGraph(pathRecommendation + "negativeExample/shapesGraph.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(pathRecommendation + "negativeExample/dataGraph.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);
        assertFalse(report.conforms());
    }

    /** One example from the SHACL recommendation */
    @Test
    void testPositive() {
        Graph shapesGraph = RDFDataMgr.loadGraph(pathRecommendation + "positiveExample/shapesGraph.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(pathRecommendation + "positiveExample/dataGraph.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);
        assertTrue(report.conforms());
    }




    /** Test negation */
    @Test
    void testValidation() {
        String pathJohn = "src/test/resources/validatorUsage/john/";
        Graph originalDataGraph = RDFDataMgr.loadGraph(pathJohn + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(pathJohn + "shapes.ttl");

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, originalDataGraph);
        assertTrue(report.conforms());
    }

    /** Demonstration of SPARQL query as literal */
    @Test
    void testSparql() {
        String pathSparql = "src/test/resources/validatorUsage/sparql/";
        Graph sparqlShapesGraph = RDFDataMgr.loadGraph(pathSparql + "sparql.ttl");

        Util.debugPrint(sparqlShapesGraph, sparqlShapesGraph,null, null, null, null);
        assertTrue(true);
    }
}
