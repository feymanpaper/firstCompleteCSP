import java.util.ArrayList;
import java.util.List;

public class Tuple {
    private int first;
    private int second;
    public Tuple(int first, int second) {
        this.first=first;
        this.second=second;
    }

    public String toString() {
        return "<"+first+", "+second+">" ;
    }
    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }
}
