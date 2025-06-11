package org.example.assignmentsystem;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a volunteer with a name and a ranked list of preferred services.
 */
@Getter
@Setter
public class Volunteer {

    /**
     * The volunteer's full name.
     */
    private String name;

    /**
     * List of up to 5 preferred service names, ranked by preference order.
     */
    private List<String> preferences;

    /**
     * Default constructor required for JSON serialization/deserialization.
     */
    public Volunteer() {}

    /**
     * Constructs a volunteer with a given name and list of preferences.
     *
     * @param name the volunteer's name
     * @param preferences list of preferred services, ranked
     */
    public Volunteer(String name, List<String> preferences) {
        this.name = name;
        this.preferences = preferences;
    }
}
