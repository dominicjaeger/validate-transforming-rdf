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


public class TestSimpleOneClassPlus {
    private final static String path = "src/test/resources/simple/oneClassPlus/";

    @Test
    void testPreUpdate_noTriple() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");

        Node subject = NodeFactory.createURI("http://example.com/ns#b");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#A");
        assertFalse(originalDataGraph.contains(subject, property, object));
    }

    @Test
    void testPreUpdate_noValidation() {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");

        ValidationReport report = ShaclValidator.get().validate(originalShapesGraph, originalDataGraph);
        assertFalse(report.conforms());
    }

    @Test
    void testPostActions_hasTriple() throws FileNotFoundException {
        Graph originalDataGraph = RDFDataMgr.loadGraph(path + "data.ttl");
        Graph originalShapesGraph = RDFDataMgr.loadGraph(path + "shapes.ttl");
        Set<Action> actions = ActionUtil.parse(path + "actions");

        Model updatedModel = ActionUtil.apply(actions, ModelFactory.createModelForGraph(originalDataGraph), originalShapesGraph);

        Node subject = NodeFactory.createURI("http://example.com/ns#b");
        Node property = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node object = NodeFactory.createURI("http://example.com/ns#A");
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


    /*
      Just here in case we need the code snippets again
     */
   /* @Disabled
    @Test
    void exampleWithProperties_actionsUsingShaclOld() {
        // WRONG PATH
        String originalData = "src/test/resources/thesis/exampleWithOneBasicClass/data.ttl";
        String actions = "src/test/resources/thesis/exampleWithOneBasicClass/actionClass.ttl";

        Graph originalDataGraph = RDFDataMgr.loadGraph(originalData);
        Graph actionGraph = RDFDataMgr.loadGraph(actions);

        Model originalModel = ModelFactory.createModelForGraph(originalDataGraph);
        Model actionModel = ModelFactory.createModelForGraph(actionGraph);
        Model updatesModel = ModelFactory.createDefaultModel();

        System.out.println("All statements in the model:");
        actionModel.listStatements().forEach(System.out::println);
        System.out.println();

        Predicate<Statement> filterAnon = s -> s.getSubject().isAnon();
        Predicate<Statement> actionPredicate = s -> s.getSubject().getNameSpace().equals("http://www.w3.org/ns/shacl/action#");
        ExtendedIterator<Statement> baseActionIterator = actionModel.listStatements().filterDrop(filterAnon).filterKeep(actionPredicate);
        Property classProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#class");

        System.out.println("Base Actions are");
        Set<Resource> actionSet = baseActionIterator.mapWith(statement -> statement.getSubject()).toSet();
        actionSet.forEach(r -> {
            Resource left = null;
            Set<Resource> right = new HashSet<>();

            Property orderProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#order");
            Property propertyProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#property");
            ExtendedIterator<Resource> propertyNodes = r.listProperties(propertyProperty).mapWith(s -> s.getObject().asResource());
            for (Resource propertyNode : propertyNodes.toSet()) {
                Literal order = propertyNode.getProperty(orderProperty).getLiteral();
                ExtendedIterator<Resource> classes = propertyNode.listProperties(classProperty).mapWith(s -> s.getObject().asResource());
                if (order.toString().equals("0^^http://www.w3.org/2001/XMLSchema#integer")) {
                    left = classes.next(); // always 1
                } else if (order.toString().equals("1^^http://www.w3.org/2001/XMLSchema#integer")) {
                    classes.forEach(myClass -> right.add(myClass));
                }
            }
            System.out.println("Left side is " + left);
            System.out.println("Right side is " + right);
            System.out.println("Original data");
            originalModel.listStatements(null, classProperty, left).forEach(System.out::println);


            originalModel.listStatements(null, classProperty, left).forEach(statement -> {
                List<Statement> updates = new LinkedList<>();
                right.forEach(resource -> updatesModel.createStatement(statement.getSubject(), classProperty, resource));
                updatesModel.add(updates);
                // TODO Use alternative path instead?!
            });

            System.out.println("Updates model is ");
            updatesModel.listStatements().forEach(System.out::println);
            System.out.println();


            updatesModel.add(originalModel);
            Model updatedModel = ModelFactory.createDefaultModel();
            updatedModel.add(originalModel);
            updatedModel.add(updatesModel);
            System.out.println("Updated model is ");
            updatedModel.listStatements().forEach(System.out::println);

        });
    }*/

}

