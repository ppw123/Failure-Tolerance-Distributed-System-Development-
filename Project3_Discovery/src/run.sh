#!/bin/sh

EXAMPLE_CP=lib/json_simple-1.1.jar:lib/log4j-api-2.0.2.jar:lib/log4j-core-2.0.2.jar:.

javac -classpath $EXAMPLE_CP *.java && java -classpath $EXAMPLE_CP DiscoveryServer $1 "$@"
