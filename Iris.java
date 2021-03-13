import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.security.MessageDigest;

public class Iris {

    private static final String DATABASE_NAME = "Users.db";
    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(6666);
        Socket s = ss.accept();
        DataInputStream dis=new DataInputStream(s.getInputStream());  
        String  str = (String)dis.readUTF();  
        

        ss.close();
    }

    private static void logIn(String username, String password) throws Exception {

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);

        PreparedStatement userStatement = conn.prepareStatement("SELECT passHash FROM users WHERE username = ?");
        userStatement.setString(1, username);

        ResultSet userPasswords = userStatement.executeQuery();

        String hashedPassword = hashPass(password);

        int noOfPasswords = 0;
        String retrievedPassword = null;

        while (userPasswords.next()) {

            retrievedPassword = userPasswords.getString(1);
            noOfPasswords++;
        }

        if (noOfPasswords == 0) {

            System.out.println("Username doesn't exist");
        }
        else if (retrievedPassword.equals(hashedPassword)) {

            System.out.println("Successful log in");
        }
        else {
            System.out.println("Incorrect Password");
        }
    }

    private static String hashPass(String password) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger number  = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}