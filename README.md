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

