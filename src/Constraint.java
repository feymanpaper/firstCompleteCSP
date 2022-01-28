import com.sun.corba.se.spi.activation.ActivatorOperations;

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
    public void Add(ArrayList<Tuple> arrT){
        for(Tuple tuple:arrT){
            this.tuples.add(tuple);
        }
    }
    public ArrayList<Tuple> Remove(int ele, boolean dir){
        ArrayList<Tuple> ToDeleteTuple=new ArrayList<>();
        ArrayList<Tuple> AfterDeleteTuple= (ArrayList<Tuple>) this.tuples.clone();
        for(Tuple t:this.tuples){
            if(dir==true){
                if(t.getFirst()==ele){
                    ToDeleteTuple.add(t);
                    AfterDeleteTuple.remove(t);
                }
            }else if(dir==false){
                if(t.getSecond()==ele){
                    ToDeleteTuple.add(t);
                    AfterDeleteTuple.remove(t);
                }
            }
        }
        this.tuples=AfterDeleteTuple;
        return ToDeleteTuple;
    }
    public String toString() {
        StringBuffer result = new StringBuffer() ;
        result.append("c("+first+", "+second+")\n") ;
        for (Tuple bt : tuples)
            result.append(bt+"\n") ;
        return result.toString() ;
    }
    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }
}
