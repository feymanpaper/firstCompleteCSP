package impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BinaryConstraint {
  private int firstVar, secondVar ;
  private ArrayList<BinaryTuple> tuples ;
  
  public BinaryConstraint(int fv, int sv, ArrayList<BinaryTuple> t) {
    firstVar = fv ;
    secondVar = sv ;
    tuples = t ;
  }

  public int getFirstVar() {
    return firstVar;
  }

  public int getSecondVar(){ return secondVar; }

  public int getNTuples(){ return tuples.size(); }

  public String toString() {
    StringBuffer result = new StringBuffer() ;
    result.append("c("+firstVar+", "+secondVar+")\n") ;
    for (BinaryTuple bt : tuples)
      result.append(bt+"\n") ;
    return result.toString() ;
  }

  /**
   * check matching of variable names
   * (to be used to find desired binary constraint)
   * (t
   * @param v1 first variable to check with
   * @param v2 second variable to check with
   * @return boolean if matches
   */
  public boolean checkVars(int v1, int v2) {
      return (((firstVar == v1) && (secondVar == v2)) || ((firstVar == v2) && (secondVar == v1)));
  }

  /**
   * returns variables in this constraint as a binary tuple
   * @return binary tuple of VARIABLE NAMES (NOT VALUES)
   */
  public BinaryTuple getVars(){
    return new BinaryTuple(firstVar, secondVar);
  }

  /**
   * check if the given tuple exists in the domain
   * (to be used by REVISE method)
   * @param v1 (d_i)
   * @param v2 (d_j)
   * @param first direction indicator of an arc (if true match from l->r and false
   *              for the opposite
   * @return boolean
   */
  public boolean checkMatch(int v1, int v2, boolean first){
    boolean match = false;
    for (BinaryTuple bt: tuples){
      if(bt.matches(v1, v2, first)) {
          match = true;
          break;
        }
    }
    return match;
  }

  /**
   * removes any tuples containing the pruned domain value
   * @param v domain value to remove (first)
   * @return list of removed tuples
   */
  public List<BinaryTuple> removeTuple(int v, boolean first){
    List<BinaryTuple> rms = new ArrayList<>();
    ArrayList<BinaryTuple> copy = (ArrayList<BinaryTuple>) tuples.clone();
    for(int i = 0; i < tuples.size(); i++){
      if(tuples.get(i).has(v,first)){
        rms.add(tuples.get(i));
        copy.remove(tuples.get(i));
      }
    }
    tuples = copy;
    return rms;
  }

  /**
   * adds list of tuples to allowed binary tuples
   * @param tups tuples to add
   */
  public void addTuples(BinaryTuple[] tups){
    Collections.addAll(tuples, tups);
  }
}
