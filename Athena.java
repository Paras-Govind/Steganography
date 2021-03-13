import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class Athena {

    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;

    private int panelSideLength = 500;

    public Athena()  {
    
        try {
            s = new Socket("localhost", 6666);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            JFrame clientFrame = new JFrame("Athena");
            clientFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    try {
                        dos.writeUTF("end");
                        dos.flush();
                        s.close();
                        dis.close();
                        dos.close();
                    }
                    catch (Exception event) {
                    }
                    System.exit(0);
                }
            });
    
            GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = graphics.getDefaultScreenDevice();
            
            // Steps required to display the frame and only the current frame
            clientFrame.setResizable(false);
            clientFrame.setLayout(null);
            device.setFullScreenWindow(clientFrame);
    
            Dimension frameSize = clientFrame.getSize();
    
            int midFrameX = (int) frameSize.getWidth() / 2;
            int midFrameY = (int) frameSize.getHeight() / 2;
    
            JPanel inputPanel = new JPanel();
            inputPanel.setSize(panelSideLength, panelSideLength);
    
            inputPanel.setLocation(midFrameX - (panelSideLength / 2), midFrameY - (panelSideLength / 2));

            JLabel usernameLabel = new JLabel("Username:");
            JLabel passwordLabel = new JLabel("Password:");

            JTextField userField = new JTextField(6);
            JPasswordField passwordField = new JPasswordField(6);

            usernameLabel.setBounds(0, 0, 100, 20);
            passwordLabel.setBounds(0, 20, 100, 20);
            userField.setBounds(105, 0, 50, 20);
            passwordField.setBounds(105, 20, 50, 20);

            inputPanel.add(usernameLabel);
            inputPanel.add(userField);
            inputPanel.add(passwordLabel);
            inputPanel.add(passwordField);

            JButton logInButton = new JButton("Log In");
            logInButton.setBounds(250, 20, 50, 30);
            logInButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        dos.writeUTF("0," + userField.getText() + "," + new String(passwordField.getPassword()));
                        dos.flush();
                        String logInResult = (String) dis.readUTF();
                        String[] detailsArray = logInResult.split(",");
                        if (detailsArray[1].equals("true")) {
                            JOptionPane.showMessageDialog(clientFrame, detailsArray[0], "Log In Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(clientFrame, detailsArray[0], "Log In Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch (IOException exception) {

                    }
                }
            });

            JButton createAccountButton = new JButton("Create Account");
            createAccountButton.setBounds(250, 40, 50, 30);
            createAccountButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        dos.writeUTF("1," + userField.getText() + "," + new String(passwordField.getPassword()));
                        dos.flush();
                        String logInResult = (String) dis.readUTF();
                        String[] detailsArray = logInResult.split(",");
                        if (detailsArray[1].equals("true")) {
                            JOptionPane.showMessageDialog(clientFrame, detailsArray[0], "Account Creation Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(clientFrame, detailsArray[0], "Account Creation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch (IOException exception) {

                    }
                }
            });
            
            inputPanel.add(logInButton);
            inputPanel.add(createAccountButton);

            clientFrame.add(inputPanel);

            clientFrame.setVisible(true);
        }

        catch (UnknownHostException e) {
            System.out.println("Unknow Host Exception: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
