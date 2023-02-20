import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestThesisProperties {
    private final static String path = "src/test/resources/thesis/properties/";


    @Test
    void testIsomorphism() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph updateDataGraph = RDFDataMgr.loadGraph(path + "dataUpdates.ttl");

        Model originalModel = ModelFactory.createModelForGraph(originalDataGraph);
        Model updateModel = ModelFactory.createModelForGraph(updateDataGraph);

        Graph unionGraph = new Union(originalDataGraph, updateDataGraph);
        Model unionModel = originalModel.union(updateModel);

        assertTrue(unionModel.isIsomorphicWith(ModelFactory.createModelForGraph(unionGraph)));
        assertTrue(unionGraph.isIsomorphicWith(unionModel.getGraph()));
    }

    @Test
    void testOriginal() {
        Graph shapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertFalse(report.conforms());
    }

    @Test
    void testUpdatedData() {
        Graph shapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(path + "dataUpdated.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertTrue(report.conforms());
    }

    /**
     * In this case b does not have to be connected to c because of the updated shapes
     */
    @Test
    void testUpdatedShapes() {
        Graph shapesGraph = RDFDataMgr.loadGraph(path + "shapesUpdated.ttl");
        Graph dataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        ValidationReport report = ShaclValidator.get().validate(shapesGraph, dataGraph);

        assertTrue(report.conforms());
    }


}
