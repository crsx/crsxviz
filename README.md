# crsxviz
Visualizer for CRSX debug traces

## CRSX

CRSX is a compiler for higher-order rewriting.
See https://github.com/crsx/crsx

CRSX has the ability to generate debug traces with the command
"crsx-debug-steps", which explains which terms were rewritten etc.

## CRSXVIZ

CRSXVIZ is a visualizer for those CRSX debug traces.

This project exists to work on this visualization effort.

## Build Instructions

To build a distributable jar package use the following command

```mvn```

The jar can be executed by running 

```
chmod a+x run.sh
./run.sh
```

License
=======

This program and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, which accompanies this
distribution, and is available at http://opensource.org/licenses/EPL-1.0.
