import java.net.*;
import java.io.*;

public class Athena {
    public static void main(String[] args) throws Exception {

        Socket s=new Socket("localhost", 6666);  
        System.out.println("Created socket");
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
        dout.writeUTF("0,ben,spying");
        dout.flush();  
        dout.close();  
        s.close();  
    }
}
