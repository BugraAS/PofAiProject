import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class BoxPool {
    // why did I store the variables like this instead of a ArrayList<Rectangle>?
    // there was a very smart reason when I first started...
    private ArrayList<Integer> widths;
    private ArrayList<Integer> xoff;
    private ArrayList<Integer> heights;
    private ArrayList<Integer> yoff;
    private int nitems;

    public BoxPool() {
        widths = new ArrayList<Integer>();
        xoff = new ArrayList<Integer>();
        heights = new ArrayList<Integer>();
        yoff = new ArrayList<Integer>();
        nitems = 0;
    }

    public BoxPool(String fname) {
        this();
		try { // this section is from internet
			Scanner scanner = new Scanner(new File(fname));

			int count = scanner.nextInt();
            for (int i = 0; i < count; i++) {
                int width = scanner.nextInt();
                int height = scanner.nextInt();
                addRectangle(new Rectangle(width, height));
            }
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            throw new IllegalArgumentException();
		}
    }

    public void addRectangle(Rectangle rec) {
        widths.add(rec.width);
        heights.add(rec.height);
        xoff.add(rec.x);
        yoff.add(rec.y);
        nitems += 1;
    }

    public void removeRectangle(int index) {
        widths.remove(index);
        heights.remove(index);
        xoff.remove(index);
        yoff.remove(index);
        nitems -= 1;
    }

    public int size() {
        return nitems;
    }

    public Rectangle[] getRectangles() {
        Rectangle[] out = new Rectangle[nitems];
        for (int i = 0; i < out.length; i++) {
            out[i] = new Rectangle(xoff.get(i), yoff.get(i), widths.get(i), heights.get(i));
        }
        return out;
    }

    public int calcTotalArea() {
        int out = 0;
        for (int i = 0; i < nitems; i++)
            out += widths.get(i) * heights.get(i);
        return out;
    }

    public Rectangle getBoundary() {
        Rectangle out = new Rectangle();
        int tmin = 0;
        int tmax = 0;
        for (int i = 0; i < nitems; i++) {
            tmin = Math.min(tmin, xoff.get(i));
            tmax = Math.max(tmax, xoff.get(i) + widths.get(i));
        }
        out.width = tmax - tmin;
        out.x = tmin;
        tmin = 0;
        tmax = 0;
        for (int i = 0; i < nitems; i++) {
            tmin = Math.min(tmin, yoff.get(i));
            tmax = Math.max(tmax, yoff.get(i) + heights.get(i));
        }
        out.height = tmax - tmin;
        out.y = tmin;
        return out;
    }

    public void normalize() {
        Rectangle bound = getBoundary();
        Point off = new Point(-(int) ((double) bound.x + bound.width / 2.0),
                -(int) ((double) bound.y + bound.height / 2.0));
        offset(off);
    }

    public void offset(Point delta) {
        for (int i = 0; i < nitems; i++) {
            xoff.set(i, xoff.get(i) + delta.x);
            yoff.set(i, yoff.get(i) + delta.y);
        }
    }

    public double[] calcSDF(int b1) {
        double out = Double.MAX_VALUE;
        int outi = -1;
        for (int i = 0; i < nitems; i++) {
            if (i == b1)
                continue;
            double temp = distance(b1, i);
            if (out >= temp) {
                out = temp;
                outi = i;
            }
        }
        double[] grad = calcGrad(b1, outi);
        return new double[] { out, grad[0], grad[1] };
    }

    public void move(int ind, Point p) {
        xoff.set(ind, xoff.get(ind) + p.x);
        yoff.set(ind, yoff.get(ind) + p.y);
    }

    public void rotate(int ind) {
        int x = xoff.get(ind);
        int y = yoff.get(ind);
        int width = widths.get(ind);
        int height = heights.get(ind);
        int diff = (width - height) / 2;
        x += diff;
        y -= diff;
        xoff.set(ind, x);
        yoff.set(ind, y);
        widths.set(ind, height);
        heights.set(ind, width);
    }

    private double sdBox(double x, double y, double width, double height) {
        double dx = Math.abs(x) - width;
        double dy = Math.abs(y) - height;
        return Math.max(dx, dy);
    }

    private double distance(int b1, int b2) {
        double x = xoff.get(b1) - xoff.get(b2);
        double y = yoff.get(b1) - yoff.get(b2);
        double b1w = widths.get(b1);
        double b1h = heights.get(b1);
        double aspect = b1w / b1h;
        return sdBox(x, y * aspect, widths.get(b2), heights.get(b2) * aspect) - b1w;
    }

    private double[] calcGrad(int b1, int b2) {
        double x = xoff.get(b1) - xoff.get(b2);
        double y = yoff.get(b1) - yoff.get(b2);
        double b1w = widths.get(b1);
        double b1h = heights.get(b1);
        double aspect = b1w / b1h;
        double dx = sdBox(x, 0, widths.get(b2), 1.0) - b1w;
        double dy = sdBox(0, y * aspect, 1.0, heights.get(b2) * aspect) - b1w;
        if (dx > dy)
            return new double[] { Math.signum(x), 0.0 };
        else
            return new double[] { 0.0, Math.signum(y) };
    }
}
