import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BoxPool {
    // why did I store the variables like this instead of a ArrayList<Rectangle>?
    // there was a very smart reason when I first started...
    private Rectangle maxBoundary;
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
    public BoxPool(BoxPool pool) {
        maxBoundary = pool.maxBoundary;
        widths = new ArrayList<Integer>(pool.widths);
        xoff = new ArrayList<Integer>(pool.xoff);
        heights = new ArrayList<Integer>(pool.heights);
        yoff = new ArrayList<Integer>(pool.yoff);
        nitems = pool.nitems;
    }

    public BoxPool(String fname) {
        this();
		try { // this section is from internet
			Scanner scanner = new Scanner(new File(fname));

			int count = scanner.nextInt();
            maxBoundary = new Rectangle(scanner.nextInt(), scanner.nextInt());
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
    public void resolveConflict(){
		Random rand = new Random();
		while (hasConflict() ) {
			for (int i = 0; i < nitems; i++) {
				double[] sdf = calcSDF(i);
				if(sdf[0]< 0.0){
					move(i, new Point((int)(Math.round(sdf[1]*3) ), (int)(Math.round( sdf[2]*3))));
				}else{
					move(i, new Point((int)(-Math.round(sdf[1]*3) ), (int)(-Math.round( sdf[2]*3))));	
				}
				move(i, new Point(rand.nextInt(4)-2,rand.nextInt(4)-2));
			}
        }
	}

    public void addRectangle(Rectangle rec) {
        widths.add(rec.width);
        heights.add(rec.height);
        xoff.add(rec.x);
        yoff.add(rec.y);
        nitems += 1;
    }
    public void setRectangle(int ind, Rectangle rec) {
        widths.set(ind,rec.width);
        heights.set(ind,rec.height);
        xoff.set(ind,rec.x);
        yoff.set(ind,rec.y);
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
    public Rectangle getRectangle(int ind){
        return new Rectangle(xoff.get(ind), yoff.get(ind), widths.get(ind), heights.get(ind));
    }

    public Rectangle[] getRectangles() {
        Rectangle[] out = new Rectangle[nitems];
        for (int i = 0; i < out.length; i++) {
            out[i] = getRectangle(i);
        }
        return out;
    }

    public int calcTotalArea() {
        int out = 0;
        for (int i = 0; i < nitems; i++)
            out += widths.get(i) * heights.get(i);
        return out;
    }
    public boolean hasConflict(){
		for (int i = 0; i < nitems; i++) {
			double[] temp = calcSDF(i);
			if(temp[0] < -0.0)
				return true;
		}
		return false;
	}

    public Rectangle getGrid(){
        return maxBoundary;
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
    public boolean isContained(){
        for (int i = 0; i < nitems; i++) {
            boolean temp = maxBoundary.contains(getRectangle(i));
            if(!temp)
                return false;
        }
        return true;
    }

    public void align(){
        Rectangle bound = getBoundary();
        offset(new Point(-bound.x, -bound.y));
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

    public void place(int ind, Point p) {
        xoff.set(ind, p.x);
        yoff.set(ind, p.y);
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

    public double[] calcSDF(int b1) {
        double out = Double.MAX_VALUE;
        double[] outd = new double[2];
        for (int i = 0; i < nitems; i++) {
            if (i == b1)
                continue;
            double tdis = distance(b1, i);
            if (out >= tdis) {
                out = tdis;
                outd = delta(b1, i);
            }
        }
        double[] grad = grad(outd);
        return new double[] { out, grad[0], grad[1] };
    }

    public double[] delta(int b1, int b2) {
        double x = ( xoff.get(b1) - xoff.get(b2)) + (widths.get(b1) - widths.get(b2))/2.0;
        double y = ( yoff.get(b1) - yoff.get(b2)) + (heights.get(b1) - heights.get(b2))/2.0;
        return new double[]{x, y};
    }
    private double regionCheck(int i1, int i2, List<Integer> x,List<Integer> l){
        double ma = Math.max(x.get(i1),x.get(i2));
        double mi = Math.min(x.get(i1)+l.get(i1), x.get(i2)+l.get(i2));
        return ma - mi;
    }
    private double distance(int b1, int b2){
        double x = regionCheck(b1, b2, xoff, widths);
        double y = regionCheck(b1, b2, yoff, heights);
        if(x < -0.0 && y < -0.0)
            return x + y;
        else
            return Math.max(x, 0.0) + Math.max(y,0.0);
    }
    private double[] grad(double[] delt){
        double l = Math.sqrt(delt[0]*delt[0] + delt[1]*delt[1]);
        if (l != 0.0)
            return new double[]{delt[0]/l, delt[1]/l};
        return delt;
    }
}
