package org.example.assignmentsystem;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an assignment of volunteers to a particular service.
 * Contains the service name and the list of assigned volunteer names.
 */
@Getter
@Setter
public class ServiceAssignment {

    /**
     * Name of the service (e.g., Reception, Security).
     */
    private String serviceName;

    /**
     * List of volunteer names assigned to this service.
     */
    private List<String> assignedVolunteers;

    /**
     * Default constructor initializing an empty list of volunteers.
     */
    public ServiceAssignment() {
        this.assignedVolunteers = new ArrayList<>();
    }

    /**
     * Constructor to create a service assignment with a name and no volunteers.
     * @param serviceName the name of the service
     */
    public ServiceAssignment(String serviceName) {
        this.serviceName = serviceName;
        this.assignedVolunteers = new ArrayList<>();
    }

    /**
     * Constructor to create a service assignment with a name and a list of volunteers.
     * @param serviceName the name of the service
     * @param assignedVolunteers the list of volunteer names assigned
     */
    public ServiceAssignment(String serviceName, List<String> assignedVolunteers) {
        this.serviceName = serviceName;
        this.assignedVolunteers = assignedVolunteers;
    }
}
