import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestShaclRecommendationGeneralExample {
    @Test
    void testNegative() {
        String SHAPES = "src/test/resources/shaclRecommendation/generalExample/negativeExample/shapesGraph.ttl";
        String DATA = "src/test/resources/shaclRecommendation/generalExample/negativeExample/dataGraph.ttl";
        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        Graph dataGraph = RDFDataMgr.loadGraph(DATA);

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertFalse(report.conforms());
    }

    @Test
    void testPositive() {
        String SHAPES = "src/test/resources/shaclRecommendation/generalExample/positiveExample/shapesGraph.ttl";
        String DATA = "src/test/resources/shaclRecommendation/generalExample/positiveExample/dataGraph.ttl";
        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        Graph dataGraph = RDFDataMgr.loadGraph(DATA);

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertTrue(report.conforms());
    }


}
