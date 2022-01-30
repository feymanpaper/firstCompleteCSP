import impl.BinaryConstraint;
import impl.BinaryTuple;
import impl.ListStack;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;
public class Solver{
    private static final int EMPTY = -1;
    private static final int EXIT = 2;
    private BinaryCSP csp;
    private HashMap<Integer, Integer> variables;
    private int[] assigned;
    private int[][] domains;
    private boolean fail = false;
    private ArrayList<BinaryConstraint> constraints ;
    private HashMap<Integer, List<Integer>> connections;
    private int num;
    private int last = -1;
    BinaryTuple lastTry;
    // added to act as a stack
    private ListStack<Map<BinaryTuple, BinaryTuple[]>> stack = new ListStack<>();
    private Control control;
    private Counter counter;

    public Solver(BinaryCSP csp, Heuristics type, Heuristics selType){
        this.csp = csp;
        constraints = csp.getConstraints();
        //TODO : What does this mean?
        variables = csp.getVariables();
        num = variables.size();
        writeDomain();
        assigned = new int[num];
        for(int i = 0; i < num; i++){ assigned[i] = EMPTY; }
        setConnections();
        if(type.getVal() < EMPTY) control = new Control(type, selType,true);
        else control = new Control(type, selType,false);
    }

    /**
     * main method to be called to run the solver
     * @param type T for FC, F for MAC algorithm to use
     * @return boolean denoting whether solution exists.
     */
    public boolean solve(boolean type){
        counter = new Counter(type);
        if(type) doForwardCheck();
        if (checkIfFound()) return true;
        else{
            counter.setEnd();
            return false;
        }
    }

    /**
     * checks if the solution is found having
     * the method has returned by verifying the
     * values filled in the assignment
     * @return boolean indicator
     */
    private boolean checkIfFound(){
        return completeAssignment();
    }


    private boolean getFail(){ return fail; }
    private void setFail(boolean fail){ this.fail = fail; }

    ArrayList<BinaryConstraint> getConstraints(){ return constraints; }
    List<Integer> getConnectionVar(int var){ return connections.get(var); }


    /**
     * write domain using the domain bounds, creates ragged array
     */
    private void writeDomain(){
        domains = new int[num][];
        int[][] domainBounds = csp.getDomainBounds();
        for(int i = 0; i < num; i++) {
            int length = domainBounds[i][1] - domainBounds[i][0] + 1;
            domains[i] = new int[length];
            int low = domainBounds[i][0];
            for (int j = 0; j < length; j++) {
                domains[i][j] = low++;
            }
        }
    }

    /**
     * resets to start with the same csp problem
     *
     */
    public void reset(){
        writeDomain();
        assigned = new int[num];
        for(int i = 0; i < num; i++){ assigned[i] = EMPTY; }
        constraints = csp.getConstraints();
    }


    /**
     * reinitialise the solver with the new csp problem
     * @param csp new csp problem to solve
     */
    void setNew(BinaryCSP csp){
        this.csp = csp;
        constraints = csp.getConstraints();
        variables = csp.getVariables();
        reset();
    }

    protected HashMap<Integer, Integer> getVariables(){ return variables; }

    /**
     * returns index of binary constraint interested located in constraint list
     * @param v1 first variable
     * @param v2 second variable
     * @return index of it located/ -1 if invalid (doesn't exists)
     */
    private int getConstraint(int v1, int v2){
        int i = 0;
        for(BinaryConstraint bc: constraints){
            if(bc.checkVars(v1,v2)) return i;
            i++;
        }
        return EMPTY;
    }

    /**
     * initialises variable list for the first time according to the heuristics
     * @return variable list
     */
    private List<Integer> getVarList(){
        List<Integer> varList = new ArrayList<>(getVariables().keySet());
        switch (control.type){
            case MAXDEG:
                varList = control.orderByDeg(variables, connections);
                break;
            case MAXCAR:
                varList = control.orderByCard(variables, connections);
                break;
            default:
                // for all other dynamic ordering, initialise in sdf ordering first
                varList = sortVarList(varList);
                break;
        }
        return varList;
    }


