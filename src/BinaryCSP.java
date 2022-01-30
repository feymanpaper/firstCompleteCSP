import impl.BinaryConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public final class BinaryCSP {
  private String name;
  private int[][] domainBounds ;
  private ArrayList<BinaryConstraint> constraints ;
  // name文件名 , db domain域 , 限制条件
  BinaryCSP(String name, int[][] db, ArrayList<BinaryConstraint> c) {
    this.name = name;
    domainBounds = db ;
    constraints = c ;
  }
  
  public String toString() {
    StringBuffer result = new StringBuffer() ;
    result.append("CSP:\n") ;
    for (int i = 0; i < domainBounds.length; i++)
      result.append("Var "+i+": "+domainBounds[i][0]+" .. "+domainBounds[i][1]+"\n") ;
    for (BinaryConstraint bc : constraints)
      result.append(bc+"\n") ;
    return result.toString() ;
  }

  public String getName(){ return name; }
  
  public ArrayList<BinaryConstraint> getConstraints() {
    return constraints ;
  }

  int[][] getDomainBounds(){ return domainBounds; }

  /**
   * find notations used for variables and save the sorted hashmap with
   * variable value as key with its index as value
   * @return hashmap as saved
   */
  public HashMap<Integer, Integer> getVariables(){
    ArrayList<Integer> vars = new ArrayList<>();
    for(BinaryConstraint bc: constraints){
      int var1 = bc.getFirstVar();
      int var2 = bc.getSecondVar();
      if(!vars.contains(var1)) vars.add(var1);
      if(!vars.contains(var2)) vars.add(var2);
    }
    Collections.sort(vars);
    HashMap<Integer, Integer>  map = new HashMap<>();
    int[] sorted = vars.stream().mapToInt(i -> i).toArray();
    for(int i = 0; i < sorted.length; i++){
      map.put(sorted[i], i);
    }

    return map;
  }
}
