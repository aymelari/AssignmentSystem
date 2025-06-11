package org.example.assignmentsystem;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for managing volunteers, their preferences,
 * and calculating volunteer-to-service assignments using a Genetic Algorithm.
 */
@Service
@Getter
public class AssignmentService {

    /**
     * Thread-safe map storing volunteers by their name.
     * Acts as in-memory storage for preferences.
     */
    private Map<String, Volunteer> volunteers = new ConcurrentHashMap<>();

    /**
     * List holding the current assignment results.
     * Each entry represents a service and its assigned volunteers.
     */
    private final List<ServiceAssignment> assignments = new ArrayList<>();

    /**
     * Maximum number of volunteers allowed per service.
     * This is a fixed constraint in the assignment logic.
     */
    private static final int MAX_VOLUNTEERS_PER_SERVICE = 3;

    /**
     * Saves or updates the preferences for a volunteer.
     * @param volunteer the Volunteer object containing name and preference list
     */
    public void savePreferences(Volunteer volunteer) {
        volunteers.put(volunteer.getName(), volunteer);
    }

    /*
    // Alternative asynchronous execution using Spring's @Async (commented out)
    @Async
    public CompletableFuture<List<ServiceAssignment>> runOptimizationAsync() {
        return CompletableFuture.completedFuture(runOptimization());
    }
    */

    /**
     * Starts the optimization process in a new thread to avoid blocking
     * the main thread or incoming HTTP requests.
     * This allows concurrent handling of multiple clients.
     */
    public void runOptimizationInNewThread() {
        Thread thread = new Thread(() -> {
            runOptimization(); // Run the Genetic Algorithm optimization
            System.out.println("Optimization finished in background thread.");
        });
        thread.start();
    }

    /**
     * Runs the Genetic Algorithm to compute an optimal assignment of volunteers to services.
     * Ensures that service capacity constraints are respected.
     *
     * @return a list of ServiceAssignment objects representing the result
     */
    public List<ServiceAssignment> runOptimization() {

        // Define the fixed list of services available
        List<String> serviceNames = List.of(
                "Reception", "Security", "Food", "Transport", "Cleaning",
                "Logistics", "Guiding", "Medical", "Registration", "HelpDesk"
        );

        // Create and run the genetic algorithm with current volunteers and services
        GeneticAlgorithmService.GeneticAlgorithm ga = new GeneticAlgorithmService.GeneticAlgorithm(serviceNames, volunteers, this);
        Chromosome best = ga.run();

        // Clear previous assignments before storing new results
        assignments.clear();

        // Initialize map from service names to assigned volunteer lists
        Map<String, List<String>> serviceToVolunteers = new HashMap<>();
        for (String service : serviceNames) {
            serviceToVolunteers.put(service, new ArrayList<>());
        }

        // Populate service-to-volunteers map from the best chromosome solution
        best.getVolunteerToService().forEach((volunteer, service) -> {
            serviceToVolunteers.get(service).add(volunteer);
        });

        // Convert map entries to ServiceAssignment objects and store in assignments list
        serviceToVolunteers.forEach((service, vols) -> {
            assignments.add(new ServiceAssignment(service, vols));
        });

        // Return the final assignments
        return assignments;
    }
}
