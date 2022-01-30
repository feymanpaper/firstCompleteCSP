import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

/**
 * Control class including functions for finding static and
 * dynamic variable ordering heuristics
 */
public class Control {
    private static final int EMPTY = -1;
    Heuristics type;
    Heuristics selType;
    private boolean fixed;

    Control(Heuristics type, Heuristics selType, boolean fixed){
        this.type = type;
        this.selType = selType;
        this.fixed = fixed;
    }

    // order the variables by descending order of degree

    /**
     * static ordering - maximum degree
     * @param variables variables of the problem
     * @param connections map of variable with list of connected variables
     * @return list of static ordered variables
     */
    public List<Integer> orderByDeg(HashMap<Integer, Integer> variables, HashMap<Integer, List<Integer>> connections){
        List<Integer> varList = new ArrayList<>(variables.keySet());
        HashMap<Integer, Integer> varConstCounts = new HashMap<>();
        for(int v1: varList){
            int deg = connections.get(v1).size();
            varConstCounts.put(v1, deg);
        }
        // sort by decreasing order of values
        HashMap<Integer, Integer> sorted = varConstCounts
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        List<Integer> vars = new ArrayList<>(sorted.keySet());
        return vars;
    }

    /**
     * select the next variable to assign the value to by
     * maximum cardinality rule : from the current value, choose the variable
     * it's connected largest number with amongst already assigned
     * @param variables of the problem
     * @param connections map of variable with list of connected variables
     * @return static ordering list of variables
     */
    public List<Integer> orderByCard(HashMap<Integer, Integer> variables, HashMap<Integer, List<Integer>> connections){
        List<Integer> varList = new ArrayList<>(variables.keySet());
        List<Integer> ordered = new ArrayList<>();
        double randomDouble = Math.random() * (varList.size());
        int randomInt = (int) randomDouble;
        int var = varList.get(randomInt);
        ordered.add(var);
        while (ordered.size() < varList.size()){
            List<Integer> values = connections.get(var);
            int curN = -1;
            for(int v: values){
                if(ordered.indexOf(v) > EMPTY) continue;
                List<Integer> vals = connections.get(v);
                int N = 0;
                for(int v1: vals){
                    if(ordered.indexOf(v1) > EMPTY){
                        N++;
                    }
                }
                if(curN < 0 || N > curN){
                    curN = N;
                    var = v;
                }
            }
            ordered.add(var);
        }
        Set<Integer> s = new HashSet<>(ordered);
        if(ordered.size() != s.size()) System.out.println("LENGTHS DIFFER!!");
        return ordered;
    }

    /**
     * static ordering input reader to return the ordered list
     * @param fn file path to file
     * @return ordered list
     */
    public List<Integer> orderByInput(String fn){
        FileReader inFR;
        StreamTokenizer in;
        List<Integer> list = new ArrayList<>();
        try {
            inFR = new FileReader(fn) ;
            in = new StreamTokenizer(inFR) ;
            in.ordinaryChar('(') ;
            in.ordinaryChar(')') ;
            in.nextToken() ;                                         // n
            int n = (int)in.nval ;
            //int[] vars = new int[n];
            for (int i = 0; i < n; i++) {
                in.nextToken() ;                                  // ith ub
                list.add((int)in.nval);
                in.nextToken() ;                                   // ','
                in.nextToken() ;
                list.add((int)in.nval);
            }
            inFR.close() ;
        }
        catch (FileNotFoundException e) {System.out.println(e);}
        catch (IOException e) {System.out.println(e);}
        return list;

    }

    /**
     * brelaz implementation, pass the value of var of currently assigned
     * then select the variable with max degree in constraint sub-graph of future variables
     * First var should be selected by SDF! Use varList to check if the variables in bt are not assigned.
     * @param varList list of unassigned variables including var
     * @param domains domain values
     * @param variables of the problem
     * @param connections map of variable with list of connected variables
     * @return next var
     */
    public int brelaz(List<Integer> varList, int[][] domains,HashMap<Integer, Integer> variables,
                      HashMap<Integer, List<Integer>> connections){
        HashMap<Integer, Integer> map = sortByDomain(varList, domains, variables);
        int i = 0;
        List<Integer> two = new ArrayList<>();
        for(int v: map.keySet()){
            if(i == 2) break;
            two.add(v);
            i++;
        }
        if(two.size() >= 2 && two.get(0).equals(two.get(1))){
            int maxDeg = -1;
            int val = -1;

            for(int j = 0; j < 2; j++){
                int nDeg = 0;
                for(int cons : connections.get(two.get(j))){
                    if(varList.indexOf(cons) > EMPTY) nDeg++;
                }
                if (maxDeg == EMPTY || nDeg > maxDeg) {
                    maxDeg = nDeg;
                    val = varList.get(i);
                }
            }
            return val;
        }
        return two.get(0);
    }

    /**
     * dynamic ordering of variable by descending order of degree
     * @param varList currently unassigned variables (if includes current var, ignore)
     * @param connections map of variable with list of connected variables
     * @return sorted by degree with future variables in HashMap<Variable, Degree> pairs
     */
    public HashMap<Integer, Integer> sortByDegree(List<Integer> varList, HashMap<Integer, List<Integer>> connections){
        HashMap<Integer, Integer> degCounts = new HashMap<>();
        for (int v1 : varList) {
            int nDeg = 0;
            nDeg = connections.get(v1).size();
            degCounts.put(v1, nDeg);
        }
        // sort in descending order
        HashMap<Integer, Integer> sorted = degCounts
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        return sorted;

    }

    /**
     * sorts variable by the size of domain available for each (will be for variables which are nto assigned)
     * @param varList variables to be sorted, currently unassigned
     * @param domains current domain for all variables
     * @param variables list of variables with its index indicator for domain array
     * @return Hashmap of variable, number of available values count pair
     */
    protected HashMap<Integer, Integer> sortByDomain(List<Integer> varList, int[][] domains,
                                                     HashMap<Integer, Integer> variables){
        //System.out.println("-------Sort By Domain---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int v :varList){
            // number of positive values
            int times = 0;
            //if(v == var) continue;

            int[] varD = domains[variables.get(v)];//domains[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > EMPTY) times++;
            }
            varCounts.put(v, times);
        } // sort in ascending order of values
        HashMap<Integer, Integer> sorted = varCounts
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                        toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                LinkedHashMap::new));
        return sorted;//sortedVars;
    }

    /**
     * find the next value which gives the minimum quotient of dom size/degree
     * @param varList varlist with currently unassigned variables
     * @param domains current domains for all variables
     * @param variables of the problem
     * @param connections map of variable with list of connected variables
     * @return the next variable to assign the value to
     */
    public int domDeg(List<Integer> varList, int[][] domains, HashMap<Integer, Integer> variables,
                      HashMap<Integer, List<Integer>> connections){
        HashMap<Integer, Integer> dom = sortByDomain(varList, domains, variables);
        HashMap<Integer, Integer> deg = sortByDegree(varList, connections);
        double minRatio = -1;
        int minVar = -1;
        int times = 0;
        for(int vv : dom.keySet()){
            double ratio = dom.get(vv)/((1.0)*(deg.get(vv)));
            if(minRatio < 0 || minRatio > ratio){
                minRatio = ratio;
                minVar = vv;
            }
            times++;
            if (times == 2) break;
        }
        return minVar;
    }





}
