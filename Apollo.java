import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.plaf.ColorUIResource;

public class Apollo {
    
    public void encryptText(String text, String filePath) throws IOException {
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
    
    // private static Color getAverageColor(BufferedImage image, int x, int y) {
    //     int height = image.getHeight();
    //     int width = image.getWidth();

    //     int totalRed = 0;
    //     int totalGreen = 0;
    //     int totalBlue = 0;

    //     int totalPixels = 0;

    //     for (int row = Math.max(y - 1, 0); row < Math.min(y + 1, height); row++) {
    //         for (int col = Math.max(x - 1, 0); col < Math.min(x + 1, width); col++) {
    //             int clr = image.getRGB(col, row);
    //             totalRed += (clr & 0x00ff0000) >> 16;
    //             totalGreen += (clr & 0x0000ff00) >> 8;
    //             totalBlue += clr & 0x000000ff;

    //             totalPixels++;
    //         }
    //     }

    //     int averageRed = totalRed / totalPixels;
    //     int averageGreen = totalGreen / totalPixels;
    //     int averageBlue = totalBlue / totalPixels;

    //     return new ColorUIResource(averageRed, averageGreen, averageBlue);

    //     int clr = image.getRGB(Math.max(x - 1, 0), y);
    //     int red = (clr & 0x00ff0000) >> 16;
    //     int green = (clr & 0x0000ff00) >> 8;
    //     int blue = clr & 0x000000ff;

    //     return new ColorUIResource(red, green, blue);
    // }
}