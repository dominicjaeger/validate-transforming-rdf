import com.validatingevolvingrdf.Action;
import com.validatingevolvingrdf.ActionUtil;
import com.validatingevolvingrdf.Transformer;
import com.validatingevolvingrdf.Util;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMainRemoveProperty {

    private final static String path = "src/test/resources/main/removeProperty/";


    private final static Node subject = NodeFactory.createURI("http://example.com/ns#a");
    private final static Node property = NodeFactory.createURI("http://example.com/ns#p");
    private final static Node object = NodeFactory.createURI("http://example.com/ns#b");

    /**
     * The triple will be removed by the action, so it must be there before
     */
    @Test
    void testPreUpdate_hasTriple() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        assertTrue(originalDataGraph.contains(subject, property, object));
    }

    /**
     * The triple must not be there after the action
     */
    @Test
    void testPostActions_noTriple() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        assertFalse(updatedModel.getGraph().contains(subject, property, object));
    }

    @Test
    void testPostActions_invalid() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, updatedModel.getGraph());
        assertFalse(report.conforms());
    }

    @Test
    void testShapesGoal() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph updatedShapesGraph = RDFDataMgr.loadGraph(path + "shapesGoal.ttl");

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }

    @Test
    void testPostTransformation_invalid() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }



    /** Update original data and compare result of action algorithm with expected data */
    @Test
    void testShClosed_updateData() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph goalDataGraph = RDFDataMgr.loadGraph(path + "dataGoal.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedDataModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);
        //Util.debugPrint(goalShapesGraph, goalShapesGraph,null, null, updatedShapesGraph, updatedShapesGraph);

        assertTrue(updatedDataModel.getGraph().isIsomorphicWith(goalDataGraph));
    }

    /** Test if the transformation algorithm produces the expected shapes graph as result */
    @Test
    void testShClosed_updateShapes() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph goalShapesGraph = RDFDataMgr.loadGraph(path + "shapesGoal.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(ModelFactory.createModelForGraph(originalShapesGraph), actions);
        Util.debugPrint(goalShapesGraph, goalShapesGraph,null, null, updatedShapesGraph, updatedShapesGraph);

        assertTrue(updatedShapesGraph.isIsomorphicWith(goalShapesGraph));
    }



    @Test
    @DisplayName("Test validation with updated data graph on original shapes graph")
    void test_validation_updatedData_originalShapes() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updateData = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, updateData.getGraph());
        assertFalse(report.conforms());
    }

    @Test
    @DisplayName("Test validation with original data graph on transformed shapes graph")
    void test_validation_originalData_updatedShapes() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(ModelFactory.createModelForGraph(originalShapesGraph), actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }
}
