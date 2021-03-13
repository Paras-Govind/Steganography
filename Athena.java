import java.net.*;
import java.io.*;

public class Athena {
    public static void main(String[] args) throws Exception {

        Socket s=new Socket("192.168.0.13", 6666);  
        System.out.println("Created socket");
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
        dout.writeUTF("ben, spying");  
        dout.flush();  
        dout.close();  
        s.close();  
    }
}