    /**
     * returns selected variable according to the dynamic variable selection for intermediate
     * variable selection calls
     * @param varList current list of unassigned variables
     * @return next variable chosen
     */
    private int selectVar(List<Integer> varList){
        int nextVar;
        switch (control.type){
            case SDF:
                varList = sortVarList(varList);
                nextVar = varList.get(0);
                break;
            case BRELAZ:
                nextVar = control.brelaz(varList, domains, variables, connections);
                break;
            case DOMDEG:
                nextVar = control.domDeg(varList, domains, variables, connections);
                break;
            default:
                // for all others return the first element
                nextVar = varList.get(0);
                break;
        }
        return nextVar;
    }


    /**
     * sorts varList in smallest domain first order (counts domain size and order in variables
     * with smallest domain first) to be called before FC selectVar
     * @param varList a list of unassigned variables
     * @return sorted varList
     */
    private List<Integer> sortVarList(List<Integer> varList){
        //System.out.println("-------Sort Var List---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int var = varList.get(i);

            int[] varD = domains[variables.get(var)];//domains[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > EMPTY) times++;
            }
            varCounts.put(var, times);
        }
        // now let's sort the map in ascending order of value
        HashMap<Integer, Integer> sorted = varCounts
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        List<Integer> sortedList = new ArrayList<>(sorted.keySet());
        return sortedList;
    }

    /**
     * selects value from the variable domain to choose to assign,
     * chosen according to ascending assignment ordering, find the first non-negative value in
     * domains array
     * @param var variable interested
     * @return value to assign
     */
    private int selectVal(List<Integer> varList, int var){
        int val = -1;
        if(control.selType == Heuristics.MINCONF) {
            val = minConflicts(varList, var);
        }
        else {
            int[] domain = domains[variables.get(var)];//domains[var];
            Arrays.sort(domain);
            // assuming that it is already sorted in -1, -1, ... some values order
            int check = EMPTY;
            if (lastTry != null && lastTry.getVal1() == var) check = lastTry.getVal2();
            for (int i = 0; i < domain.length; i++) {
                if (domain[i] > EMPTY && domain[i] != check) {
                    val = domain[i];
                    break;
                }
            }
        }
        return val;
    }

    /**
     * removes value from domain of variable var
     * @param var variable of interest
     * @param val value to remove from the domain of var
     * @return the index at which the value was located in domains array of var
     */
    private int removeVal(int var, int val){
        int ind = -1;
        int[] dom = domains[variables.get(var)];
        // delete val from domain
        for(int i = 0; i < dom.length; i++){
            if(dom[i] == val){
                dom[i] = EMPTY;
                ind = i;
                break;
            }
        }
        try {
            if(ind < 0) throw new ArrayIndexOutOfBoundsException("No such value in domain");
        }
        catch (ArrayIndexOutOfBoundsException e){ e.getMessage(); }
        return ind;

    }





    /**
     * assign value to the var and remove all other values from its domain
     * @param var variable to assign value to
     * @param val value to assign
     * @return array of removed values
     */
    private int[] assign(int var, int val){
        int valid = (int) Arrays.stream(domains[variables.get(var)]).filter(e -> e > EMPTY).count();
        int[] removed = new int[valid-1];
        int j = 0;
        int varIndex = variables.get(var);
        int[] dom = domains[varIndex];
        // by assignment, remove all other values from the domain array by EMPTY
        // and save these to new ``removed'' array to pass back
        for(int i = 0; i < domains[varIndex].length; i++){
            if(dom[i] != EMPTY && dom[i] != val){
                removed[j++] = dom[i];
                dom[i] = EMPTY;
            }
        }
        return removed;
    }

    /**
     * recover values removed previously to var's domain
     * @param var variable considered
     * @param removed array of values previously removed
     * @return boolean ensure all values removed are restored
     */
    private boolean unassign(int var, int[] removed){
        int varIndex = variables.get(var);
        int[] dom = domains[varIndex];
        int j = 0;
        for(int i = 0; i < domains[varIndex].length; i++){
            if(dom[i] < 0 && j < removed.length) dom[i] = removed[j++];
        }
        return (j == removed.length);
    }

    /**
     * checks if domain is empty
     *
     * @return boolean for T/F
     */
    private boolean isEmptyDomain(int[] domain){
        return Arrays.stream(domain).allMatch(i -> i < 0);
    }

    /**
     * checks if all values are assigned
     *
     * @return all not empty T/F
     */
    private boolean completeAssignment() {
        return Arrays.stream(assigned).allMatch(i -> i > EMPTY);
    }


    /**
     * prints out solution according to the format for testing
     * @param printType T for bulk print of statistics only, in csv, F for print out in txt including solutions
     * @param fn output file path, provided if called from test, empty if called separately
     * @param heuristics type of heuristics used to be included in the output (var#val)
     * @throws IOException exception thrown in the error of invalid filepath
     */
    public void printSol(boolean exists, boolean printType, String fn, String heuristics) throws IOException {
        String wd = System.getProperty("user.dir");
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd_HH-mm");
        Date date = new Date();
        String time = formatter.format(date);
        Path path = Paths.get(wd, "runner/src/test/output");
        if(printType && fn.equals("")){
            File file = new File(path.toString()+ '/' + csp.getName() + "_" +time + "_Solver_out.csv");
            FileWriter fr = new FileWriter(file);
            fr.write("Filename,Type,Time1,Time2,NodeCount1,NodeCount2,VarOrder,ValOrder\n");
            fr.close();
            fn = file.getCanonicalPath();
        }
        if(!printType && fn.equals("")){
            // single output
            fn = wd + "/src/output/" + csp.getName() + "_" + time + "_out.txt";
        }
        counter.printStats(exists, csp.getName(), printType, fn, heuristics);
        if(!printType) {
            File file = new File(fn);
            FileWriter fr = new FileWriter(file, true);
            if(exists) {
                fr.write("Solution:\n");
                int i = 0;
                for (Integer v : variables.keySet()) {
                    fr.write("Var " + v + ", " + assigned[i++] + "\n");
                }
            }
            else{
                fr.write("No valid assignment");
            }
            fr.close();
        }
    }

    /**
     * wrapper to push provided map to the liststack
     * @param map map of <constraint (var1,var), list of pruned value pairs> from revise
     */
    private void push(Map<BinaryTuple, BinaryTuple[]> map){
        stack.push(map);
    }


    /**
     * get the constraint from the constraints list given its index
     * @param index index where constraint is located
     * @return BinaryConstraint
     */
    private BinaryConstraint getConstraintIndex(int index){
        return constraints.get(index);
    }

    /**
     * wrapper to get single array of currently domain values
     * for given variable
     * @param var current variable considered
     * @return integer array of domain values
     */
    private int[] getVarDomain(int var){
        return domains[variables.get(var)];
        //return domains[var];
    }

    /**
     * wrapper to assign value to the variable,
     * on the assigned array, calls assign and get
     * all the removed values from the variable domain
     * due to assignment
     * @param var variable
     * @param val value
     * @return array of removed values
     */
    private int[] assignVal(int var, int val){
        assigned[variables.get(var)] = val;
        int[] removed = assign(var, val);
        return removed;
    }

    /**
     * unassign value to the variable from assigned array
     * sets lastTry to get this assignment pair
     * @param var variable to unassign from
     */
    private void unassignVal(int var){
        int v = assigned[variables.get(var)];
        lastTry = new BinaryTuple(var, v);
        assigned[variables.get(var)] = EMPTY;

    }

    /**
     * to be run at the start, log for each variable the list of
     * variable to which it is connected
     */
    private void setConnections(){
        //TODO: what do this mean?
        HashMap<Integer, List<Integer>> connectCounts = new HashMap<>();
        for(BinaryConstraint bt : constraints){
            int v1 = bt.getFirstVar();
            int v2 = bt.getSecondVar();
            List<Integer> list = new ArrayList<>() ;
            if (connectCounts.containsKey(v1)){
                list = connectCounts.get(v1);
                list.add(v2);
            }
            else list.add(v2);
            connectCounts.put(v1, list);
            List<Integer> list2 = new ArrayList<>() ;
            if (connectCounts.containsKey(v2)) {
                list2 = connectCounts.get(v2);
                list2.add(v1);
            }
            else list2.add(v1);
            connectCounts.put(v2, list2);
        }
        connections = connectCounts;

    }

    /**
     * value heuristics, find next variable according to min-conflicts method
     * @param varList current variable list
     * @param var current variable selected
     * @return value to assign
     */
    private int minConflicts(List<Integer> varList, int var){
        int[] curVarDom = domains[variables.get(var)];
        // get all variables var is connected to
        List<Integer> cons = connections.get(var);
        // <val of domain, no of incomp val in future vars' dom>
        HashMap<Integer, Integer> valCount = new HashMap<>();
        for(int val: curVarDom){
            if(val == EMPTY) continue;
            int notCom = 0;
            for(int futureVar : cons){
                // ignore any variable that is already assigned
                if(varList.indexOf(futureVar) == EMPTY) continue;
                BinaryConstraint bc = getConstraintIndex(getConstraint(var, futureVar));
                boolean first = true;
                if(bc.getSecondVar() == var) first = false;
                int[] otherDom = domains[variables.get(futureVar)];
                for(int j = 0; j < otherDom.length; j++){
                    // count any incompatible matches
                    if(!bc.checkMatch(val, otherDom[j], first)) notCom++;
                }

            }
            valCount.put(val, notCom);

        }
        // sort in ascending order
        valCount = sort(valCount, true);
        return valCount.keySet().iterator().next();
    }

    /**
     * a short helper function for sorting
     * @param counts map to sort
     * @param order T for ascending and F for descending order
     * @return sorted map
     */
    public HashMap<Integer, Integer> sort(HashMap<Integer, Integer> counts, boolean order){
        HashMap<Integer, Integer> sorted;
        // ascending order
        if(order){
            // sort by ascending order of values
            sorted = counts
                    .entrySet()
                    .stream()
                    .sorted(comparingByValue())
                    .collect(
                            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));
        }
        else{
            // sort by decreasing order of values
            sorted = counts
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(
                            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));
        }
        return sorted;

    }



    // FC ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * get variable list, set the clock and run FC
     */
    public void doForwardCheck(){
        List<Integer> varList = getVarList();
        counter.setStart();
        FC(varList);
    }

    /**
     * implementation of forward checking
     * @param varList a list of unassigned variables
     * @return integer to denote status, return EXIT (2) if soln is found
     */
    private int FC(List<Integer> varList){
        counter.increment(false);
        counter.increment(true);
        // all positive so all variables are assigned
        if(completeAssignment()){
            counter.setEnd();
            return EXIT;
        }
        if(last == EMPTY) last = varList.get(0);
        int var = selectVar(varList);
        last = var;
        int val = selectVal(varList, var);
        List<Integer> copVarList = (List<Integer>) ((ArrayList<Integer>) varList).clone();
        if(branchFCLeft(varList, var, val) == EXIT) return EXIT ;
        if(branchFCRight(copVarList, var, val) == EXIT) return EXIT ;
        return 0;
    }

    /**
     * implementation of left branch in Forward Checking
     * @param varList a list of unassigned variables
     * @param var variable chosen to use
     * @param val value to be assigned to that variable
     * @return return value for status, EXIT to finish
     */
    private int branchFCLeft(List<Integer> varList, int var, int val){
        counter.increment(false);
        int[] removed = assignVal(var, val);
        if(reviseFA(varList, var)){
            varList.remove(Integer.valueOf(var));
            last = -1;
            if(FC(varList) == EXIT){
                return EXIT;
            }
        }
        undoPruning();
        // un-assign
        if(!unassign(var, removed)) System.out.println("ERROR");
        unassignVal( var);
        varList.add(var);
        sortVarList(varList);
        return 0;
    }

    /**
     * implementation of right branch in Forward Checking
     * delete by removing from domain bounds, to be restored later
     * @param varList a list of unassigned variables (includes var which is in use in left branch)
     * @param var variable name
     * @param val value assigned to that variable
     * @return return value for status, EXIT to finish
     */
    private int branchFCRight(List<Integer> varList, int var, int val){
        counter.increment(false);
        removeVal(var, val);
        String s = "";
        int[] doms = getVarDomain(var);
        // checks if domain is empty (if sum of all values = -(length)
        if(Arrays.stream(doms).sum() > (EMPTY)*(doms.length)){
            if(reviseFA(varList, var)){
                if(FC(varList) == EXIT) return EXIT;
            }
            undoPruning();
        }
        //restore
        for(int i = 0; i < doms.length; i++){
            if(doms[i] < 0){
                doms[i] = val;
                break;
            }
        }
        return 0;
    }
    /**
     * revise domains of x_i and does pruning!
     * @param type T for FC and F for MAC (since MAC requires indicator
     *             of whether the domain has been emptied to be passed to AC3 method call
     * @param bt binary constraint of interest
     * @param var1 x_i
     * @param var2 x_j
     * @return boolean denoting if the domain of x_i has changed
     */
    private boolean revise(boolean type, BinaryConstraint bt, int var1, int var2) {
        int[] d1 = domains[variables.get(var1)];
        int[] d2 = domains[variables.get(var2)];
        boolean first;
        // boolean to denote the direction
        if (bt.getFirstVar() != var1) first = false;
        else first = true;
        boolean changed = false;
        int ind = -1;
        for(int i : d1){
            ind++;
            if(i < 0) continue;
            boolean supported = false;
            int j = -1;
            while(!supported && j < d2.length - 1){
                j++;
                if(d2[j] < 0) continue;
                // check the value pair is consistent
                if(bt.checkMatch(i, d2[j], first)){
                    supported = true;
                }
            }
            if(!supported){
                // remove all the tuples from the constraints list
                // to push to stack

                //rms记录被删除的BinaryTuple[]
                List<BinaryTuple> rms = bt.removeTuple(i, first);
                BinaryTuple[] rmsArray;
                if(rms.size() > 0 ) {
                    rmsArray = rms.toArray(new BinaryTuple[0]);
                    rms.toArray(rmsArray);
                }
                else {
                    // case where there are no removed tuples
                    // pass dummy and check later
                    rmsArray = new BinaryTuple[1];
                    rmsArray[0] = new BinaryTuple(i, -1);
                }

                Map<BinaryTuple, BinaryTuple[]> map = stack.pop();
                BinaryTuple copyRightOrder = bt.getVars();
                if(!first) copyRightOrder.setFirst(false);
                //Map记录被删除的Map<BinaryTuple, BinaryTuple[]>
                map.put(copyRightOrder, rmsArray);
                stack.push(map);
                // remove val
                d1[ind] = EMPTY;

                changed = true;
            }
        }
        if(isEmptyDomain(d1)) {
            // domain is empty set fail flag and return immediately
            if(!type) fail = true;
            return false;
        }
        if(!type) return changed;
        return true;
    }
    /**
     * arc revision with all future variables
     * @param varList varlist is smf ordered list of variables containing var
     * @param var variable selected to work on
     * @return boolean true if the future assignment makes it consistent/ else false mean unpruning should be done
     */
    private boolean reviseFA(List<Integer> varList, int var){
        boolean consistent = true;
        Map<BinaryTuple, BinaryTuple[]> map = new HashMap<>();
        //TODO : why
        push(map);
        //stack.push(map);
        // assuming that varList is already order with smallest domain first!
        for(Integer futureVar: varList){
            if(futureVar.equals(var)) continue;
            int index = getConstraint(futureVar, var);
            if(index < 0) continue; // constraint doesn't exists so no need to revise
            consistent = revise(true, getConstraintIndex(index), futureVar, var);//, val);
            if(!consistent) return false;
        }
        return true;
    }
    /**
     * reverses the operation of reviseFA by popping saved to restore
     * previous binary tuples for each binary constraint and domain values for
     * futureVar
     */
    private void undoPruning(){
        Map<BinaryTuple, BinaryTuple[]> pruned = stack.pop();
        for (Map.Entry<BinaryTuple, BinaryTuple[]> pair : pruned.entrySet()) {
            for(BinaryConstraint bc: constraints){
                if(pair.getKey().both(bc.getFirstVar(), bc.getSecondVar())){
                    bc.addTuples(pair.getValue());
                }
            }
            int futureVar;
            int val;
            // from L to R get the first var
            // for the case where the getValue does not have removed tuples in the first place
            if(pair.getValue().length == 1 && pair.getValue()[0].getVal2() < 0){
                if(!pair.getKey().getFirst()) futureVar = pair.getKey().getVal2();
                else futureVar = pair.getKey().getVal1();
                val = pair.getValue()[0].getVal1();
            }
            else {
                futureVar = pair.getKey().getVal1();
                val = pair.getValue()[0].getVal1();

                // from R to L get the second var
                if (!pair.getKey().getFirst()) {
                    //System.out.println("opposite");
                    futureVar = pair.getKey().getVal2();
                    val = pair.getValue()[0].getVal2();
                }
            }
//            System.out.println("var to recover is " + futureVar);
//            System.out.println("value to recover is " + val);
            int varIndex = variables.get(futureVar);
            for(int i = 0; i < domains[varIndex].length; i++){
                if(domains[varIndex][i] < 0){
                    domains[varIndex][i] = val;
                    //sort?, will put all -1s in the front

                    break;
                }
            }
//            for(int i = 0; i < domains[futureVar].length; i++){
//                if(domains[futureVar][i] < 0){
//                    domains[futureVar][i] = val;
//                    //sort?, will put all -1s in the front
//
//                    break;
//                }
//            }
            Arrays.sort(domains[futureVar]);
        }

    }









}