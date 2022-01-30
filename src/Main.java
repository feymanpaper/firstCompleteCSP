import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.css.CssMetaData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
       IOHandler io = new IOHandler();
       BinaryCSP csp=io.getCSP();
       Solver solver=new Solver(csp,Heuristics.SDF, Heuristics.ASCEND);
        boolean found = solver.solve(true);
        if(found) System.out.println("FOUND");
        else System.out.println("SOLUTION");
        solver.printSol( found, false, "", "sdf#asc");
        solver.reset();
       System.out.println("ask");
    }
}
