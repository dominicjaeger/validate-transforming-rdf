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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests both:
 * 1)two actions and
 * 2) an action with a concept on the right side that is not just a class
 */
public class TestSimpleTwoClassesPlus {
    private final static String path = "src/test/resources/simple/twoClassesPlus/";


    @Test
    void testPostActions_hasTriple() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        Node subject = NodeFactory.createURI("http://example.com/ns#d");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#C");

        assertTrue(updatedModel.getGraph().contains(subject, property, object));
    }

    @Test
    void testPostActions_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, updatedModel.getGraph());
        assertTrue(report.conforms());
    }

    @Test
    void testPostTransformation_validates() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ValidationReport report = ShaclValidator.get().validate(updatedShapesGraph, originalDataGraph);

        assertTrue(report.conforms());
    }
}
