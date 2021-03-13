import java.io.*;
import java.net.*;

public class Iris {
    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(6666);

        while (true) {

            Socket s = ss.accept();
            DataInputStream dis = new DataInputStream(s.getInputStream());  
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            Clotho newThread = new Clotho(s, dis, dos);

            newThread.run();
        }
    }
}