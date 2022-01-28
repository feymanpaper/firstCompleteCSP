import java.util.ArrayList;

public class CSP {
    private int[][] domain;
    private ArrayList<Constraint> consList;
    public CSP(int[][] domain, ArrayList<Constraint>consList){
        this.domain=domain;
        this.consList=consList;
    }
    public int[][] getDomain() {
        return domain;
    }

    public ArrayList<Constraint> getConsList() {
        return consList;
    }
//    public String toString() {
//        StringBuffer result = new StringBuffer() ;
//        result.append("CSP:\n") ;
//        for (int i = 0; i < domain.length; i++)
//            result.append("Var "+i+": "+domain[i][0]+" .. "+domain[i][domain[0].length-1]+"\n") ;
//        for (Constraint bc : consList)
//            result.append(bc+"\n") ;
//        return result.toString() ;
//    }
}
