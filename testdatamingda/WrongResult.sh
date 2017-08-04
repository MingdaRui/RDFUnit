#!/bin/bash

#
# example call for dump-based testing (You can use either a URI or a local file name):
# bin/rdfunit -d https://raw.github.ocm/AKSW/n3-collection/master/News-100.ttl -s nif
#

MAIN_CLS="org.aksw.rdfunit.validate.cli.ValidateCLI"

# TODO use the jar for faster execution
#if [-s bin/rdfunit.jar ]
#then
#    mvn assembly:single -DdescriptorId=jar-with-dependencies
#            cp /tmp/docu.html "$doc"".html"
#fi

if [ ! -d "rdfunit-validate/target" ]; then
  echo "First run, compiling code..."
  mvn -pl rdfunit-validate -am clean install \
      -Dmaven.test.skip=true \
      -Dmaven.javadoc.skip=ture \
      -Dgpg.skip=true \
      -Dsource.skip=true
  echo "Compiling finished..."
fi

mvn -pl rdfunit-validate exec:java -q -Dexec.mainClass="$MAIN_CLS" -Dexec.args="$*"
