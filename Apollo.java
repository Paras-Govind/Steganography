import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.plaf.ColorUIResource;

public class Apollo {

    public static void main(String[] args) throws IOException {
        Apollo ap = new Apollo();
        ap.encryptText("Hello", "largest-test.png");
    }

    private String encryptMessage(String text) {

        String characters = "";
        String keyword = "PARTHENON";
        String keywordChars = "";
        String newText = "";

        int index = 0;
        while(keywordChars.length() != text.length()) {
            if(index == keyword.length()) {
                index = 0;
            }
            
            keywordChars += keyword.charAt(index);
            index++;
        }

        System.out.println(keywordChars);

        for (int i = 0; i < 256; i++) {
            characters += (char) i;
        }

        System.out.println(characters);

        char[][] table = new char[5][5];

        int start = 0;

        table[0][0] = 'X';
        for (int i = 1; i < table.length; i++) {
            table[0][i] = characters.charAt(i - 1);
            table[i][0] = characters.charAt(i - 1);
        }

        for (int row = 1; row < table.length; row++) {
            for (int col = 1; col < table.length; col++) {
                int pos = start + col - 1;

                if (pos >= characters.length()) {
                    pos = col - 1;
                }

                table[row][col] = characters.charAt(pos);
            }
            start++;
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            int row = 0;

            // find column
            for (int j = 0; j < table.length; j++) {
                if (table[j][0] == c) {
                    row = j;
                    break;
                }
            }

            for (int j = 0; j < table.length; j++) {
                if (table[row][j] == keywordChars.charAt(i)) {
                    newText += table[0][j];
                    break;
                }
            }
        }


        return newText;
    }
    
    public void encryptText(String text, String filePath) throws IOException {

        text = encryptMessage(text);

        BufferedImage image = ImageIO.read(new File(filePath));

        System.out.println(text.length());
        byte[] bytes = text.getBytes("UTF-8");

        String binary = String.format("%8s", Integer.toBinaryString(text.length())).replace(" ", "0");

        for (int i = 0; i < bytes.length; i++) {
            int asciiValue = bytes[i];

            String binaryValue = Integer.toBinaryString(asciiValue);

            binary += String.format("%8s", binaryValue).replace(" ", "0");
        }

        System.out.println(binary.substring(binary.length() - 8, binary.length()));

        int x = 0;
        int y = 0;

        int pairIndex = 0;

        int[] components;

        for (int pixelNumber = 0; pixelNumber < image.getWidth() * image.getHeight(); pixelNumber++) {
            boolean itIsOver = false;
            x = pixelNumber % image.getWidth();
            y = pixelNumber / image.getWidth();

            int clr = image.getRGB(x, y);
            int red = (clr & 0x00ff0000) >> 16;
            int green = (clr & 0x0000ff00) >> 8;
            int blue = clr & 0x000000ff;

            components = new int[] { red, green, blue };

            for (int colorIndex = 0; colorIndex < 3; colorIndex++) {

                String pair = binary.substring(pairIndex, pairIndex + 2);

                int pixelValue = components[colorIndex];
                String pixelValueString = String.format("%8s", Integer.toBinaryString(pixelValue)).replace(" ", "0");
                String newPixelValueString = pixelValueString.substring(0, 6);
                newPixelValueString += pair;
                int newPixelValue = Integer.parseInt(newPixelValueString, 2);

                components[colorIndex] = newPixelValue;

                System.out.println(pair + ": " + pixelValueString + " became " + newPixelValueString);

                pairIndex += 2;

                if (pairIndex == binary.length()) {
                    itIsOver = true;
                    break;
                }
            }

            if (itIsOver) {
                System.out.println(pixelNumber);
                Color newColor = new ColorUIResource(components[0], components[1], components[2]);

                image.setRGB(x, y, newColor.getRGB());
                break;
            }
            
            Color newColor = new ColorUIResource(components[0], components[1], components[2]);

            image.setRGB(x, y, newColor.getRGB());
        }

        File file = new File("temp.png");

        ImageIO.write(image, "png", file);
    }

}