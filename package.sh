rm -rf target/npm
sbt clean
sbt createPackage
(cd target/npm/apex-assist; npm i --production; vsce package)
code --install-extension target/npm/apex-assist/apex-assist-*.vsix
