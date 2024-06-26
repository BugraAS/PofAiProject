import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GeneticAlgorithm {

    static private Random rand = new Random();
    private ArrayList<BoxPool> population;
    private BoxPool adam;
    private int populationSize;
    private double mutationRate;
    private double idealFitnessThreshold = 0.9;
    private static final int MAX_GENERATION = 5000;

    public GeneticAlgorithm(BoxPool pool, int populationSize, double mutationRate) {
        this.adam = pool;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.population = new ArrayList<>(populationSize);
        initializePopulation();
    }

    static BoxPool randomizePool(BoxPool pool){
        BoxPool out = new BoxPool(pool);
        for (int i = 0; i < out.size(); i++) {
            out.place(i, new Point(rand.nextInt(20) - 10,rand.nextInt(20) - 10));
            if(rand.nextBoolean())
                out.rotate(i);
        }
        out.resolveConflict();
        out.align();
        return out;
    }

    private void initializePopulation() {
        for (int i = 0; i < populationSize; i++) {
            population.add(randomizePool(adam));
        }
    }
    static private BoxPool mutatePool(int ind, BoxPool pool){
        BoxPool out = new BoxPool(pool);
        out.align();
        out.resolveConflict();
        if(rand.nextBoolean())
            out.rotate(ind);
        else{
            Rectangle rec = out.getRectangle(ind);
            Rectangle grid = out.getBoundary();
            if(grid.contains(out.getGrid()))
                grid = out.getGrid();
            out.place(ind, new Point(rand.nextInt(grid.width-rec.width+1), rand.nextInt(grid.height-rec.height+1)));
        }
        out.resolveConflict();
        out.align();
        return out;
    }

    //TODO: Handle the cases where rectangles dont fit inside grid
    // this method is meant to find those rectangles and mutate them until everything fits
    static private int findPeeker(BoxPool pool){
        int out = -1;
        Rectangle grid = pool.getGrid();
        for (int i = 0; i < pool.size(); i++) {
            if(!grid.contains(pool.getRectangle(i)))
                return i;
        }
        return out;
    }

    // One-point crossover
    static private BoxPool crossover(BoxPool p1, BoxPool p2) {
        BoxPool child = new BoxPool(p1);
        for (int i = 0; i < p1.size(); i++) {
            if (rand.nextBoolean())
                p1.setRectangle(i, p2.getRectangle(i));
        }
        child.resolveConflict();
        child.align();
        return child;
    }

    static private BoxPool crossoverOPX(BoxPool p1, BoxPool p2) {
        /*
        The OPX Crossover  (One-point Crossover)

        See more information
        https://www.researchgate.net/publication/335991207_Izmir_Iktisat_Dergisi_Gezgin_Satici_Probleminin_Genetik_Algoritmalar_Kullanarak_Cozumunde_Caprazlama_Operatorlerinin_Ornek_Olaylar_Bazli_Incelenmesi_Investigation_Of_Crossover_Operators_Using_Genetic_
        */

        BoxPool child = new BoxPool(p1);
        int c = rand.nextInt(p1.size());

        if (rand.nextBoolean()) {
            while (c < p1.size()) {
                child.setRectangle(c, p2.getRectangle(c));
                c++;
            }
        }

        child.resolveConflict();
        child.align();
        return child;
    }

    static private BoxPool crossoverTPX(BoxPool p1, BoxPool p2) {
        /*
        The TPX Crossover for G1DList (two-point crossover)

        See more information in the
        <https://scholar.google.com/scholar?output=instlink&q=info:7d_ZB2LqT3QJ:scholar.google.com/&hl=tr&as_sdt=0,5&scillfp=480710777611443790&oi=lle>
        <https://scholar.archive.org/work/yxts2ace4rcxfjugvhdjby2o3a/access/wayback/https://www.isr-publications.com/jmcs/691/download-ccgdc-a-new-crossover-operator-for-genetic-data-clustering>
         */

        BoxPool child = new BoxPool(p1);
        int c1 = rand.nextInt(p1.size());
        int c2 = rand.nextInt(p1.size());

        while (c1 == c2) {
            c2 = rand.nextInt(p1.size());
        }

        if (c1 > c2) {
            int temp = c1;
            c1 = c2;
            c2 = temp;
        }

        // TODO check it
//        BoxPool p = new BoxPool();
        if (rand.nextBoolean()) {
//            for (int i = c1; i < c2; i++) {
//                p.addRectangle(p1.getRectangle(i));
//            }
            for (int i = 0; i < p2.size(); i++) {
                if (i <= c1) {
                    child.setRectangle(i, p2.getRectangle(i));
                } else if (i < c2) {
                    child.setRectangle(i, p1.getRectangle(i));
                } else {
                    child.setRectangle(i, p2.getRectangle(i));
                }
            }
        }

        child.resolveConflict();
        child.align();
        return child;
    }

    private double calculateFillRatio(BoxPool pool) {
        Rectangle bnd = pool.getBoundary();
        return pool.calcTotalArea() / (double)(bnd.width*bnd.height);
    }

    private void printSolutionDetails(BoxPool pool) {
        Rectangle[] rectangles = pool.getRectangles();
        System.out.println("Number of Rectangles: " + pool.size());
        for (int i = 0; i < rectangles.length; i++) {
            Rectangle rect = rectangles[i];
            System.out.println("Rectangle " + i + ": x=" + rect.x + ", y=" + rect.y + ", width=" + rect.width + ", height=" + rect.height);
        }
        // If you want to print more detailed information, you can add it to this method.
    }

    public BoxPool solve() {
        int generation = 0;
        BoxPool bestSolution = null;
        double bestFitness = Double.MAX_VALUE;

        while (true) {
            ArrayList<BoxPool> newPopulation = new ArrayList<>(populationSize);
            Collections.sort(population, (x,y) -> (Integer.compare(calcBestArea(x), calcBestArea(y))));

            newPopulation.add(population.get(0));
            newPopulation.add(population.get(1));

            // Select, cross and mutate individuals for the next generation
            for (int i = 0; i < populationSize -2; i++) {
                BoxPool parent1 = selectParent();
                BoxPool parent2 = selectParent();
//                BoxPool child = crossover(parent1, parent2);
//                BoxPool child = crossoverOPX(parent1, parent2);
                BoxPool child = crossoverTPX(parent1, parent2);
                child = mutate(child);
                child.resolveConflict();
                child.align();

                newPopulation.add(child);
            }

            // Select best individual
            for (BoxPool pool : newPopulation) {
                double fitness = calcBestArea(pool);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSolution = pool;
                }
            }

            // Condition check
            if (terminationConditionMet(generation, bestSolution)) {
                System.out.println("Ideal condition met, stopping the algorithm.");
                break;
            }

            population = newPopulation;
            generation++;
            if(generation % 50 == 0)
                System.out.println("Generation " + generation + " Best Fitness: " + bestFitness);
        }

        // Print results on the console and visualize it
        if(bestSolution == null){
            System.err.println("No solution found.");
            return null;
        }

        System.out.println("Best solution found:");
        printSolutionDetails(bestSolution);
        return bestSolution;
    }

    // Tournament selection strategy
    private BoxPool selectParent() {
        Random rand = new Random();
        int tournamentSize = 5; // Or some other size you determine
        BoxPool best = null;
        double bestFitness = Double.MAX_VALUE;

        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = rand.nextInt(population.size());
            BoxPool contender = population.get(randomIndex);
            double contenderFitness = calcBestArea(contender);
            if (best == null || contenderFitness < bestFitness) {
                best = contender;
                bestFitness = contenderFitness;
            }
        }
        return best;
    }

    static private int calcBestArea(BoxPool pool){
        Rectangle bnd = pool.getBoundary();
        return  bnd.width*bnd.height - pool.calcTotalArea();
    }


    // Termination condition remains the same
    private boolean terminationConditionMet(int generation, BoxPool bestSolution) {
        // Generation number checking
        if (generation >= MAX_GENERATION) {
            return true;
        }

        // Fitness threshold checking
        double fillRatio = calculateFillRatio(bestSolution);
        return (fillRatio <= 1.0) && (fillRatio >= idealFitnessThreshold);
    }

    private BoxPool mutate(BoxPool pool) {

        for (int i = 0; i < pool.size(); i++) {
            if (rand.nextDouble() < mutationRate) {
                pool=mutatePool(i, pool); // this a problem for garbage collector to deal with
            }
        }
        pool.resolveConflict();
        pool.align();
        return pool;
    }


}
