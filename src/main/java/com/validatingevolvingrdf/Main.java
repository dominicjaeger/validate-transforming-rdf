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
    private final static String meshBasePath = "src/main/resources/mesh2022Top1000/";
    private final static String yagoBasePath = "src/main/resources/yago/";

    /**
     * TODO for minus: SHACL paths have no NOT. SPARQL paths do and X AND NOT Y should be OK for difference.
     * Maybe use SHACL SPARQL for the MINUIS CASE https://www.w3.org/TR/2017/REC-shacl-20170720/#sparql-constraints-example
     * Paper says that sh:closed should also work
     * Maybe also some hack like with the implicit AND?
     *
     * @param args TODO check SPARQL CONSTRUCT for singletons/constants
     *             The graph template can contain triples with no variables (known as ground or explicit triples),
     *             and these also appear in the output RDF graph returned by the CONSTRUCT query form.
     */

    public static void main(String[] args) {
        if (args.length > 2) {
            System.out.println("Using shapes graph: " + args[0]);
            System.out.println("Using actions: " + args[1]);
            System.out.println("Using data graph: " + args[2]);
            try {
                localExperiment(args[2], args[0], args[1]);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else if (args.length > 1) {
            System.out.println("Using shapes graph: " + args[0]);
            System.out.println("Using actions: " + args[1]);
            System.out.println("Getting Yago data with SPARQL.");
            try {
                remoteExperiment("https://yago-knowledge.org/sparql/query", args[0], args[1]);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Incorrect usage. Please check documentation.");
        }

    }

    private static void remoteExperiment(String queryService, String shapesPath, String actionsPath) throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(shapesPath);
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(actionsPath);
        /** Print the Yago shapes using the following line */
//        Util.debugPrint(null, yagoShapes, null,null,null, null);

        /** E.g. for LIMIT 1000 we got
         * Original model has 1000 statements.
         * Updated model has 1609 statements.
         */
        String query = "DESCRIBE ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Place> } LIMIT 1000";
        Query places = QueryFactory.create(query);
        Model originalDataModel = ModelFactory.createDefaultModel();
        try (RDFConnection conn = RDFConnection.connect(queryService)) {
            originalDataModel.add(conn.queryConstruct(places));
        }
        /** Print the retrieved statements*/
        originalDataModel.listStatements().forEach(System.out::println);
        System.out.println("Original model has " + originalDataModel.size() + " statements.");

        Model updatedModel = ActionUtil.apply(actions, originalDataModel, originalShapesGraph);
        System.out.println("Updated model has " + updatedModel.size() + " statements.");
        System.out.println();

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);


        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report1 = validator.validate(originalShapesGraph, updatedModel.getGraph());
        ValidationReport report2 = validator.validate(updatedShapesGraph, originalDataModel.getGraph());
        if (report1.conforms() == report2.conforms()) {
            System.out.println("Both reports report the same.");
        } else {
            System.err.println("Reports have a different result. This must not happen.");
        }
        //        writeReports(report1, report2, System.out);

    }

    private static void localExperiment(String dataGraphPath, String shapesGraphPath, String actionsPath) throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(shapesGraphPath);
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        List<Action> actions = ActionUtil.parse(actionsPath);

        System.out.println("Starting to load data");
        Instant startDataLoad = Instant.now();
        Graph originalDataGraph = RDFDataMgr.loadGraph(dataGraphPath);
        Instant endDataLoad = Instant.now();
        System.out.println("Time to load data into graph: " + Duration.between(startDataLoad, endDataLoad));
        Model originalDataModel = ModelFactory.createModelForGraph(originalDataGraph);
        System.out.println("Original model has " + originalDataModel.size() + " statements.");
        System.out.println();

        Instant startTimeToUpdateData = Instant.now();
        // TODO At least so far with the implementation, time grows with the number of nodes in the original graph not in the updates
        Model updatedModel = ActionUtil.apply(actions, originalDataModel, originalShapesGraph);
        Instant endTimeToUpdateData = Instant.now();
        System.out.println("Time to update data: " + Duration.between(startTimeToUpdateData, endTimeToUpdateData));
        System.out.println("Updated model has " + updatedModel.size() + " statements.");
        System.out.println();

        Instant startTimeToTransformShapes = Instant.now();
        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);
        Instant endTimeToTransformShapes = Instant.now();
        System.out.println("Time to transform shapes: " + Duration.between(startTimeToTransformShapes, endTimeToTransformShapes));

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report1 = validator.validate(originalShapesGraph, updatedModel.getGraph());
        Instant startTimeForUpdatedShapesValidation = Instant.now();
        ValidationReport report2 = validator.validate(updatedShapesGraph, originalDataGraph);
        Instant endTimeForUpdatedShapesValidation = Instant.now();
        System.out.println("Time to validate original data with updated shapes: " +
                Duration.between(startTimeForUpdatedShapesValidation, endTimeForUpdatedShapesValidation));


        if (report1.conforms() == report2.conforms()) {
            System.out.println("Both reports reported the same.");
        } else {
            System.err.println("Reports had a different result. This must not happen.");
        }

        // writeReports(report1, report2, System.out);
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
