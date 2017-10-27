/*
 * File: InsightDS.java
 * Description: Parses 'Contributions by Individuals' files found: http://classic.fec.gov/finance/disclosure/ftpdet.shtml
 *
 * Created: October 26, 2017
 * Last Change: October 27, 2017 16:44:00
 *
 * Author Name: Michael Corso
 * Author E-Mail: mc5262@nyu.edu
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InsightDS {

    // VERBOSE OUTPUT TO HELP DEBUG / TEST SPEED
    private static boolean debug = false;
    
    // OUTPUT FILES
    private static File zipOutputFile, dateOutputFile;
    // HASHMAP FOR DATE DATA: <CMTE_ID, <DATE, CONTRIBUTIONS>>
    private static LinkedHashMap<String, LinkedHashMap<String, ArrayList<Integer>>> contributionsByDate = new LinkedHashMap<>();
    // HASHMAP FOR ZIP DATA: <ZIP_CODE, CONTRIBUTIONS>
    private static LinkedHashMap<String, ArrayList<Integer>> contributionsByZip = new LinkedHashMap<>();
    // STORE OUTPUT LINES BEFORE READY TO BE WRITTEN TO FILES
    private static ArrayList<String> zipCodeOutput = new ArrayList<>(), dateOutput = new ArrayList<>();

    // SORTING ALG TO SORT CONTRIBUTIONS
    private static void insertionSort(ArrayList<Integer> contributions, int insert) {

        if (contributions == null) {
            contributions = new ArrayList<>();
            contributions.add(insert);
        }
        else if (contributions.size() == 0) {
            contributions.add(insert);
        }
        else if (contributions.size() == 1) {
            if (contributions.get(0) < insert) contributions.add(insert);
            else contributions.add(0, insert);
        }
        else {
            for (int i = 0; i < contributions.size() - 1; i++) {
                if (insert <= contributions.get(i)) {
                    contributions.add(i, insert);
                    return;
                }
            }
            contributions.add(insert);
        }
    }

    // CHECKS THE OUTPUT DIRECTORY / FOLDERS
    private static void checkOutputs() {

        try {

            // CHECK IF OUTPUT FILES EXIST
            if (zipOutputFile.exists() || dateOutputFile.exists()) {
                Scanner scan = new Scanner(System.in);
                System.out.println(String.format("\nOne or more of the specified output files exist, " +
                        "do you want to overwrite them?\nType 'Yes' to continue."));
                if (!scan.nextLine().toLowerCase().equals("yes")) System.exit(1);
            }
            // CHECK IF THIS PROGRAM CAN CREATE THE OUTPUT FILES
            try {
                zipOutputFile.createNewFile();
            }
            catch (Throwable t) {
                throw new Throwable(String.format("Error: Failed to create output file: " + zipOutputFile));
            }
            try {
                dateOutputFile.createNewFile();
            }
            catch (Throwable t) {
                throw new Throwable(String.format("Error: Failed to create output file: " + zipOutputFile));
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    // IF DATE IS VALID RETURN TRUE, ELSE RETURN FALSE
    private static boolean validateDate(String date) {
        try {
            LocalDate ld = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMddyyyy"));
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }

    // RECORD ENTRY FOR DATE OUTPUT
    private static void recordDateEntry(String cmte_id, String t_date, String t_amount) {

        LinkedHashMap<String, ArrayList<Integer>> innerMap = new LinkedHashMap<>();
        contributionsByDate.putIfAbsent(cmte_id, innerMap);

        // COMMITTEE ID & DATE COMBINATION ALREADY EXIST
        if (contributionsByDate.get(cmte_id).containsKey(t_date)) {
            innerMap = new LinkedHashMap<>(contributionsByDate.get(cmte_id));
            insertionSort(contributionsByDate.get(cmte_id).get(t_date), Integer.parseInt(t_amount));
        }
        // COMMITTEE ID & DATE COMBINATION IS A NEW ENTRY
        else {
            contributionsByDate.get(cmte_id).put(t_date, new ArrayList<Integer>());
            contributionsByDate.get(cmte_id).get(t_date).add(Integer.parseInt(t_amount));
        }
        // UPDATE HASHMAP
        innerMap.put(t_date, contributionsByDate.get(cmte_id).get(t_date));
        contributionsByDate.put(cmte_id, innerMap);
    }

    // RECORD ENTRY FOR  ZIP CODE OUTPUT
    private static void recordZipCodeEntry(String cmte_id, String zip_code, String t_amount) {

        // THIS ZIP CODE EXISTS
        if (contributionsByZip.containsKey(zip_code)) {
            insertionSort(contributionsByZip.get(zip_code), Integer.parseInt(t_amount));
        }
        // THIS ZIP CODE IS A NEW ENTRY
        else {
            contributionsByZip.put(zip_code, new ArrayList<Integer>());
            contributionsByZip.get(zip_code).add(Integer.parseInt(t_amount));
        }

        // NUMBER OF CONTRIBUTIONS
        int transaction_count = contributionsByZip.get(zip_code).size();
        // SUM OF ALL CONTRIBUTIONS
        int cumulative_contributions = contributionsByZip.get(zip_code).stream().mapToInt(value -> value).sum();
        // MEDIAN OF ALL CONTRIBUTIONS
        double median;
        if ((transaction_count % 2) == 0) {
            median = ((contributionsByZip.get(zip_code).get(transaction_count / 2) +
                    contributionsByZip.get(zip_code).get((transaction_count / 2) - 1)) / 2.0);
        }
        else median = contributionsByZip.get(zip_code).get(transaction_count / 2);

        // RECORD ENTRY TO BE OUTPUT LATER
        zipCodeOutput.add(String.format("%s|%s|%s|%s|%s",
                cmte_id, zip_code, Math.round(median), transaction_count, cumulative_contributions));
    }

    // ITERATE THROUGH DATE RECORD ENTRIES TO COMBINE DATA BEFORE OUTPUT
    private static void generateDateOutput() {

        // FOR EACH COMMITTEE ID
        for (String id : contributionsByDate.keySet()) {

            // FOR EACH DATE TRANSACTION DATE UNDER THIS COMMITTEE ID
            for (String date : contributionsByDate.get(id).keySet()) {

                // TOTAL NUMBER OF CONTRIBUTIONS FROM THIS ID/DATE COMBINATION
                int transaction_count = contributionsByDate.get(id).get(date).size();
                // SUM OF ALL CONTRIBUTIONS FROM THIS ID/DATE COMBINATION
                int cumulative_contributions = contributionsByDate.get(id).get(date).stream().mapToInt(value -> value).sum();
                // MEDIAN OF ALL CONTRIBUTIONS FROM THIS ID/DATE COMBINATION
                double median;
                if ((transaction_count % 2) == 0) {
                    median = ((contributionsByDate.get(id).get(date).get(transaction_count / 2) +
                            contributionsByDate.get(id).get(date).get((transaction_count / 2) - 1)) / 2.0);
                }
                else median = contributionsByDate.get(id).get(date).get(transaction_count / 2);
                // RECORD ENTRY TO BE OUTPUT LATER
                dateOutput.add(String.format("%s|%s|%s|%s|%s",
                        id, date, Math.round(median), transaction_count, cumulative_contributions));
            }
        }
    }

    // PARSE INPUT FILE
    private static void parseInputFile(File inputFile) {

        try {
            // DEBUG / TESTING VARS
            long start = System.currentTimeMillis();
            int count = 0;

            // HASHMAP TO STORE DATA
            contributionsByDate = new LinkedHashMap<>();
            contributionsByZip = new LinkedHashMap<>();

            // RELEVANT RECORD DATA
            String line, cmte_id, zip_code, t_date, t_amount, other_id;

            // PARSE LINE-BY-LINE
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            while ((line = br.readLine()) != null) {
                try {
                    if (debug) {
                        count++;
                        if (count % 10000 == 0) System.out.print(String.format("\rParsing line # %,d.",
                                count));
                    }
                    // FLAGS FOR IF WE'RE KEEPING THIS RECORD FOR THE SPECIFIED OUTPUT
                    //      { medianvals_by_date, medianvals_by_zip }
                    boolean[] flags = {true, true};

                    // GET RELEVANT RECORD DATA
                    cmte_id = line.split("\\|")[0];
                    zip_code = line.split("\\|")[10];
                    t_date = line.split("\\|")[13];
                    t_amount = line.split("\\|")[14];
                    other_id = line.split("\\|")[15];

                    // SKIP RECORD FOR ALL OUTPUTS IF: other_id, cmte_id, OR t_amount ARE EMPTY
                    if (!other_id.replace(" ", "").equals("")) continue;
                    if (cmte_id.replace(" ", "").equals("")) continue;
                    if (t_amount.replace(" ", "").equals("")) continue;

                    // CHECK IF DATE IS MALFORMED
                    flags[0] = validateDate(t_date);

                    // CHECK IF ZIP CODE IS MALFORMED
                    if (zip_code.length() < 5) flags[1] = false;
                    else if (zip_code.length() > 5) zip_code = zip_code.substring(0, 5);

                    // RECORD RECORD IF FLAG IS TRUE
                    if (flags[0]) recordDateEntry(cmte_id, t_date, t_amount);
                    if (flags[1]) recordZipCodeEntry(cmte_id, zip_code, t_amount);

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (debug) {
                long end = System.currentTimeMillis();
                System.out.println(String.format("\nTime to parse file: %s seconds.",
                        ((end - start) / 1000)));
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // WRITE TO FILE
    private static void writeFiles() {
        // ZIP CODE FILE
        try {
            if (debug) System.out.println(String.format("\nWriting to: %s", zipOutputFile.getPath()));
            long start = System.currentTimeMillis();
            String out = String.join("\n", zipCodeOutput);
            Files.write(Paths.get(zipOutputFile.getAbsolutePath()), out.getBytes());
            if (debug) {
                long end = System.currentTimeMillis();
                System.out.println(String.format("Time to write %s: %s seconds.",
                        zipOutputFile.getName(), ((end - start) / 1000)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // DATE FILE
        try {
            if (debug) System.out.println(String.format("\nWriting to: %s", dateOutputFile.getPath()));
            long start = System.currentTimeMillis();
            String out = String.join("\n", dateOutput);
            Files.write(Paths.get(dateOutputFile.getAbsolutePath()), out.getBytes());
            if (debug) {
                long end = System.currentTimeMillis();
                System.out.println(String.format("Time to write %s: %s seconds.",
                        dateOutputFile.getName(), ((end - start) / 1000)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void howToUse() {
        System.out.println(String.format("\nHow to use:" +
                "\n\tjava -jar [fileName] [inputFile] [zipCodeOutFile] [dateOutFile]" +
                "\n\tIE: java -jar PoliDonors ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt"));
        System.exit(1);
    }

    public static void main(String[] args) {

        try {

            // REQUIRES 3 ARGUMENTS: INPUT, ZIP OUTPUT,
            if (args.length != 3) howToUse();

            // CHECK INPUT FILE
            File inputFile = new File(args[0]);
            if (!inputFile.exists()) {
                System.out.println("Error: Input file does not exist.");
                howToUse();
            }

            // CHECK OUTPUT FILES
            zipOutputFile = new File(args[1]);
            dateOutputFile = new File(args[2]);
            checkOutputs();

            // PARSE FILE AND GENERATE OUTPUT
            parseInputFile(inputFile);
            generateDateOutput();
            writeFiles();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
