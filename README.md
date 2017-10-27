===============================================================

    Description: Parses 'Contributions by Individuals' files found at: 
    	http://classic.fec.gov/finance/disclosure/ftpdet.shtml
    Created: October 26, 2017
    Author Name :: E-Mail: Michael Corso :: mc5262@nyu.edu
    
===============================================================

BUILDING/RUNNING USING RUN.SH:

    sh ./run.sh
 
    Builds the .class (./src/class) and .jar (./src/) files and runs the jar with the following parameters:
    	java -jar ./src/FindPoliDonors.jar ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt
    
MANUALLY RUNNING JAR:
	
    java -jar FindPoliDonors.jar [input file] [zip code output file] [date output file]
    IE: java -jar ./src/FindPoliDonors.jar ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt

RUNNING TESTS:

   Three tests included:
       (1) Short test provided by Insight,
       (2) Modified test 1 to test for record-skipping situations, and
       (3) A longer test with 50,000 lines (mostly for efficiency testing)
   
   To run all tests:
       sh ./insight_testsuite/run_tests.sh

DEBUG/TIME INFO:

	To print to console debug/time info:
		(1) Open the source code (can be found: ./src/java/InsightDS.java)
		(2) Set line 26 (boolean debug) to true;
		(3) Rebuild using ./run.sh

SIMPLE OVERVIEW:

    (1) Input files are parsed line-by-line using BufferedReader (found to be most efficient based on tests).
    (2) Flags are maintained to determine whether the current record will be kept for zip and/or date outputs.
    (3) Record-data is stored in LinkedHashMaps to maintain order.
    (4) Zip code output is generated immediately but stored in an ArrayList until complete since this was faster than 
        performing File I/O on ArrayList dumps greater than size of 100. This efficiency was based on a file with 4.2
	million lines, this may need to be revisited with significantly larger file sizes.
    (5) After parsing the entire file, the date output data is generated, and the date and zip output data are written
        to their respective output files.
