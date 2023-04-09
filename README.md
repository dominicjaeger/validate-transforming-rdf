# SHACL validation of evolving RDF graphs

Implementation of a transformation that takes
a set of SHACL constraints
and a sequence of actions
and transforms them into a modified set of constraints.

## Experiments

Each experiment requires three command-line arguments
<ol>
  <li>the path to a file with the shapes graph</li>
<li>the path to a file with the actions</li>
<li>for the data graph</li>
  <ul>
  <li> either the path to a file with the data graph</li>
  <li> or a number as LIMIT for a SPARQL query to the Yago experiment</li>
  </ul>
</ol>

For the Medical Subject Headings (MeSH) experiment, use

```
java -jar -Xmx9g .\validatingevolvingrdf-0.1-jar-with-dependencies.jar local "C:\Users\domin\ThesisResources\mesh\shapes.ttl" "C:\Users\domin\ThesisResources\mesh\actions" "C:\Users\domin\ThesisResources\mesh\mesh2022.nt"
```

For the Yago experiment, use
```
java -jar -Xmx9g .\validatingevolvingrdf-0.1-jar-with-dependencies.jar remote "C:\Users\domin\ThesisResources\yago\shapes.nt" "C:\Users\domin\ThesisResources\yago\actions" 100
```

## Resources

The directory
[src/main/resources](https://github.com/dominicjaeger/validate-transforming-rdf/tree/dev/src/main/resources)
contains the resources that were used during the experiments.

The [shapes graph for the Yago experiment](https://github.com/dominicjaeger/validate-transforming-rdf/blob/dev/src/main/resources/yago/shapes.nt)
is a copy of the SHACL constraints available at the
[Yago project page](https://yago-knowledge.org/downloads/yago-4).
The shapes graph for the MeSH experiments and the actions are custom made.

The data graphs are not included due to the large file sizes.
The data graph for the MeSH experiment can be downloaded as single file at
[https://www.nlm.nih.gov/databases/download/mesh.html](https://www.nlm.nih.gov/databases/download/mesh.html) 
courtesy of the U.S. National Library of Medicine.
We query the Yago data graph using the SPARQL endpoint
[https://yago-knowledge.org/sparql/query](https://yago-knowledge.org/sparql/query).

