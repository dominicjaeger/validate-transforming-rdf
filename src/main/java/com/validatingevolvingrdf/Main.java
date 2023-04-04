package com.validatingevolvingrdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {
    private final static String YAGO_QUERY_LIMIT = "10"; // Pay attention to whitespace before
    private final static String meshBasePath = "src/main/resources/mesh2022Top1000/";
    private final static String yagoBasePath = "src/main/resources/yago/";

    public static void main(String[] args) {
        if (args.length > 3) {
            String shapesPath = args[1];
            String actionsPath = args[2];
            System.out.println("Using shapes graph: " + shapesPath);
            System.out.println("Using actions: " + actionsPath);
            if ("local".equals(args[0])) {
                String dataPath = args[3];
                System.out.println("Using data graph: " + dataPath);
                try {
                    localExperiment(dataPath, shapesPath, actionsPath);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else if ("remote".equals(args[0])) {
                String yagoQueryLimit = args[3];
                System.out.println("Getting Yago data with SPARQL with LIMIT " + yagoQueryLimit + ".");
                try {
                    remoteExperiment("https://yago-knowledge.org/sparql/query", shapesPath, actionsPath, yagoQueryLimit);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            System.err.println("Wrong usage. Please check documentation.");
        }
    }

    private static Model applyWithTime(List<Action> actions, Model originalDataModel, Graph originalShapesGraph) {
        Instant startTimeToUpdateData = Instant.now();
        Model updatedModel = ActionUtil.apply(actions, originalDataModel, originalShapesGraph);
        Instant endTimeToUpdateData = Instant.now();
        System.out.println("Time to update data: " + Duration.between(startTimeToUpdateData, endTimeToUpdateData));
        System.out.println("Updated model has " + updatedModel.size() + " statements.");
        System.out.println();
        return updatedModel;
    }

    private static Graph transformWithTime(Model originalShapesModel, List<Action> actions) {
        Instant startTimeToTransformShapes = Instant.now();
        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);
        Instant endTimeToTransformShapes = Instant.now();
        System.out.println("Time to transform shapes: " + Duration.between(startTimeToTransformShapes, endTimeToTransformShapes));
        return updatedShapesGraph;
    }

    private static void doValidation(Graph originalShapesGraph, Graph updatedShapesGraph, Graph originalDataGraph, Graph updatedDataGraph) {
        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report1 = validator.validate(originalShapesGraph, updatedDataGraph);
        Instant startTimeForUpdatedShapesValidation = Instant.now();
        ValidationReport report2 = validator.validate(updatedShapesGraph, originalDataGraph);
        Instant endTimeForUpdatedShapesValidation = Instant.now();
        System.out.println("Time to validate original data with updated shapes: " +
                Duration.between(startTimeForUpdatedShapesValidation, endTimeForUpdatedShapesValidation));
        if (report1.conforms() == report2.conforms()) {
            System.out.println("Both reports report the same.");
        } else {
            System.err.println("Reports have a different result. This must not happen.");
        }
        // writeReports(report1, report2, System.out);
    }

    private static void remoteExperiment(String queryService, String shapesPath, String actionsPath, String queryLimit) throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(shapesPath);
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(actionsPath);
        /** Print the Yago shapes using the following line */
//        Util.debugPrint(null, yagoShapes, null,null,null, null);

        String query = "DESCRIBE ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Place> } LIMIT " + queryLimit;
        Query places = QueryFactory.create(query);
        Model originalDataModel = ModelFactory.createDefaultModel();
        try (RDFConnection conn = RDFConnection.connect(queryService)) {
            originalDataModel.add(conn.queryConstruct(places));
        }
//        originalDataModel.listStatements().forEach(System.out::println);
        System.out.println("Original model has " + originalDataModel.size() + " statements.");

        Model updatedModel = applyWithTime(actions, originalDataModel, originalShapesGraph);
        Graph updatedShapesGraph = transformWithTime(originalShapesModel, actions);
        doValidation(originalShapesGraph, updatedShapesGraph, originalDataModel.getGraph(), updatedModel.getGraph());
    }

    private static void localExperiment(String dataGraphPath, String shapesGraphPath, String actionsPath) throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(shapesGraphPath);
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(actionsPath);
        Graph originalDataGraph = RDFDataMgr.loadGraph(dataGraphPath);

        Model originalDataModel = ModelFactory.createModelForGraph(originalDataGraph);
        System.out.println("Original model has " + originalDataModel.size() + " statements.");

        Model updatedModel = applyWithTime(actions, originalDataModel, originalShapesGraph);
        Graph updatedShapesGraph = transformWithTime(originalShapesModel, actions);
        doValidation(originalShapesGraph, updatedShapesGraph, originalDataGraph, updatedModel.getGraph());
    }

    private static void writeReports(ValidationReport originalShapesReport, ValidationReport updatedShapesReport, OutputStream out) {
        if (originalShapesReport != null) {
            System.out.println("ShLib report (Original shapes, updated data):");
            ShLib.printReport(originalShapesReport);
            System.out.println();
            System.out.println("Report model (Original shapes, updated data): ");
            RDFDataMgr.write(out, originalShapesReport.getModel(), Lang.TTL);
        }

        if (updatedShapesReport != null) {
            System.out.println("ShLib report (Original data, updated shapes):");
            ShLib.printReport(updatedShapesReport);
            System.out.println();
            System.out.println("Report model (Original data, updated shapes): ");
            RDFDataMgr.write(out, updatedShapesReport.getModel(), Lang.TTL);
        }
    }
}
