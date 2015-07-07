#!/bin/sh

EXAMPLE_CP=lib/scribe-1.3.5.jar:lib/json_simple-1.1.jar:lib/log4j-api-2.0.2.jar:lib/log4j-core-2.0.2.jar:lib/junit.jar:lib/org.hamcrest.core_1.3.0.v201303031735.jar:.

javac -classpath $EXAMPLE_CP *.java && java -classpath $EXAMPLE_CP org.junit.runner.JUnitCore ClientTest "$@"
