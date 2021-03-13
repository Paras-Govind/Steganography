import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.security.MessageDigest;

public class Iris {

    private static final String DATABASE_NAME = "Users.db";
    private static final String LOG_IN_FLAG = "0";
    private static final String CREATE_NEW_USER_FLAG = "1";

    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(6666);
        Socket s = ss.accept();
        DataInputStream dis=new DataInputStream(s.getInputStream());  
        String logInDetails = (String)dis.readUTF();  

        String[] detailsArray = logInDetails.split(",");

        if (detailsArray.length != 3) {
            System.out.println("Error with provided data");
        }
        else if (detailsArray[0].equals(LOG_IN_FLAG)) {
            logIn(detailsArray[1], detailsArray[2]);
        }
        else if (detailsArray[0].equals(CREATE_NEW_USER_FLAG)) {
            createUser(detailsArray[1], detailsArray[2]);
        }
        else {
            System.out.println("There was an error with the provided data's structure.");
        }
        
        
        ss.close();
    }

    private static void createUser(String username, String password) throws Exception {

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);

        PreparedStatement checkUserStatement = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        checkUserStatement.setString(1, username);

        boolean alreadyUsedUsername = false;

        ResultSet registeredUsers = checkUserStatement.executeQuery();
        while (registeredUsers.next()) {
            alreadyUsedUsername = !alreadyUsedUsername;
        }

        if (alreadyUsedUsername) {
            System.out.println("That username is already in use.");
        }
        else {
            PreparedStatement addUserStatement = conn.prepareStatement("INSERT INTO users VALUES(?,?)");
            addUserStatement.setString(1, username);
            addUserStatement.setString(2, hashPass(password));
            addUserStatement.executeUpdate();
            System.out.println("Added to database.");
            addUserStatement.close();
        }
        checkUserStatement.close();
        conn.close();
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

        userStatement.close();
        conn.close();
    }

    // Code taken from https://www.geeksforgeeks.org/sha-256-hash-in-java/
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
    // End of copied code
}