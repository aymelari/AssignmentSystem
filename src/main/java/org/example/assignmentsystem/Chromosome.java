package org.example.assignmentsystem;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents a candidate solution (chromosome) in the Genetic Algorithm.
 * Maps each volunteer's name to a service assignment.
 * Calculates a fitness score based on how well the assignments match volunteer preferences.
 */
@Getter
@Setter
public class Chromosome {

    /**
     * Reference to the assignment service to access volunteers and save data if needed.
     */
    private AssignmentService assignmentService;

    /**
     * Map storing the assignment of volunteers to services.
     * Key: volunteer name, Value: assigned service name
     */
    private Map<String, String> volunteerToService;

    /**
     * Fitness score representing total dissatisfaction across all volunteers.
     * Lower fitness means better overall assignment.
     */
    private int fitness;

    /**
     * Map of volunteers keyed by their names, retrieved from AssignmentService.
     */
    private Map<String, Volunteer> volunteerMap;

    /**
     * Constructor that initializes the chromosome with assignments,
     * links to the assignment service, and calculates fitness.
     *
     * @param assignmentService reference to the AssignmentService
     * @param volunteerToService map of volunteer-to-service assignments
     */
    public Chromosome(AssignmentService assignmentService, Map<String, String> volunteerToService) {
        this.assignmentService = assignmentService;
        this.volunteerToService = volunteerToService;

        // Example: save a volunteer's preferences if needed (optional, for initial testing)
        Volunteer volunteer = new Volunteer("Aysu", List.of("Reception", "Security"));
        assignmentService.savePreferences(volunteer);

        // Retrieve the latest volunteer map from the assignment service
        volunteerMap = assignmentService.getVolunteers();

        // Calculate fitness score based on the current assignment
        calculateFitness();
    }

    /**
     * Calculates the fitness score of the chromosome.
     * The score sums the dissatisfaction of each volunteer based on preference ranking.
     * If assigned service is not in volunteer's preference list, adds a penalty of 5.
     */
    private void calculateFitness() {
        int totalDissatisfaction = 0;

        for (Map.Entry<String, String> entry : volunteerToService.entrySet()) {
            String volunteer = entry.getKey();
            String assignedService = entry.getValue();

            assert volunteerMap != null;

            // Get volunteer's preference list
            List<String> prefs = volunteerMap.get(volunteer).getPreferences();

            // Find rank of assigned service in preferences; -1 if not found
            int rank = prefs.indexOf(assignedService);

            // Add dissatisfaction: 0 if top choice, up to 5 if not preferred
            totalDissatisfaction += (rank == -1 ? 5 : rank);
        }

        this.fitness = totalDissatisfaction;
    }
}
