import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class PNGBoxPoolRenderer implements BoxPoolRenderer {
    private final String fname;
    public PNGBoxPoolRenderer(String fname){
        this.fname = fname;
    }
    @Override
    public void render(BoxPool pool){
        Random random = new Random();
        Rectangle bnd = pool.getGrid();
        pool.align();
        BufferedImage image = new BufferedImage(bnd.width, bnd.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Color.WHITE);
        Rectangle[] recs = pool.getRectangles();
        g.setColor(Color.BLACK);
        g.fill(pool.getBoundary());
        for (Rectangle rec : recs) {
    		int red = random.nextInt(128 - 32) +32;
            int green = random.nextInt(255-64) + 64;
            int blue = random.nextInt(255-128)+128;
            g.setColor(new Color(red, green, blue));
            g.fill(rec);
        }

		// save image as PNG
		File file = new File(fname);
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
