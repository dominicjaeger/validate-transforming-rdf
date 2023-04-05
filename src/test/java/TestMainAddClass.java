import com.validatingevolvingrdf.Action;
import com.validatingevolvingrdf.ActionUtil;
import com.validatingevolvingrdf.Transformer;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestMainAddClass {
    private final static String underUpdatesPath = "src/test/resources/main/addClass/underUpdates/";

    @Test
    void testBasicTransformation() throws FileNotFoundException {
        String correctTransformationPath = "src/test/resources/main/addClass/correctTransformation/";
        Graph goalShapesGraph = RDFDataMgr.loadGraph(correctTransformationPath + "shapesGoal.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(correctTransformationPath + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(correctTransformationPath + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        assertTrue(updatedShapesGraph.isIsomorphicWith(goalShapesGraph));
    }


    @Test
    void testPreUpdate_noTriple() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(underUpdatesPath + "data.ttl");

        Node subject = NodeFactory.createURI("http://example.com/ns#b");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#A");
        assertFalse(originalDataGraph.contains(subject, property, object));
    }

    @Test
    void testPreUpdate_noValidation() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(underUpdatesPath + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(underUpdatesPath + "shapes.ttl");

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }

    @Test
    void testPostActions_hasTriple() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(underUpdatesPath + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(underUpdatesPath + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(underUpdatesPath + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        Node subject = NodeFactory.createURI("http://example.com/ns#b");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#A");
        assertTrue(updatedModel.getGraph().contains(subject, property, object));
    }

    @Test
    void testPostActions_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(underUpdatesPath + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(underUpdatesPath + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(underUpdatesPath + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, updatedModel.getGraph());
        assertTrue(report.conforms());
    }

    @Test
    void testPostTransformation_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(underUpdatesPath + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(underUpdatesPath + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(underUpdatesPath + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);
        assertTrue(report.conforms());
    }
}

