import java.util.ArrayList;

public class CSP {
    private int[][] domain;
    private ArrayList<Constraint> consList;
    public CSP(int[][] domain, ArrayList<Constraint>consList){
        this.domain=domain;
        this.consList=consList;
    }
}
