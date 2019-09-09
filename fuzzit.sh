mvn package

## Install fuzzit specific version for production or latest version for development :
# https://github.com/fuzzitdev/fuzzit/releases/latest/download/fuzzit_Linux_x86_64
wget -q -O fuzzit https://github.com/fuzzitdev/fuzzit/releases/download/v2.4.51/fuzzit_Linux_x86_64
chmod a+x fuzzit

## upload fuzz target for long fuzz testing on fuzzit.dev server or run locally for regression
./fuzzit create job --engine jqf --type ${1} --args "dev.fuzzit.examplejava.ParseComplexTest fuzz" fuzzitdev/parse-complex ./target/example-java-1.0-SNAPSHOT-fat-tests.jar
