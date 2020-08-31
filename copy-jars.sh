#!/bin/bash

(cd ../ApexLink; mvn dependency:copy-dependencies -DincludeScope=runtime)
rm -rf npm/jars
mkdir npm/jars
cp ../ApexLink/target/apexlink*.jar npm/jars/.
rm npm/jars/apexlink*-javadoc.jar
rm npm/jars/apexlink*-sources.jar
cp ../ApexLink/target/dependency/* npm/jars/.
