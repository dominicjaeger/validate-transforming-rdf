package com.validatingevolvingrdf;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Transformer {
    private final static String propertyUri = "http://www.w3.org/ns/shacl#property";


    private final static String pathUri = "http://www.w3.org/ns/shacl#path";
    private final static String alternativePathUri = "http://www.w3.org/ns/shacl#alternativePath";


    public static Graph transform(Model originalShapesModel, List<Action> actions) {
        Model originalShapesNoTargets = ModelFactory.createDefaultModel();
        /* For proper equality between two Property */
        Property targetNodeProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#targetNode");
        Predicate<Statement> targetPredicate = s -> s.getPredicate().equals(targetNodeProperty);
        originalShapesNoTargets.add(originalShapesModel.listStatements().filterDrop(targetPredicate).toList());

        Model updatedShapesModel = ModelFactory.createDefaultModel();
        updatedShapesModel.add(originalShapesModel);

        List<Action> shallowCopy = new ArrayList<Action>(actions);
        /** Transformation needs to be in reverse order according to definition in paper */
        Collections.reverse(shallowCopy);
        for (Action action : shallowCopy) {
            if (action.addsAClass()) {
                final Property classProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#class");
                final Property orProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#or");
                Resource actionConcept = originalShapesNoTargets.createResource(action.predicatePart);
                Resource actionNewResource = originalShapesNoTargets.createResource(action.variableExpressionPart);
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
                } else {
                    /**
                     * Some constraint components declare only a single parameter.
                     * For example sh:ClassConstraintComponent has the single parameter sh:class.
                     * These parameters may be used multiple times in the same shape, and each value
                     * of such a parameter declares an individual constraint.
                     * The interpretation of such declarations is conjunction, i.e. all constraints apply.
                     * See https://www.w3.org/TR/2017/REC-shacl-20170720/#constraints
                     *
                     * Thus, we do not need to use sh:and
                     */
                    originalShapesNoTargets.listResourcesWithProperty(classProperty).forEach(subject -> {
                        NodeIterator objects = originalShapesNoTargets.listObjectsOfProperty(subject, classProperty);
                        objects.forEach(object -> {
                            if (object.asResource().equals(actionNewResource)) {
                                Resource anonBetweenNotAndConcept = updatedShapesModel.createResource();
                                final Property notProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#not");
                                updatedShapesModel.add(subject, notProperty, anonBetweenNotAndConcept);
                                actionConcept.listProperties().forEach(statement -> updatedShapesModel.add(anonBetweenNotAndConcept, statement.getPredicate(), statement.getObject()));
                            }
                        });
                    });
                }
            } else {
                final Property pathProperty = ResourceFactory.createProperty(pathUri);
                final Property altPathProperty = ResourceFactory.createProperty(alternativePathUri);
                Resource basicObjectProperty = originalShapesNoTargets.createResource(action.predicatePart);
                Resource actionNewResource = originalShapesNoTargets.createResource(action.variableExpressionPart);
                if (action.actionType.equals(Action.ActionType.PLUS)) {
                    final Property propertyProperty = ResourceFactory.createProperty(propertyUri);
                    originalShapesNoTargets.listResourcesWithProperty(propertyProperty).forEach(shapeBeforeProperty -> {
                        NodeIterator afterPropertyIt = originalShapesNoTargets.listObjectsOfProperty(shapeBeforeProperty, propertyProperty);
                        afterPropertyIt.forEach(afterPropertyNode -> {
                            NodeIterator afterPathIt = originalShapesNoTargets.listObjectsOfProperty(afterPropertyNode.asResource(), pathProperty);
                            afterPathIt.forEach(afterPathNode -> {
                                if (afterPathNode.asResource().equals(actionNewResource)) {
                                    Resource anonBetweenPathAndAltPath = updatedShapesModel.createResource();
                                    List<RDFNode> nodesForList = new ArrayList<>();
                                    nodesForList.add(afterPathNode);
                                    nodesForList.addAll(basicObjectProperty.listProperties(pathProperty).mapWith(Statement::getObject).toList());
                                    RDFList alternativePathList = updatedShapesModel.createList(nodesForList.iterator());
                                    updatedShapesModel.remove(afterPropertyNode.asResource(), pathProperty, afterPathNode);
                                    updatedShapesModel.add(afterPropertyNode.asResource(), pathProperty, anonBetweenPathAndAltPath);
                                    updatedShapesModel.add(anonBetweenPathAndAltPath.asResource(), altPathProperty, alternativePathList);
                                }
                            });
                        });
                    });
                } else {
                    String msg = "Not yet possible in a useful way, as the SHACL recommendation and Apache Jena "
                            + "do not implement difference in property paths";
                    throw new NotImplementedException(msg);
                }
            }
        }
        return updatedShapesModel.getGraph();

    }
}
