#!/bin/bash
#
# Use this shell script to compile (if necessary) your code and then execute it. Below is an example of what might be found in this file if your program was written in Python
#
#python ./src/find_political_donors.py ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt

# Rebuild class file(s)
javac -d ./src/class/ ./src/java/InsightDS.java
# Rebuild JAR
jar cfe ./src/FindPoliDonors.jar InsightDS -C ./src/class/ InsightDS.class
# Run JAR with parameters: input, output1, output2
java -jar ./src/FindPoliDonors.jar ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt