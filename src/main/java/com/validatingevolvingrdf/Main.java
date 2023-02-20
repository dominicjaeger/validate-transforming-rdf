package com.validatingevolvingrdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileOutputStream;
import java.time.Duration;
import java.time.Instant;


@SuppressWarnings("ALL")
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello main method");
    }

    private static void lexvo() {
        String originalData = "C:/Users/domin/OneDrive/Desktop/lexvo_2013-02-09.rdf";

        Instant startDataLoad = Instant.now();
        Graph originalDataGraph = RDFDataMgr.loadGraph(originalData);
        Instant endDataLoad = Instant.now();
        System.out.println("Time to load data into graph: " + Duration.between(startDataLoad, endDataLoad));

        Instant startToModel = Instant.now();
        Model originalModel = ModelFactory.createModelForGraph(originalDataGraph);
        Instant endToModel = Instant.now();
        System.out.println("Time to create model from graph: " + Duration.between(startToModel, endToModel));
        System.out.println("Original model has " + originalModel.size() + " statements.");

        Model updatedModel = ModelFactory.createDefaultModel();
        updatedModel.add(originalModel);

        Property meansProperty = originalModel.getProperty("http://lexvo.org/ontology#", "means");
        Property nearlySameAsProperty = originalModel.createProperty("http://lexvo.org/ontology#", "nearlySameAs");

        Selector meansSelector = new SimpleSelector(null, meansProperty, (String) null);

        Instant startTimeToUpdateData = Instant.now();
        originalModel.listStatements(meansSelector).forEach(s -> updatedModel.add(s.getResource(), nearlySameAsProperty, s.getObject()));
        Instant endTimeToUpdateData = Instant.now();
        System.out.println("Time to update data: " + Duration.between(startTimeToUpdateData, endTimeToUpdateData));
        System.out.println("Updated model has " + updatedModel.size() + " statements.");

        Model updatesOnly = ModelFactory.createDefaultModel();
        originalModel.listStatements(meansSelector).forEach(s -> updatesOnly.add(s.getResource(), nearlySameAsProperty, s.getObject()));
        try {
            FileOutputStream fout = new FileOutputStream("target/updates.ttl");
            updatesOnly.write(fout, "TTL");
            fout.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
