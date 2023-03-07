package com.validatingevolvingrdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionUtil {

    // TODO make into List because actions are ordered
    public static Set<Action> parse(String actionsPath) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(actionsPath));
        Set<Action> resultSet = new HashSet<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.isBlank()) {
                continue;
            }
            String targetUri;
            Action.ActionType actionType;
            String selectorShapeName;
            Pattern prefixPattern = Pattern.compile("([+-]) (\\S+) (\\S+)");
            Matcher prefixMatcher = prefixPattern.matcher(line);
            Pattern infixPattern = Pattern.compile("(\\S+) ([+-]) (\\S+)");
            Matcher infixMatcher = infixPattern.matcher(line);
            if (prefixMatcher.matches() || infixMatcher.matches()) {
                if (prefixMatcher.matches()) {
                    targetUri = prefixMatcher.group(2);
                    actionType = "+".equals(prefixMatcher.group(1)) ? Action.ActionType.PLUS : Action.ActionType.MINUS;
                    selectorShapeName = prefixMatcher.group(3);
                } else {
                    targetUri = infixMatcher.group(1);
                    actionType = "+".equals(infixMatcher.group(2)) ? Action.ActionType.PLUS : Action.ActionType.MINUS;
                    selectorShapeName = infixMatcher.group(3);
                }
                resultSet.add(new Action(targetUri, actionType, selectorShapeName));
            } else {
                System.err.println("Could not match the line, thus ignoring it:");
                System.err.println(line);
                System.err.println();
            }
        }
        sc.close();
        return resultSet;
    }

    public static Model apply(Set<Action> actions, Model originalDataModel, Graph originalShapesGraph) {
        Model updatedModel = ModelFactory.createDefaultModel();
        /* It is important to do this first, because otherwise removing nodes does not work */
        updatedModel.add(originalDataModel);


        /* We want to remove the targets from the original shapes graph to add our own targets later */
        Model originalShapesGraphAsModel = ModelFactory.createModelForGraph(originalShapesGraph);
        Model shapesGraphNoTargets = ModelFactory.createDefaultModel();
        /* For proper equality between two Property */
        Property targetNodeProperty = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#targetNode");
        Predicate<Statement> targetPredicate = s -> s.getPredicate().equals(targetNodeProperty);
        shapesGraphNoTargets.add(originalShapesGraphAsModel.listStatements().filterDrop(targetPredicate).toList());

        /* In the words of the formalization, we need to check for each node in the data graph if it is part
          of the evaluation (which is a set of nodes) of the right side of the action
         */
        for (Action action : actions) {
            if (action.addsAClass()) {
                /* The right side of the action  is called concept and chooses on which nodes the left side is applied*/
                Resource concept = ResourceFactory.createResource(action.predicatePart);
                Set<Resource> conceptHoldsForThese = new HashSet<>();
                originalDataModel.listSubjects().forEach(resource ->
                {
                    /* For each node in the data graph, we check if it is in the concept */
                    shapesGraphNoTargets.add(concept, targetNodeProperty, resource);
                    ValidationReport report = ShaclValidator.get().validate(shapesGraphNoTargets.getGraph(), originalDataModel.getGraph());
                    if (report.conforms()) {
                        conceptHoldsForThese.add(resource);
                    }
                    /* Always validate one node target */
                    shapesGraphNoTargets.remove(concept, targetNodeProperty, resource);
                });
                /* Then we perform the actions for all nodes for which the concept holds */
                for (Resource resource : conceptHoldsForThese) {
                    RDFNode newNode = updatedModel.createResource(action.variableExpressionPart);
                    Property typeProperty = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                    if (action.actionType.equals(Action.ActionType.PLUS)) {
                        updatedModel.add(resource, typeProperty, newNode);
                    } else {
                        updatedModel.remove(resource, typeProperty, newNode);

                    }
                }
            } else {
                /* The right side of the action  is called basicObjectProperty and chooses on which nodes the left side is applied*/
                originalDataModel.listStatements().forEach(s -> {
                    Property basicObjectProperty = shapesGraphNoTargets.createProperty(action.predicatePart);
                    Property pathProperty = shapesGraphNoTargets.createProperty("http://www.w3.org/ns/shacl#path");
                    Resource bopContent = basicObjectProperty.getRequiredProperty(pathProperty).getObject().asResource();
                    Statement oldTriple = shapesGraphNoTargets.createStatement(s.getSubject(), updatedModel.createProperty(bopContent.toString()), s.getObject());
                    Statement newTriple = shapesGraphNoTargets.createStatement(s.getSubject(), updatedModel.createProperty(action.variableExpressionPart), s.getObject());
                    if (originalDataModel.contains(oldTriple)) {
                        if (action.actionType.equals(Action.ActionType.PLUS)) {
                            updatedModel.add(newTriple);
                        } else {
                            updatedModel.remove(newTriple);
                        }
                    }
                });
            }
        }
        return updatedModel;
    }
}
