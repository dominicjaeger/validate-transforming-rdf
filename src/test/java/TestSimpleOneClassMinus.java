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
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSimpleOneClassMinus {
    private final static String path = "src/test/resources/simple/oneClassMinus/withAction/";
    private final static String pathBasicTransformation = "src/test/resources/simple/oneClassMinus/correctShapeTransformation/";


    private final static String pathCase2 = "src/test/resources/simple/oneClassMinus/withActionCase2/";

    @Test
    void testBasicTransformation() throws FileNotFoundException {
        Graph goalShapesGraph = RDFDataMgr.loadGraph(pathBasicTransformation + "shapesGoal.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(pathBasicTransformation + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(pathBasicTransformation + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);
        Util.debugPrint(originalShapesGraph, originalShapesGraph,null, null, updatedShapesGraph, updatedShapesGraph);
        assertTrue(updatedShapesGraph.isIsomorphicWith(goalShapesGraph));
    }

    /** Updated graph with original shapes does not validate (as expected) */
    @Test
    void testPostUpdate_reportConforms() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        List<Action> actions = ActionUtil.parse(path + "actions");
        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report = validator.validate(originalShapesGraph, updatedModel.getGraph());
        assertFalse(report.conforms());
    }

    /** After the update, the triple must be gone from the data graph */
    @Test
    void testPostUpdate_tripleGone() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        List<Action> actions = ActionUtil.parse(path + "actions");
        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        Node subject = NodeFactory.createURI("http://example.com/ns#b");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#A");
        assertFalse(updatedModel.getGraph().contains(subject, property, object));
    }

    /** Original data with updated shapes does not validate (as expected) */

    @Test
    void testPostTransformation_noValidation() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);

        assertFalse(report.conforms());
    }


    /** For case 2, only the shapes graph changed. Actions and data remain unchanged */
    @Test
    void testPostUpdate_case2_preUpdateNotConform() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(pathCase2 + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(pathCase2 + "data.ttl");

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report = validator.validate(originalShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }
    @Test
    void testPostUpdate_case2_reportConforms() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(pathCase2 + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(pathCase2 + "data.ttl");

        List<Action> actions = ActionUtil.parse(pathCase2 + "actions");
        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report = validator.validate(originalShapesGraph, updatedModel.getGraph());
        assertTrue(report.conforms());
    }

    @Test
    void testPostTransformation_case2_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(pathCase2 + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(pathCase2 + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(pathCase2 + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);
        Util.debugPrint(originalDataGraph, originalShapesGraph, actions, report, null, updatedShapesGraph);

        assertTrue(report.conforms());
    }
}