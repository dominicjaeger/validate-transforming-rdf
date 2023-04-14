import com.validatingevolvingrdf.Action;
import com.validatingevolvingrdf.ActionUtil;
import com.validatingevolvingrdf.Transformer;
import org.apache.jena.graph.Graph;
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

    /**
     * Update original data and compare result of action algorithm with expected data
     */
    @Test
    void testCompareUpdatedDataWithExpected() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph goalDataGraph = RDFDataMgr.loadGraph(path + "dataGoal.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedDataModel = ActionUtil.apply(actions,
                ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        assertTrue(updatedDataModel.getGraph().isIsomorphicWith(goalDataGraph));
    }

    /**
     * Transform original shapes graph and compare result of transformation algorithm
     * with expected shapes graph
     */
    @Test
    void testCompareTransformedShapesWithExpected() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Graph goalShapesGraph = RDFDataMgr.loadGraph(path + "shapesGoal.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(
                ModelFactory.createModelForGraph(originalShapesGraph), actions);

        assertTrue(updatedShapesGraph.isIsomorphicWith(goalShapesGraph));
    }


    @Test
    void test_validation_updatedData_originalShapes() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Model updateData = ActionUtil.apply(actions,
                ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        ValidationReport report = ShaclValidator.get().validate(
                originalShapesGraph, updateData.getGraph());
        assertFalse(report.conforms());
    }

    @Test
    void test_validation_originalData_updatedShapes() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        List<Action> actions = ActionUtil.parse(path + "actions");

        Graph updatedShapesGraph = Transformer.transform(
                ModelFactory.createModelForGraph(originalShapesGraph), actions);

        ValidationReport report = ShaclValidator.get().validate(
                updatedShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }
}
