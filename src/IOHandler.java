import impl.BinaryConstraint;
import impl.BinaryTuple;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class IOHandler {
    public BinaryCSP getCSP(){
        Scanner sc=new Scanner(new BufferedInputStream(System.in));
        System.out.print("Please input the number of Queens: ");
        int n=sc.nextInt();
        System.out.println("Number of the variables: "+n) ;
        System.out.println("Domains of the variables: ") ;
        int[][] domain=new int[n][2];
        for(int i=0;i<n;i++){
            domain[i][0]=0;
            domain[i][1]=n-1;
        }
//        int [][] domain=new int[n][n];
//        for(int i=0;i<n;i++){
//            System.out.println("x" + i + ": ["+ 0 + "," + (n-1)+"]");
//            for(int j=0;j<n;j++){
//                domain[i][j]=j;
//            }
//        }
        int first,second;
        System.out.println("Constraints of the variables: ");
        ArrayList<BinaryConstraint> conList=new ArrayList<>();
        for (int var1 = 0; var1 < n-1; var1++){
            for (int var2 = var1+1; var2 < n; var2++) {
                System.out.println("c(x"+var1+", "+ "x"+var2+")") ;
                first=var1;
                second=var2;
                ArrayList<BinaryTuple> arr=new ArrayList<>();
                for (int val1 = 0; val1 < n; val1++){
                    for (int val2 = 0; val2 < n; val2++) {
                        if(checkPos(var1,var2,val1,val2)){
                            System.out.println("(" + val1+", "+val2 + ")") ;
                            arr.add(new BinaryTuple(val1,val2));
                        }
                    }
                }
                System.out.println() ;
                BinaryConstraint c=new BinaryConstraint(first,second,arr);
                conList.add(c);
            }
        }
        sc.close();
        BinaryCSP csp=new BinaryCSP("Nquenns",domain,conList);
        return csp;
    }
    public boolean checkPos(int var1,int var2,int val1,int val2){
        if ((val1 != val2) && (Math.abs(val1 - val2) != (var2-var1))) {
            return true;
        }else return false;
    }
}
