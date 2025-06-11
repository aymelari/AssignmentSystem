package org.example.assignmentsystem;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service containing the Genetic Algorithm implementation
 * to optimize volunteer assignments to services.
 */
@Service
public class GeneticAlgorithmService {

    /**
     * Inner class implementing the Genetic Algorithm for volunteer assignment.
     */
    public static class GeneticAlgorithm {

        // Configuration constants for the GA
        public static final int NUM_GENERATIONS = 100;          // Number of iterations
        public static final int POPULATION_SIZE = 50;           // Population size per generation
        public static final int MAX_VOLUNTEERS_PER_SERVICE = 3; // Capacity limit per service
        public static final int NUM_SERVICES = 10;              // Number of different services

        private final List<String> serviceList;                  // List of service names
        private final Map<String, Volunteer> volunteers;         // Map of volunteers by name
        private final AssignmentService assignmentService;       // Reference to assignment service

        /**
         * Constructor initializing GA with services, volunteers, and assignment service reference.
         *
         * @param serviceList      list of service names available
         * @param volunteers       map of volunteer objects keyed by their names
         * @param assignmentService reference to service handling assignments and preferences
         */
        public GeneticAlgorithm(List<String> serviceList, Map<String, Volunteer> volunteers, AssignmentService assignmentService) {
            this.serviceList = serviceList;
            this.volunteers = volunteers;
            this.assignmentService = assignmentService;
        }

        /**
         * Runs the genetic algorithm to find an optimal assignment.
         *
         * @return best Chromosome (assignment) found after NUM_GENERATIONS
         */
        public Chromosome run() {
            // Generate initial random population
            List<Chromosome> population = generateInitialPopulation();

            // Iterate through generations
            for (int generation = 0; generation < NUM_GENERATIONS; generation++) {
                // Sort population by fitness (lower is better)
                population.sort(Comparator.comparingInt(Chromosome::getFitness));
                List<Chromosome> newPopulation = new ArrayList<>();

                // Elitism: retain the top 2 best solutions directly
                newPopulation.add(population.get(0));
                newPopulation.add(population.get(1));

                // Fill rest of the population with offspring via selection, crossover, and mutation
                while (newPopulation.size() < POPULATION_SIZE) {
                    Chromosome parent1 = select(population);
                    Chromosome parent2 = select(population);
                    Chromosome child = crossover(parent1, parent2);
                    mutate(child);
                    newPopulation.add(child);
                }

                // Replace old population with the new generation
                population = newPopulation;
            }

            // Return the best chromosome (solution) found
            return population.stream().min(Comparator.comparingInt(Chromosome::getFitness)).orElse(null);
        }

        /**
         * Generates the initial population of chromosomes with random valid assignments.
         *
         * @return list of initial Chromosomes
         */
        private List<Chromosome> generateInitialPopulation() {
            List<Chromosome> population = new ArrayList<>();
            List<String> volunteerNames = new ArrayList<>(volunteers.keySet());

            for (int i = 0; i < POPULATION_SIZE; i++) {
                Map<String, List<String>> serviceAssignments = new HashMap<>();
                Map<String, String> assignment = new HashMap<>();

                // Initialize empty volunteer lists for each service
                for (String service : serviceList) {
                    serviceAssignments.put(service, new ArrayList<>());
                }

                // Shuffle volunteers to create variety in assignments
                Collections.shuffle(volunteerNames);
                for (String vName : volunteerNames) {
                    Volunteer v = volunteers.get(vName);
                    List<String> prefs = new ArrayList<>(v.getPreferences());

                    // Shuffle preferences for randomness
                    Collections.shuffle(prefs);
                    boolean assigned = false;

                    // Assign volunteer to a preferred service if capacity allows
                    for (String s : prefs) {
                        if (serviceAssignments.get(s).size() < MAX_VOLUNTEERS_PER_SERVICE) {
                            serviceAssignments.get(s).add(vName);
                            assignment.put(vName, s);
                            assigned = true;
                            break;
                        }
                    }

                    // If not assigned based on preferences, assign to any service with capacity
                    if (!assigned) {
                        for (String s : serviceList) {
                            if (serviceAssignments.get(s).size() < MAX_VOLUNTEERS_PER_SERVICE) {
                                serviceAssignments.get(s).add(vName);
                                assignment.put(vName, s);
                                break;
                            }
                        }
                    }
                }

                // Create a Chromosome with the generated assignment map
                population.add(new Chromosome(assignmentService, assignment));
            }

            return population;
        }

        /**
         * Selects a chromosome from the population using tournament selection.
         *
         * @param population list of chromosomes
         * @return selected Chromosome
         */
        private Chromosome select(List<Chromosome> population) {
            Random r = new Random();
            Chromosome a = population.get(r.nextInt(population.size()));
            Chromosome b = population.get(r.nextInt(population.size()));
            return a.getFitness() < b.getFitness() ? a : b;
        }

        /**
         * Creates a new Chromosome by combining assignments from two parent Chromosomes.
         *
         * @param c1 first parent Chromosome
         * @param c2 second parent Chromosome
         * @return child Chromosome after crossover
         */
        private Chromosome crossover(Chromosome c1, Chromosome c2) {
            Map<String, String> childMap = new HashMap<>();

            // For each volunteer, randomly choose assignment from one of the parents
            for (String v : c1.getVolunteerToService().keySet()) {
                if (Math.random() < 0.5) {
                    childMap.put(v, c1.getVolunteerToService().get(v));
                } else {
                    childMap.put(v, c2.getVolunteerToService().get(v));
                }
            }
            return new Chromosome(assignmentService, childMap);
        }

        /**
         * Mutates a Chromosome by randomly changing some volunteer assignments.
         *
         * @param c Chromosome to mutate
         */
        private void mutate(Chromosome c) {
            Map<String, String> map = c.getVolunteerToService();
            List<String> services = new ArrayList<>(serviceList);
            Random random = new Random();

            // Each volunteer has a small chance (5%) to be reassigned randomly
            for (String v : map.keySet()) {
                if (Math.random() < 0.05) {
                    map.put(v, services.get(random.nextInt(services.size())));
                }
            }

            // Recalculate fitness after mutation by creating a new Chromosome instance
            // Note: This line currently creates a new instance but does not replace the reference outside
            c = new Chromosome(assignmentService, map);
        }
    }
}
