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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExistsOnePropertyPlus {

    private final static String path = "src/test/resources/simple/existsOnePropertyPlus/";

    private final static Node subject = NodeFactory.createURI("http://example.com/ns#a");
    private final static Node property = NodeFactory.createURI("http://example.com/ns#p");
    private final static Node object = NodeFactory.createURI("http://example.com/ns#b");

    /**
     * The triple will be added by the action, so it must not be there before
     */
    @Test
    void testPreUpdate_noTriple() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        assertFalse(originalDataGraph.contains(subject, property, object));
    }

    /**
     * Before the action, the shape ex:Shape does not hold for a because there is no property with p going out from a
     */
    @Test
    void testPreUpdate_noValidation() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }

    /**
     * The triple must be there after the action
     */
    @Test
    void testPostActions_hasTriple() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Set<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        assertTrue(updatedModel.getGraph().contains(subject, property, object));
    }

    @Test
    void testPostActions_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Set<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, updatedModel.getGraph());
        assertTrue(report.conforms());
    }

    @Test
    void testPostTransformation_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        Set<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);

        assertTrue(report.conforms());
    }
}
