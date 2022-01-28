import java.util.*;

public class CSPSolver {
    public CSP csp;
    public int[] assginArr;
    public Stack<Map<Tuple, ArrayList<Tuple>>> PruneStack = new Stack<>();
    public CSPSolver(CSP csp){
        this.csp=csp;
    }
    public void ForwardChecking(List<Integer> varList){
        if(completeAssignment()){
            printSolution();
            return ;
        }
        int var=selectVar(varList);
        int val=selectVal(domain(var));
        branchFCLeft(varList, var, val);
        branchFCRight(varList, var, val);
    }

    private void branchFCLeft(List<Integer> varList, int var, int val) {
        assign(var, val);
        if(reviseFutureArcs(varList, var)){
            varList.remove(var);
            ForwardChecking(varList);
        }
        undoPruning();
        unassignVal(var);
        varList.add(var);
        Collections.sort(varList);
    }

    private void unassignVal(int var) {
        assginArr[var]=-1;
    }


    private void branchFCRight(List<Integer> varList, int var, int val) {
        deleteValue(var, val);
        if(!true){
            if(reviseFutureArcs(varList,var)){
                ForwardChecking(varList);
            }
            undoPruning();
        }
        restoreValue(var, val);
    }

    private void deleteValue(int var, int val) {
    }

    private int selectVar(List<Integer> varList) {
        return varList.get(0);
    }
    private int selectVal(int[] domainVar) {
        //TODO
        Arrays.sort(domainVar);
        return 1;
    }

    private void printSolution() {
    }

    private boolean completeAssignment() {
        //TODO
        return false;
    }
    private void unassign(int var, int val) {
    }

    private void undoPruning() {
        Map<Tuple,ArrayList<Tuple>> prunMap=PruneStack.peek();
        PruneStack.pop();
        for (Map.Entry<Tuple, ArrayList<Tuple>> entry : prunMap.entrySet()) {
            for(Constraint con: csp.getConsList()){
                if(entry.getKey().getFirst()==con.getFirst()&&entry.getKey().getSecond()==con.getSecond()){
                    con.Add(entry.getValue());
                }else if(entry.getKey().getSecond()==con.getFirst()&&entry.getKey().getSecond()==con.getFirst()){
                    con.Add(entry.getValue());
                }
            }
        }


    }

    private boolean reviseFutureArcs(List<Integer> varList, int var) {
        boolean consistent=true;
        for(int futureVar:varList){
            if(futureVar==var) continue;
            consistent=Revise(futureVar,var);
            if(!consistent) return false;
        }
        return true;
    }

    private boolean Revise(int futureVar, int var) {
        //GetIndex
        int idx=-1;
        for(int i=0;i<csp.getConsList().size();i++){
            Constraint con=csp.getConsList().get(i);
            if(con.getFirst()==var&&con.getSecond()==futureVar){
                idx=i;
                break;
            }
            if(con.getFirst()==futureVar&&con.getSecond()==var){
                idx=i;
                break;
            }
        }
        assert idx!=-1:"idx!=-1";
        Constraint con =csp.getConsList().get(idx);
        //小的在前面
        boolean dir=true;
        if(var<futureVar){
            dir=false;
        }else{
            dir=true;
        }
        int[] nowDomain=csp.getDomain()[var];
        int[] futureDomain=csp.getDomain()[futureVar];
        int deleteIdx=0;
        for(int future:futureDomain){
            if(future<0) continue;
            boolean support=false;
            for(int now:nowDomain){
                if(now<0) continue;
                for(Tuple t:con.getTuples()){
                    if(dir==false){
                        if(t.getFirst()==now&&t.getSecond()==future){
                            support=true;
                            break;
                        }
                    }else if(dir==true){
                        if(t.getFirst()==future&&t.getSecond()==now){
                            support=true;
                            break;
                        }
                    }
                }
                if(support==false){
                    ArrayList<Tuple> DeleteTuple = con.Remove(future, dir);
                    Map<Tuple, ArrayList<Tuple>> map = PruneStack.peek();
                    PruneStack.pop();
                    Tuple RestoreTuple=new Tuple(con.getFirst(),con.getSecond());
                    map.put(RestoreTuple, DeleteTuple);
                    PruneStack.push(map);
                    futureDomain[deleteIdx] = -1;
                }
            }
            deleteIdx++;
        }
        boolean empty=true;
        for(int i=0;i<futureDomain.length;i++){
            if(futureDomain[i]!=-1){
                empty=false;
                break;
            }
        }
        if(empty==true) return false;
        else return true;
    }

    private void assign(int var, int val) {
        assginArr[var]=val;
    }
    private void restoreValue(int var, int val) {
    }
    private int[] domain(int var){
        return csp.getDomain()[var];
    }

}
