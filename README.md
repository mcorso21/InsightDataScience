===============================================================

Description: Parses 'Contributions by Individuals' files found: http://classic.fec.gov/finance/disclosure/ftpdet.shtml

Created: October 26, 2017
Last Change: October 27, 2017

Author Name: Michael Corso
Author E-Mail: mc5262@nyu.edu

===============================================================

HOW TO USE:

    java -jar FindPoliDonors.jar [input file] [zip code output file] [date output file]
    IE: java -jar ./src/FindPoliDonors.jar ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt

DEBUG/TIME INFO:

	If you want the program to print to the console the current line it's parsing and the time spent:
		(1) Open the source code (can be found: ./src/java/InsightDS.java)
		(2) Set line 26 (boolean debug) to true;
		(3) Rebuild using ./run.sh

SIMPLE OVERVIEW:

    (1) Input files are parsed line-by-line using BufferedReader.
    (2) Flags are maintained to determine whether the current record will be kept for zip and/or date outputs.
    (3) Record-data is stored in LinkedHashMaps to maintain order.
    (4) Zip code output is generated immediately but stored in an ArrayList until complete since this was faster than 
        performing File I/O on ArrayList dumps greater than size = 100.
    (5) After parsing the entire file, the date output data is generated, and the date and zip output data are written
        to their respective output files.
