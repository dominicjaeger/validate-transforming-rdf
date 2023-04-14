# SHACL validation of evolving RDF graphs

Implementation of a transformation that takes
a set of SHACL constraints
and a sequence of actions
and transforms them into a modified set of constraints.

## Usage

Each experiment requires multiple command-line arguments
<ol>
  <li>either the world `local` or `remote`</li>
  <li>the path to a file with the shapes graph</li>
<li>the path to a file with the actions</li>
<li>for the data graph</li>
  <ul>
  <li> either the path to a file with the data graph</li>
  <li> or a number as LIMIT for a SPARQL query to the Yago experiment</li>
  </ul>
</ol>


Optional: To build the project, install Maven and run
```
mvn package
```
which will place the results in the subfolder `target`

For the Medical Subject Headings (MeSH) experiment, use

```
java -jar -Xmx9g .\validatingevolvingrdf-0.2-jar-with-dependencies.jar local <pathToShapes> <pathToActions> <pathToDataGraph>
```

For the Yago experiment, use
```
java -jar -Xmx9g .\validatingevolvingrdf-0.2-jar-with-dependencies.jar remote <pathToShapes> <pathToActions> 1000
```

## Main algorithms

The algorithms are in the directory
[src/main/java/com/validatingevolvingrdf](https://github.com/dominicjaeger/validate-transforming-rdf/tree/main/src/main/java/com/validatingevolvingrdf).
In this folder,
- [Transformer.java](https://github.com/dominicjaeger/validate-transforming-rdf/blob/dev/src/main/java/com/validatingevolvingrdf/Transformer.java) contains the main transformation algorithm
- [ActionUtil.java](https://github.com/dominicjaeger/validate-transforming-rdf/blob/dev/src/main/java/com/validatingevolvingrdf/ActionUtil.java) contains the parser for the action files as well as the algorithm to apply actions to a data graph
- [Main.java](https://github.com/dominicjaeger/validate-transforming-rdf/blob/dev/src/main/java/com/validatingevolvingrdf/Main.java) contains the code that performs local experiments (MeSH) and remote experiments (using the Yago SPARQL endpoint).


## Resources

The directory
[src/main/resources](https://github.com/dominicjaeger/validate-transforming-rdf/tree/main/src/main/resources)
contains the resources that were used during the experiments.

The [shapes graph for the Yago experiment](https://github.com/dominicjaeger/validate-transforming-rdf/blob/dev/src/main/resources/yago/shapes.nt)
is a copy of the SHACL constraints available at the
[Yago project page](https://yago-knowledge.org/downloads/yago-4).
The shapes graph for the MeSH experiment and the actions are custom made.

The data graphs are not included due to the large file sizes.
The data graph for the MeSH experiment can be downloaded as single file at
[https://www.nlm.nih.gov/databases/download/mesh.html](https://www.nlm.nih.gov/databases/download/mesh.html) 
courtesy of the U.S. National Library of Medicine.
We query the Yago data graph using the SPARQL endpoint
[https://yago-knowledge.org/sparql/query](https://yago-knowledge.org/sparql/query).

## Test cases

The directory
[src/test/resources/main](https://github.com/dominicjaeger/validate-transforming-rdf/tree/main/src/test/resources/main)
contains the most important test cases.
They display how the transformation works and check that *G* validates *(C,T)* if and only if *G<sup>α</sup>* validates *(C<sup>α</sup>,T)*.
The folders contain four files:
1. `actions` a sequence of updates
2. `shapes.ttl` the original shapes graph
3. `shapesGoal.ttl` the expected result of the transformation
4. `data.ttl` the data graph for the test case

The unit tests for these test cases are located in
[src/test/java](https://github.com/dominicjaeger/validate-transforming-rdf/tree/main/src/test/java).
The most important test cases start with `TestMain...`.
They test
- if the transformation algorithm correctly transforms the graph from `shapes.ttl` into the expected `shapesGoal.ttl`
- if the validation report is the same for the original data graph with the updated shapes graph as for the updated data graph with the original shapes graph
- some additional smaller tests, like if some triples are contained in the updated data graph

Tests are automatically executed whenever a push to the repository happens.
The results can be seen in the [GitHub Actions](https://github.com/dominicjaeger/validate-transforming-rdf/actions).