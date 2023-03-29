import com.validatingevolvingrdf.Action;
import com.validatingevolvingrdf.ActionUtil;
import com.validatingevolvingrdf.Transformer;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSimpleBasicTransformation {

    @Test
    void testBasicTransformation() throws FileNotFoundException {
        String path = "src/test/resources/simple/basicTransformation/";
        Graph goalShapesGraph = RDFDataMgr.loadGraph(path + "shapesGoal.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        assertTrue(updatedShapesGraph.isIsomorphicWith(goalShapesGraph));
    }
}
