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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class Main {
    private final static String meshBasePath = "src/main/resources/mesh2022Top1000/";
    private final static String yagoBasePath = "src/main/resources/yago/";

    public static void main(String[] args) {
        try {
            yago();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String dataPath = meshBasePath + "data.nt";
//        if (args.length > 0) {
//            System.out.println("Using graph data: " + args[0]);
//            dataPath = args[0];
//        } else {
//            System.out.println("Using included MeSH data with top 1000 lines");
//        }
//        try {
//            mesh(dataPath);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static void yago() throws FileNotFoundException {
        Graph originalShapesGraph = RDFDataMgr.loadGraph(yagoBasePath + "shapes.nt");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        Set<Action> actions = ActionUtil.parse(yagoBasePath + "actions");
        /** Print the Yago shapes using the following line */
//        Util.debugPrint(null, yagoShapes, null,null,null, null);

        /** E.g. for LIMIT 1000 we got
         * Original model has 1000 statements.
         * Updated model has 1609 statements.
         */
        Query places = QueryFactory.create("DESCRIBE ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Place> } LIMIT 1000");
        String yagoQueryService = "https://yago-knowledge.org/sparql/query";
        Model originalDataModel = ModelFactory.createDefaultModel();
        try (RDFConnection conn = RDFConnection.connect(yagoQueryService)) {
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

        writeReports(report1, report2, System.out);
    }



    private static void mesh(String dataPath) throws FileNotFoundException {
        /* See License.md in the directory */
        Graph originalShapesGraph = RDFDataMgr.loadGraph(meshBasePath + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        Set<Action> actions = ActionUtil.parse(meshBasePath + "actions");

        System.out.println("Starting to load data");
        Instant startDataLoad = Instant.now();
        Graph originalDataGraph = RDFDataMgr.loadGraph(dataPath);
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

        Graph updatedShapesGraph = Transformer.transform(originalShapesModel, actions);

        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report1 = validator.validate(originalShapesGraph, updatedModel.getGraph());
        ValidationReport report2 = validator.validate(updatedShapesGraph, originalDataGraph);
        if (report1.conforms() == report2.conforms()) {
            System.out.println("Both reports report the same.");
        } else {
            System.err.println("Reports have a different result. This must not happen.");
        }

        writeReports(report1, report2, System.out);

        try {
            FileOutputStream fout = new FileOutputStream("target/outputText");
//            updatesOnly.write(fout, "TTL");
            writeReports(report1, report2, fout);
            fout.close();
        } catch (Exception e) {
            System.err.println(e);
        }

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
