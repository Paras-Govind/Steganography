import java.awt.image.*;
import java.io.*;

public class Artemis {


    public String decryptText(BufferedImage image) throws IOException {

        String binary = "";
        String message = "";

        int x = 0;
        int y = 0;

        long numberOfChars = 999999999999999999l;
        String numberOfCharsBinary = "";
        int pairsFound = 0;

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

                int pixelValue = components[colorIndex];
                String pixelValueString = String.format("%8s", Integer.toBinaryString(pixelValue)).replace(" ", "0")
                        .substring(6, 8);

                pairsFound++;

                if (pairsFound > 4) {
                    binary += pixelValueString;
                    System.out.println(pixelValueString);
                }

                else {
                    
                    numberOfCharsBinary += pixelValueString;

                    if (pairsFound == 4) {
                        numberOfChars = Long.parseLong(numberOfCharsBinary, 2);
                    }
                }

                if (binary.length() >= numberOfChars*8) {
                    itIsOver = true;
                    break;
                }
            }

            if (itIsOver) {
                System.out.println(pixelNumber);
                break;
            }
        }
        
        for (int i = 0; i < binary.length(); i += 8) {
            String characterBinary = binary.substring(i, i + 8);

            message += (char) Integer.parseInt(characterBinary, 2);
        }

        // 00101010 for *
        // 00100001 for !

        return message;
    }
}