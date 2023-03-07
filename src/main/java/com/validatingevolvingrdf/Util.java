package com.validatingevolvingrdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import java.util.Set;

public class Util {

    private static NodeFormatter getFormatter(Graph shapesGraph) {
        PrefixMap pmap = PrefixMapFactory.create(ModelFactory.createModelForGraph(shapesGraph).getNsPrefixMap());
        return new NodeFormatterTTL("TEST", pmap);
    }

    private static void printShapesGraph(Graph shapesGraph) {
        Shapes shapes = Shapes.parse(shapesGraph);
        /*
          The default iterator in "Shapes" only considers shapes with targets, but we want all of them
          This implementation yields as side effect not only the "top level" but also nested shapes,
          e.g. implicit NodeShape in sh:or
          */
        for (Node s : shapes.getShapeMap().keySet()) {
            shapes.getShape(s).print(System.out, getFormatter(shapesGraph));
        }
    }


    public static void debugPrint(Graph originalDataGraph, Graph originalShapesGraph,
                                  Set<Action> actionSet, ValidationReport report,
                                  Graph updatedDataGraph, Graph updatedShapesGraph
    ) {
        if (report != null) {
            System.out.println();
            System.out.println("ShLib report:");
            ShLib.printReport(report);
            System.out.println();
            System.out.println("Report model: ");
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
            System.out.println();
            System.out.println();
        }

        if (originalDataGraph != null) {
            System.out.println("Original data graph is: ");
            System.out.println(originalDataGraph);
            System.out.println();
        }

        if (originalShapesGraph != null) {
            System.out.println("Original Shapes graph is: ");
            printShapesGraph(originalShapesGraph);
            System.out.println();
        }

        if (actionSet != null) {
            System.out.println("Actions are:");
            actionSet.forEach(System.out::println);
            System.out.println();
        }

        if (updatedDataGraph != null) {
            System.out.println("Updated data graph is: ");
            System.out.println(updatedDataGraph);
            System.out.println();
        }

        if (updatedShapesGraph != null) {
            System.out.println("Updated shapes graph is: ");
            printShapesGraph(updatedShapesGraph);
            System.out.println();
        }
    }
}
