

import java.awt.Point;
import java.awt.Rectangle;
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

    public BoxPool solve() {
        int generation = 0;
        BoxPool bestSolution = null;
        double bestFitness = Double.MAX_VALUE;

        while (true) {
            ArrayList<BoxPool> newPopulation = new ArrayList<>(populationSize);
            Collections.sort(population, (x,y) -> (Integer.compare(calcBestArea(x), calcBestArea(y))));

            newPopulation.add(population.get(0));
            newPopulation.add(population.get(1));

            // Yeni nesil için bireyleri seç, çaprazla ve mutasyona uğrat
            for (int i = 0; i < populationSize -2; i++) {
                BoxPool parent1 = selectParent();
                BoxPool parent2 = selectParent();
                BoxPool child = crossover(parent1, parent2);
                child = mutate(child);
                child.resolveConflict();
                child.align();

                newPopulation.add(child);
            }

            // En iyi bireyi bul ve kaydet
            for (BoxPool pool : newPopulation) {
                double fitness = calcBestArea(pool);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSolution = pool;
                }
            }
            // Durum kontrolü
            if (terminationConditionMet(generation, bestSolution)) {
                System.out.println("Ideal condition met, stopping the algorithm.");
                break;
            }

            population = newPopulation;
            generation++;
            if(generation % 50 == 0)
                System.out.println("Generation " + generation + " Best Fitness: " + bestFitness);
        }

        // Sonuçları konsola yazdır ve görselleştir
        if(bestSolution == null){
            System.err.println("No solution found.");
            return null;
        }
        
        System.out.println("Best solution found:");
        printSolutionDetails(bestSolution);
        return bestSolution;
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
        // Eğer daha detaylı bilgiler yazdırmak istiyorsanız, bu metod içerisine ekleyebilirsiniz.
    }

    // Termination condition remains the same
    private boolean terminationConditionMet(int generation, BoxPool bestSolution) {
        // Nesil sayısı kontrolü
        if (generation >= 1000) {
            return true;
        }

        // Fitness eşik değeri kontrolü
        double fillRatio = calculateFillRatio(bestSolution);
        return (fillRatio <= 1.0) && (fillRatio >= idealFitnessThreshold);
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

// One-point crossover
    static private BoxPool crossover(BoxPool p1, BoxPool p2) {
        BoxPool child = new BoxPool(p1);
        for (int i = 0; i < p1.size(); i++) {
            if(rand.nextBoolean())
                p1.setRectangle(i, p2.getRectangle(i));
        }
        child.resolveConflict();
        child.align();
        return child;
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
