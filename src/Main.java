import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedInputStream;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
       IOHandler io = new IOHandler();
       CSP csp=io.getCSP();
    }
}
