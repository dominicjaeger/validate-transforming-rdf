package com.validatingevolvingrdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;
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
    private final static String basePath = "src/main/resources/mesh2022Top1000/";
    public static void main(String[] args) {
        String dataPath = basePath + "data.nt";
        if (args.length > 0) {
            System.out.println("Using graph data: " + args[0]);
            dataPath = args[0];
        } else {
            System.out.println("Using included MeSH data with top 1000 lines");
        }
        try {
            mesh(dataPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeReports(ValidationReport originalShapesReport, ValidationReport updatedShapesReport, OutputStream out) {
        System.out.println("ShLib report (Original shapes, updated data):");
        ShLib.printReport(originalShapesReport);
        System.out.println();
        System.out.println("Report model (Original shapes, updated data): ");
        RDFDataMgr.write(out, originalShapesReport.getModel(), Lang.TTL);

        System.out.println("ShLib report (Original data, updated shapes):");
        ShLib.printReport(updatedShapesReport);
        System.out.println();
        System.out.println("Report model (Original data, updated shapes): ");
        RDFDataMgr.write(out, updatedShapesReport.getModel(), Lang.TTL);
    }

    private static void mesh(String dataPath) throws FileNotFoundException {
        /* See License.md in the directory */
        Graph originalShapesGraph = RDFDataMgr.loadGraph(basePath + "shapes.ttl");
        Model originalShapesModel = ModelFactory.createModelForGraph(originalShapesGraph);
        Set<Action> actions = ActionUtil.parse(basePath + "actions");

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
}
