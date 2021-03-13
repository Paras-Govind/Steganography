import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.plaf.ColorUIResource;

public class Apollo {

    public static void main(String[] args) throws IOException {
        encryptText("Hello World!");
    }

    private static BufferedImage loadImage(String path) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int clr = image.getRGB(x, y);
                    int red = (clr & 0x00ff0000) >> 16;
                    int green = (clr & 0x0000ff00) >> 8;
                    int blue = clr & 0x000000ff;

                    String rgb = Integer.toString(red) + "," + Integer.toString(green) + "," + Integer.toString(blue);

                    System.out.printf("(%-10s),", rgb);

                    // Color Red get cordinates
                    // if (red == 255) {
                    //     System.out.println(String.format("Coordinate %d %d", x, y));
                    // } else {
                    //     System.out.println("Red Color value = " + red);
                    //     System.out.println("Green Color value = " + green);
                    //     System.out.println("Blue Color value = " + blue);
                    // }
                }

                System.out.println();
            }

        }

        catch (IOException e) {
        }

        return image;

    }
    
    public static void encryptText(String text) throws IOException {
        BufferedImage image = loadImage("test.png");

        for (int i = 0; i < text.getBytes().length; i++) {
            Byte value = text.getBytes()[i];

            int y = i / image.getWidth();
            int x = i % image.getWidth();

            Color color = new ColorUIResource(value, value, value);

            image.setRGB(x, y, color.getRGB());
        }

        File file = new File("test-result.png");

        ImageIO.write(image, "png", file);
    }
}