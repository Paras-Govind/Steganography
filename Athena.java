import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

public class Athena {

    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;

    private int midFrameX;
    private int midFrameY;

    private JFrame clientFrame;

    private int panelSideLength = 500;

    private String sendFilePath = null;

    private Apollo encrypter = new Apollo();
    private Artemis decrypter = new Artemis();

    public Athena()  {
    
        try {
            s = new Socket("localhost", 6666);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            clientFrame = new JFrame("Athena");
            WindowAdapter closeWindow = new WindowAdapter() {

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
        };
            clientFrame.addWindowListener(closeWindow);
            
            // Steps required to display the frame and only the current frame
            clientFrame.setResizable(false);
            clientFrame.setLayout(null);
            clientFrame.setSize(1920, 1080);
    
            Dimension frameSize = clientFrame.getSize();
    
            midFrameX = (int) frameSize.getWidth() / 2;
            midFrameY = (int) frameSize.getHeight() / 2;
    
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
                            clientFrame.removeWindowListener(closeWindow);
                            clientFrame.dispose();
                            menu();
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
                            clientFrame.removeWindowListener(closeWindow);
                            clientFrame.dispose();
                            menu();
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
            System.out.println("Unknown Host Exception: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private void menu() {

        JFrame menuFrame = new JFrame("Menu");
        menuFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    clientFrame.dispose();
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
        }
        );
        menuFrame.setLayout(null);
        menuFrame.setResizable(false);
        menuFrame.setSize(1920, 1080);

        JButton sendButton = new JButton("Send Messages");
        sendButton.setBounds(midFrameX - 175, midFrameY - 15, 150, 30);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                send();
            }
        });

        JButton readButton = new JButton("Read Messages");
        readButton.setBounds(midFrameX + 25, midFrameY - 15, 150, 30);
        readButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                read();
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(midFrameX - 50, midFrameY + 30, 100, 20);
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    clientFrame.dispose();
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

        menuFrame.add(sendButton);
        menuFrame.add(readButton);
        menuFrame.add(quitButton);

        menuFrame.setVisible(true);
    }

    private void send() {

        JFrame sendFrame = new JFrame("Send");
        sendFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

                sendFilePath = null;
                sendFrame.dispose();
            }
        });
    
        sendFrame.setLayout(new GridLayout(3, 2));

        JLabel imageLabel = new JLabel();
        sendFrame.add(imageLabel);

        JTextField recipient = new JTextField(6);
        sendFrame.add(recipient);

        JTextField secretMessage = new JTextField(6);
        sendFrame.add(secretMessage);

        JButton openImageButton = new JButton("Open");
        openImageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setDialogTitle("Select a .png file");
                FileNameExtensionFilter pngRestrict = new FileNameExtensionFilter("Only .png files", "png");
                fileChooser.addChoosableFileFilter(pngRestrict);

                int choiceMade = fileChooser.showOpenDialog(null);

                if (choiceMade == JFileChooser.APPROVE_OPTION) {

                    sendFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                    ImageIcon previewIcon = new ImageIcon(sendFilePath);
                    imageLabel.setIcon(previewIcon);
                    imageLabel.updateUI();
                }
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try {
                    String message = secretMessage.getText();
                    String reciever = recipient.getText();
                    if (sendFilePath == null) {
                        JOptionPane.showMessageDialog(clientFrame, "Please select an image", "Send Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if (reciever.equals("")) {
                        JOptionPane.showMessageDialog(clientFrame, "Please input a recipient", "Send Error", JOptionPane.ERROR_MESSAGE);                    
                    }
                    else if (message.equals("")) {
                        JOptionPane.showMessageDialog(clientFrame, "Please input a message", "Send Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        encrypter.encryptText(message, sendFilePath);
                        BufferedImage encryptedPicture = ImageIO.read(new File("temp.png"));

                        dos.writeUTF("1," + reciever);
                        dos.flush();
                        dis.readUTF();
                
                        ImageIO.write(encryptedPicture, "png", dos);
                        dos.flush();
                        String sendResult = (String) dis.readUTF();
                        String[] sendDetails = sendResult.split(",");

                        if (sendDetails[0].equals("false")) {
                            JOptionPane.showMessageDialog(clientFrame, sendDetails[1], "Send Error", JOptionPane.ERROR_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(clientFrame, sendDetails[1], "Send Result", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
                catch (IOException exception) {

                }
            }
        });

        sendFrame.setResizable(false);
        sendFrame.setSize(1920, 1080);

        sendFrame.add(openImageButton);
        sendFrame.add(sendButton);

        sendFrame.setVisible(true);
    }

    private void read() {

        JFrame readFrame = new JFrame();
        readFrame.setSize(1920, 1080);

        readFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

                readFrame.dispose();
            }
        });

        readFrame.setLayout(new GridLayout(3, 1));

        JLabel imageLabel = new JLabel();
        JTextArea messageArea = new JTextArea();
        JTextField senderName = new JTextField();


        JButton readButton = new JButton("Read");
        readButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {

                
                    String sender = senderName.getText();

                    if (sender.equals("")) {
                        JOptionPane.showMessageDialog(readFrame, "Please input who you want to recieve a message from.", "Read Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        dos.writeUTF("2," + sender);
                        dos.flush();

                        String readResult = (String) dis.readUTF();
                        String[] readDetails = readResult.split(",");
                        if (readDetails[0].equals("true")) {

                        }
                        else {
                            JOptionPane.showMessageDialog(readFrame, readDetails[1], "Read Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                catch (Exception excep) {

                }
            }
        });

        readFrame.add(imageLabel);
        readFrame.add(messageArea);
        readFrame.add(readButton);
    }
}
