# Make common code available to the JS module
export APEXLINK=$HOME/Projects/ApexLink
rm -f samples src/main/scala/com/nawforce/common src/test/scala/com/nawforce/common
ln -s $APEXLINK/src/main/scala/com/nawforce/common src/main/scala/com/nawforce/common
ln -s $APEXLINK/src/test/scala/com/nawforce/common src/test/scala/com/nawforce/common
ln -s $APEXLINK/samples samples
