package org.example.assignmentsystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for handling volunteer assignment operations.
 * Provides endpoints to submit preferences, trigger optimization, and fetch assignments.
 */
@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Constructor to inject the assignment service.
     * @param assignmentService the service handling volunteer assignment logic
     */
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Endpoint to submit or update a volunteer's preferences.
     * Accepts a JSON object representing a Volunteer and stores it in memory.
     *
     * Example request:
     * POST /api/preferences
     * {
     *   "name": "Aysu",
     *   "preferences": ["Reception", "Security", "Food", "Medical", "Transport"]
     * }
     *
     * @param volunteer the volunteer object received from the request body
     * @return HTTP 200 OK response
     */
    @PostMapping("/preferences")
    public ResponseEntity<Void> submitPreferences(@RequestBody Volunteer volunteer) {
        assignmentService.savePreferences(volunteer);
        return ResponseEntity.ok().build(); // Responds with 200 OK and no content
    }

    /**
     * Endpoint to retrieve the current service assignments.
     * Returns a list of ServiceAssignment objects showing each service
     * and its assigned volunteers.
     *
     * Example request: GET /api/assignments
     *
     * @return HTTP 200 OK with assignment list in the response body
     */
    @GetMapping("/assignments")
    public ResponseEntity<List<ServiceAssignment>> getAssignments() {
        return ResponseEntity.ok(assignmentService.getAssignments());
    }

    /**
     * Endpoint to trigger the optimization process.
     * Starts the Genetic Algorithm in a separate thread to compute
     * fair volunteer-to-service assignments based on preferences.
     *
     * Example request: POST /api/optimize
     *
     * @return HTTP 200 OK with a message that optimization has started
     */
    @PostMapping("/optimize")
    public ResponseEntity<String> optimize() {
        assignmentService.runOptimizationInNewThread(); // Runs in a background thread
        return ResponseEntity.ok("Optimization started in background.");
    }
}
