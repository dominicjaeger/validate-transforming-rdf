# SHACL validation of evolving RDF graphs

Implementation of a transformation that takes
a set of SHACL constraints
and a set of actions
and transforms them into a modified set of constraints

For the MeSH experiment, use

```
java -jar -Xmx9g .\validatingevolvingrdf-0.1-jar-with-dependencies.jar "C:\Users\domin\ThesisResources\mesh\shapes.ttl" "C:\Users\domin\ThesisResources\mesh\actions" "C:\Users\domin\ThesisResources\mesh\mesh2022.nt"
```

For the Yago experiment, use
```
java -jar -Xmx9g .\validatingevolvingrdf-0.1-jar-with-dependencies.jar "C:\Users\domin\ThesisResources\yago\shapes.nt" "C:\Users\domin\ThesisResources\yago\actions"
```

