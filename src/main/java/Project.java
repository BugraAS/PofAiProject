
/*
 * Class Structure
 *
//[x] = BoxPool
 * 		- widths : ArrayList[]
 * 		- xoff : ArrayList[]
 * 		- heights : ArrayList[]
 * 		- yoff : ArrayList[]
 * 		- nitems : int
 * 		+ addRectangle(Rectangle r) : void
 * 		+ removeRectangle(int index) : void
 * 		+ getRectangles() : Rectangle[]
 * 		+ getBoundary() : Rectangle
 * 		+ calcTotalArea() : int
 * 		+ size() : int
 * 		+ rotateRectangle(int index) : void
 * 		+ calcSDF(int index) : double[] // kinda sloppy but will do
 * 
//[ ] = GeneticSolver
 * 		+ solve(BoxPool pool) : BoxPool
 * 		- mutate(BoxPool pool): BoxPool
 * 		- breed(BoxPool mom, BoxPool dad): BoxPool
 * 
//[x] = BoxPoolRenderer : Interface
 * 		+ render(BoxPool)
 * 
//[x] = PNGBoxPoolRenderer : BoxPoolRenderer
 * 
//[ ] = TXTBoxPoolRenderer : BoxPoolRenderer
 * 
 */

public class Project {
	public static void main(String[] args) {
		BoxPool pool = new BoxPool("input.txt");
		pool.resolveConflict();
		pool.align();
		GeneticAlgorithm solver = new GeneticAlgorithm(pool, 40, 0.05);

		BoxPool solution = solver.solve();
		solution.resolveConflict();

		PNGBoxPoolRenderer renderer = new PNGBoxPoolRenderer("dumb.png");
		renderer.render(pool);
		PNGBoxPoolRenderer renderer2 = new PNGBoxPoolRenderer("boxes.png");
		renderer2.render(solution);
	}
}
