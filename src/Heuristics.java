/**
 * encoded heuristics to be used as enum
 */
public enum Heuristics {
    MAXDEG(-3), MAXCAR(-1), SDF(3), BRELAZ(4), DOMDEG(5), ASCEND(6), MINCONF(7);
    private final int val;
    Heuristics(int val){
        this.val = val;
    }
    public int getVal(){ return val;}
}


