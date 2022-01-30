package impl;
/**
 * Assumes tuple values are integers
 */
public final class BinaryTuple implements Comparable {
  private int val1, val2 ;
  private boolean first = true;
  
  public BinaryTuple(int v1, int v2) {
    val1 = v1 ;
    val2 = v2 ;
  }
  
  public String toString() {
    return "<"+val1+", "+val2+">" ;
  }

  public void setFirst(boolean first){ this.first = first; }
  public boolean getFirst(){ return first; }

  /**
   * Checks if two variables have a constraint for theme.
   * @param v1
   * @param v2
   * @return if it exist s
   */
  public boolean matches(int v1, int v2, boolean first) {
    if(first) return (val1 == v1) && (val2 == v2) ;
    else return (val1 == v2) & (val2 == v1);
  }

  /**
   * check if both variables exist in any order
   * @param v1 first var
   * @param v2 second var
   * @return if true
   */
  public boolean both(int v1, int v2){
    return ((val1 == v1) && (val2 == v2)) || ((val1 == v2) && (val2 == v1));
  }

  /**
   * check if the variable given is the start of the arc
   * as indicated by first
   * @param v1 variable
   * @param first T for reading L->R, F for reading R -> L
   *              of the constraint
   * @return if true
   */
  public boolean has(int v1, boolean first){
    if(first) return (val1==v1);
    else return ((val2)==v1);
  }

  @Override
  public boolean equals(Object obj) {
    BinaryTuple bt = (BinaryTuple) obj;
    return (val1 == bt.val1) && (val2 == bt.val2);
  }

  public int getVal1(){ return val1; }
  public int getVal2(){ return val2; }

  @Override
  public int compareTo(Object o) {
    impl.BinaryTuple bt = (BinaryTuple) o;
    if(equals(bt)) return 0;
    return -1;
  }
}
