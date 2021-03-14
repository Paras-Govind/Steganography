import java.net.*;
import java.sql.*;
import java.io.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.awt.image.*;
import javax.imageio.*;


public class Clotho extends Thread {

    private final String DATABASE_NAME = "Users.db";
    private final String LOG_IN_FLAG = "0";
    private final String CREATE_NEW_USER_FLAG = "1";
    private final Socket s;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    private String username;
    private boolean loggedIn = false;

    public Clotho(Socket s, DataInputStream dis, DataOutputStream dos) {

        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {

        userLogIn();
        try {
            waitForSignal();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void closeCommunication() {

        try {
        s.close();
        dis.close();
        dos.close();
        }
        catch (Exception e) {

        }
    }

    private void userLogIn() {

        while (!loggedIn) {
            try {
                String logInDetails = (String) dis.readUTF();

                String[] detailsArray = logInDetails.split(",");

                if (detailsArray[0].equals("end")) {
                    closeCommunication();
                    loggedIn = !loggedIn;
                }

                else if (detailsArray.length != 3) {
                    dos.writeUTF("There was an error with the data.,false");
                } else if (detailsArray[0].equals(LOG_IN_FLAG)) {
                    String resultString = logIn(detailsArray[1], detailsArray[2]);
                    dos.writeUTF(resultString + "," + loggedIn);
                } else if (detailsArray[0].equals(CREATE_NEW_USER_FLAG)) {
                    String resultString = createUser(detailsArray[1], detailsArray[2]);
                    dos.writeUTF(resultString + "," + loggedIn);
                } else {
                    dos.writeUTF("There was an error with the data.,false");
                }
                dos.flush();
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
    }

    private String createUser(String username, String password) throws Exception {

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);

        PreparedStatement checkUserStatement = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        checkUserStatement.setString(1, username);

        boolean alreadyUsedUsername = false;

        ResultSet registeredUsers = checkUserStatement.executeQuery();
        while (registeredUsers.next()) {
            alreadyUsedUsername = !alreadyUsedUsername;
        }

        checkUserStatement.close();

        if (alreadyUsedUsername) {

            conn.close();
            return "That username is already in use.";
        } else {
            PreparedStatement addUserStatement = conn.prepareStatement("INSERT INTO users VALUES(?,?)");
            addUserStatement.setString(1, username);
            addUserStatement.setString(2, hashPass(password));
            addUserStatement.executeUpdate();
            loggedIn = true;
            this.username = username;
            addUserStatement.close();
            conn.close();
            return "Account has been created.";
        }

    }

    private String logIn(String username, String password) throws Exception {

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

        userStatement.close();
        conn.close();
        if (noOfPasswords == 0) {

            return "No account exists with that username.";
        } else if (retrievedPassword.equals(hashedPassword)) {

            loggedIn = true;
            this.username = username;
            return "Successful log in.";
        } else {
            return "Incorrect Password.";
        }
    }

    // Code taken from https://www.geeksforgeeks.org/sha-256-hash-in-java/
    private static String hashPass(String password) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
    // End of copied code

    private void waitForSignal() throws IOException {

        try {
        while (true) {

            String inputType = (String) dis.readUTF();
            String[] inputDetail = inputType.split(",");
            if (inputDetail[0].equals("1")) {
                waitForImage(inputDetail[1]);
            }

            else if (inputDetail[0].equals("2")) {
                readMessage(inputDetail[1]);
            }

            else if (inputDetail[0].equals("end")) {

                closeCommunication();
            }
        }
    }
        catch (Exception e) {
            closeCommunication();
        }
    }

    private void waitForImage(String recipient) throws IOException {

        dos.writeUTF("ready");
        dos.flush();
    
        BufferedImage image = ImageIO.read(ImageIO.createImageInputStream(dis));

        if (checkForUser(recipient)) {

            File recipientDirectory = new File("StoredImages\\" + recipient + "\\");
            File recipientSenderDirectory = new File(recipientDirectory + username + "\\");
            recipientSenderDirectory.mkdir();
            File[] unsentPictures = recipientSenderDirectory.listFiles();
            String pictureName = "picture";

            if (unsentPictures.length < 1000) {
                pictureName += "0";
            }
            if (unsentPictures.length < 100) {
                pictureName += "0";
            }
            if (unsentPictures.length < 10) {
                pictureName += "0";
            }
            pictureName += unsentPictures.length;
            File storedPicture = new File(recipientSenderDirectory + pictureName + ".png");

            ImageIO.write(image, "png", storedPicture);
            dos.writeUTF("true,Message has been sent");
            dos.flush();
        }
        else {
            dos.writeUTF("false,Recipient doesn't exist.");
            dos.flush();
        }
    }

    private boolean checkForUser(String recipient) {
    
        boolean foundRecipient = false;

        try {

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            PreparedStatement stmnt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmnt.setString(1, recipient);

            ResultSet rs = stmnt.executeQuery();
            while (rs.next()) {
                if (rs.getString(1).equals(recipient)) {
                    foundRecipient = true;
                }
            }
            stmnt.close();
            conn.close();
        }

        catch (Exception e) {

        }

        return foundRecipient;
    }

    private void readMessage(String senderName) throws Exception{

        File senderDirectory = new File("StoredImages\\" + username + "\\" + senderName + "\\");
        if (senderDirectory.exists()) {
            File[] senderFiles = senderDirectory.listFiles();
            if (senderFiles != null) {
                File fileToSend = senderFiles[0];
                if (fileToSend.exists()) {
                    
                }
                else {
                    dos.writeUTF("false,User has sent you no new messages.");
                    dos.flush();                       
                }
            }
            else {
                dos.writeUTF("false,User has sent you no new messages.");
                dos.flush();                
            }
        }
        else {
            dos.writeUTF("false,User has sent you no messages.");
            dos.flush();
        }
    }
}
