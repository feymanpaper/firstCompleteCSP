//import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Counter class to collect statistics and
 * print out in a readable format
 */
public class Counter {
    private boolean type;
    private String name;
    private long start;
    private long end;
    private int node;
    private int extra;

    // true for FC, false for MAC
    Counter(boolean type) {
        this.type = type;
        start = 0;
        end = 0;
        if(type) name = "Total FC Calls";
        else name = "Number of Arc Revisions";
        node = 0;
        extra = 0;
    }

    void setStart(){
        start = System.nanoTime();
    }

    void setEnd(){
        end = System.nanoTime();
    }

    // true for node, false for extra
    void increment(boolean type){
        if(type) node++;
        else extra++;
    }

    /**
     * called by printSol of the solver, prints out statistics
     * @param prob name of the  problem instance to record
     * @param printType printing type (T for statistics only in csv, F for full output with soln)
     * @param fn filepath to the output
     * @param heuristics type of heuristics
     * @throws IOException error in the case of invalid file path
     */
    void printStats(boolean exists, String prob, boolean printType, String fn, String heuristics) throws IOException {
        double milliseconds  = (double)(end - start) / 1000000.0;
        double seconds = milliseconds / 1000;
        String var, val;
        if(heuristics.equals("")){
            var = "sdf";
            val = "asc";
        }
        else {
            var = heuristics.split("#")[0];
            val = heuristics.split("#")[1];
        }
        File file = new File(fn);
        FileWriter fr = new FileWriter(file, true);
        if(!printType) {
            // write in a txt file
            fr.write("+++++++++++++++++" + prob + "+++++++++++++++++++++\n");
            if (type) {
                fr.write("=================== FC Output ===============\n");
            } else {
                fr.write("=================== MAC Output ===============\n");

            }
            fr.write("Depth: " + node + "\n");
            fr.write(name + ": " + extra + "\n");

            fr.write("Elapsed Time 1: " + String.format("%.2f", milliseconds) + " milliseconds\n");
            fr.write("Elapsed Time 2: " + String.format("%.2f", seconds) + " seconds\n");
            fr.write("Variable Ordering: " + var + "\n");
            fr.write("Value Ordering: " + val + "\n");
            fr.close();
        }
        else{
            // write a rwo entry to csv
            String solver = "FC";
            if(!type) solver = "MAC";
            BufferedWriter br = new BufferedWriter(fr);
            String extra = "";
            if(!exists) extra = ",no soln";
             br.write(prob + "," + solver + "," + String.format("%.2f", milliseconds) + "," + String.format("%.2f", seconds)
                    + "," + node + "," + extra + ","+ var +"," + val + extra + "\n");

            br.close();
            fr.close();

        }

    }

}
