import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestThesisLikesLoves {
    private final static String path = "src/test/resources/thesis/likesLoves/";

    @Test
    void testNoUpdate() {
        Graph shapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertTrue(report.conforms());
    }

    @Test
    void testUpdatedSimple() {
        Graph shapesGraph = RDFDataMgr.loadGraph(path + "shapesUpdatedSimple.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(path + "dataUpdated.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertTrue(report.conforms());
    }

    @Test
    void testUpdatedQualified() {
        Graph shapesGraph = RDFDataMgr.loadGraph(path + "shapesUpdatedQualified.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(path + "dataUpdated.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertTrue(report.conforms());
    }
}
