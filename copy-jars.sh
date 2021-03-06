#!/bin/bash

(cd ../apex-link; mvn dependency:copy-dependencies -DincludeScope=runtime)
rm -rf npm/jars
mkdir npm/jars
cp ../apex-link/target/apexlink*.jar npm/jars/.
rm npm/jars/apexlink*-javadoc.jar
rm npm/jars/apexlink*-sources.jar
cp ../apex-link/target/dependency/* npm/jars/.
