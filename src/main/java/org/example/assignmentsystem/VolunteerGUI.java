package org.example.assignmentsystem;

import javax.swing.*;
import java.awt.*;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.*;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Swing GUI client for the Volunteer Assignment System.
 * Allows volunteers to submit preferences and trigger optimization,
 * then displays the assignment results fetched from the server.
 */
public class VolunteerGUI extends JFrame {

    private JTextField nameField;                   // Input field for volunteer name
    private JComboBox<String>[] preferenceBoxes;   // Dropdowns for selecting 5 preferences
    private JTextArea outputArea;                   // Text area to show assignment results

    // List of available services for volunteers to select
    private static final String[] SERVICES = {
            "Reception", "Security", "Food", "Transport", "Cleaning",
            "Logistics", "Guiding", "Medical", "Registration", "HelpDesk"
    };

    private final ObjectMapper mapper = new ObjectMapper();  // For JSON serialization/deserialization
    private final HttpClient client = HttpClient.newHttpClient(); // HTTP client to communicate with server

    /**
     * Constructs the GUI, laying out components and setting up event listeners.
     */
    public VolunteerGUI() {
        setTitle("Volunteer Assignment System");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for input fields and buttons
        JPanel inputPanel = new JPanel(new GridLayout(8, 2));

        // Name label and input
        nameField = new JTextField();
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);

        // Create 5 dropdowns for volunteer preferences
        preferenceBoxes = new JComboBox[5];
        for (int i = 0; i < 5; i++) {
            preferenceBoxes[i] = new JComboBox<>(SERVICES);
            inputPanel.add(new JLabel("Preference " + (i + 1) + ":"));
            inputPanel.add(preferenceBoxes[i]);
        }

        // Buttons to submit preferences and trigger optimization
        JButton submitBtn = new JButton("Submit Preferences");
        JButton optimizeBtn = new JButton("Optimize Assignment");

        // Action listeners for buttons
        submitBtn.addActionListener(e -> submitPreferences());
        optimizeBtn.addActionListener(e -> triggerOptimization());

        inputPanel.add(submitBtn);
        inputPanel.add(optimizeBtn);

        // Text area to display assignments
        outputArea = new JTextArea();
        outputArea.setEditable(false);

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    /**
     * Gathers user input and sends volunteer preferences to the backend server.
     * Performs basic validation to ensure name and 5 unique preferences are provided.
     */
    private void submitPreferences() {
        String name = nameField.getText().trim();

        // Use LinkedHashSet to maintain insertion order and uniqueness of preferences
        Set<String> prefs = new LinkedHashSet<>();
        for (JComboBox<String> box : preferenceBoxes) {
            prefs.add((String) box.getSelectedItem());
        }

        // Validate input
        if (name.isEmpty() || prefs.size() < 5) {
            JOptionPane.showMessageDialog(this, "Please enter a name and 5 unique preferences.");
            return;
        }

        // Prepare JSON payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("preferences", prefs);

        try {
            // Build and send POST request to submit preferences
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8066/api/preferences"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            JOptionPane.showMessageDialog(this, "Preferences submitted!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting preferences.");
        }
    }

    /**
     * Sends a request to trigger the optimization process on the server,
     * then fetches and displays the volunteer assignments in a readable format.
     */
    private void triggerOptimization() {
        try {
            // POST request to start optimization
            HttpRequest optimizeRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8066/api/optimize"))
                    .POST(BodyPublishers.noBody())
                    .build();

            client.send(optimizeRequest, HttpResponse.BodyHandlers.ofString());

            // GET request to fetch the current assignments
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8066/api/assignments"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            // Parse JSON response into a list of maps
            List<Map<String, Object>> assignments = mapper.readValue(
                    response.body(),
                    List.class
            );

            // Build formatted string for display
            StringBuilder result = new StringBuilder();
            result.append("📋 Volunteer Assignments:\n\n");

            for (Map<String, Object> assignment : assignments) {
                String serviceName = (String) assignment.get("serviceName");
                List<String> volunteers = (List<String>) assignment.get("assignedVolunteers");

                result.append("📌 ").append(serviceName).append(":\n");

                if (volunteers == null || volunteers.isEmpty()) {
                    result.append("   (No volunteers assigned)\n");
                } else {
                    for (String v : volunteers) {
                        result.append("   - ").append(v).append("\n");
                    }
                }
                result.append("\n");
            }

            // Display assignments in the text area
            outputArea.setText(result.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("Error during optimization.");
        }
    }

    /**
     * Main method to launch the GUI.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VolunteerGUI().setVisible(true));
    }
}
