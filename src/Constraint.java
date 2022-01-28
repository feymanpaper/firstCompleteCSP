import java.util.ArrayList;

public class Constraint {
    private int first;
    private int second;
    private ArrayList<Tuple> tuples ;
    public Constraint(int first, int second, ArrayList<Tuple>tuples){
        this.first=first;
        this.second=second;
        this.tuples=tuples;
    }
}
