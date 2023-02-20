import com.validatingevolvingrdf.Action;
import com.validatingevolvingrdf.ActionUtil;
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

public class TestSimpleOneClassMinus {
    private final static String path = "src/test/resources/simple/oneClassMinus/";

    /** Is the sh:not working as expected?
     * Original shapes graph with original data graph should be not OK yet
     * */
    @Test
    void testPreUpdated() {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report = validator.validate(originalShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }

    /** After the update, the validation should be OK */
    @Test
    void testPostUpdate_reportConforms() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        Set<Action> actions = ActionUtil.parse(path + "actions");
        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report = validator.validate(originalShapesGraph, updatedModel.getGraph());
        assertTrue(report.conforms());
    }

    /** After the update, the triple must be gone from the data graph */
    @Test
    void testPostUpdate_tripleGone() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        Set<Action> actions = ActionUtil.parse(path + "actions");
        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        Node subject = NodeFactory.createURI("http://example.com/ns#b");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#A");
        assertFalse(updatedModel.getGraph().contains(subject, property, object));
    }
}