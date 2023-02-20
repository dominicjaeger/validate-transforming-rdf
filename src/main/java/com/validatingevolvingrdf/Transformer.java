package com.validatingevolvingrdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;

import java.util.Set;
import java.util.function.Predicate;

public class Transformer {

    public static Graph transform(Model originalShapesModel, Set<Action> actions) {
        Model originalShapesNoTargets = ModelFactory.createDefaultModel();
        /* For proper equality between two Property */
        Property targetNodeProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#targetNode");
        Predicate<Statement> targetPredicate = s -> s.getPredicate().equals(targetNodeProperty);
        originalShapesNoTargets.add(originalShapesModel.listStatements().filterDrop(targetPredicate).toList());

        Model updatedShapesModel = ModelFactory.createDefaultModel();
        updatedShapesModel.add(originalShapesModel);
        for (Action action : actions) {
            final Property classProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#class");
            final Property orProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#or");
            Resource actionConcept = originalShapesNoTargets.createResource(action.concept);
            Resource actionNewResource = originalShapesNoTargets.createResource(action.newResource);
            if (action.actionType.equals(Action.ActionType.PLUS)) {
                originalShapesNoTargets.listResourcesWithProperty(classProperty).forEach(subject -> {
                    NodeIterator objects = originalShapesNoTargets.listObjectsOfProperty(subject, classProperty);
                    objects.forEach(object -> {
                        if (object.asResource().equals(actionNewResource)) {
                            updatedShapesModel.remove(subject, classProperty, object);
                            Resource newSubjectForOriginalObject = updatedShapesModel.createResource();
                            updatedShapesModel.add(newSubjectForOriginalObject, classProperty, object);
                            RDFList orList = updatedShapesModel.createList(newSubjectForOriginalObject);
                            Resource subjectForConcept = updatedShapesModel.createResource();
                            actionConcept.listProperties().forEach(statement -> updatedShapesModel.add(subjectForConcept, statement.getPredicate(), statement.getObject()));
                            orList.add(subjectForConcept);
                            updatedShapesModel.add(subject, orProperty, orList);
                        }
                    });
                });
            }
        }
        return updatedShapesModel.getGraph();

    }
}
