import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UISubsystem extends JPanel {

    private static final int MAP_SCALE = 4;
    private static final int BASE = 300;

    private static final DroneSubsystem droneSubsystem = new DroneSubsystem();
    private static final SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem();
    private static final FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem();

    private static Thread droneSubsystemThread;
    private static Thread schedulerSubsystemThread;
    private static Thread fireIncidentSubsystemThread;

    private static Border blackline = BorderFactory.createLineBorder(Color.black);

    private static JFrame configFrame;
    private static JTextField dronesField;
    private static JTextField agentCapacityField;
    private static JTextField maxSpeedField;
    private static JButton startButton;
    private static JLabel zoneFilePathLabel;
    private static JLabel eventsFilePathLabel;
    private static JFrame simulationFrame;
    private static JPanel mapPanel;
    private static File zoneFile;
    private static File eventsFile;

    public static void setConfigFrame() {
        // Create the configuration window
        configFrame = new JFrame("System Configuration");
        configFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        configFrame.setSize(500, 250);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Labels and text fields
        JPanel dronePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dronesLabel = new JLabel("Num of Drones:");
        dronesField = new JTextField(10);
        dronePanel.add(dronesLabel);
        dronePanel.add(dronesField);

        JPanel agentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel agentCapacityLabel = new JLabel("Agent Capacity:");
        agentCapacityField = new JTextField(10);
        agentPanel.add(agentCapacityLabel);
        agentPanel.add(agentCapacityField);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel maxSpeedLabel = new JLabel("Drone Max Speed:");
        maxSpeedField = new JTextField(10);
        speedPanel.add(maxSpeedLabel);
        speedPanel.add(maxSpeedField);

        // Zone and events file upload
        JPanel zoneFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel zoneFileUploadLabel = new JLabel("Upload Zones File:");
        JButton zoneFileUploadButton = new JButton("Choose File");
        zoneFilePathLabel = new JLabel("Zones:");
        zoneFileUploadButton.addActionListener(e -> chooseFile(zoneFilePathLabel));
        zoneFilePanel.add(zoneFileUploadLabel);
        zoneFilePanel.add(zoneFileUploadButton);
        zoneFilePanel.add(zoneFilePathLabel);

        JPanel eventsFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel eventsFileUploadLabel = new JLabel("Upload Events File:");
        JButton eventsFileUploadButton = new JButton("Choose File");
        eventsFilePathLabel = new JLabel("Events:");
        eventsFileUploadButton.addActionListener(e -> chooseFile(eventsFilePathLabel));
        eventsFilePanel.add(eventsFileUploadLabel);
        eventsFilePanel.add(eventsFileUploadButton);
        eventsFilePanel.add(eventsFilePathLabel);

        // Start button panel
        JPanel startBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startButton = new JButton("Run Simulation");
        startBtnPanel.add(startButton);

        panel.add(dronePanel);
        panel.add(agentPanel);
        panel.add(speedPanel);
        panel.add(zoneFilePanel);
        panel.add(eventsFilePanel);
        panel.add(startBtnPanel);

        configFrame.getContentPane().add(panel);

        // Add an ActionListener to the button
        startButton.addActionListener(e -> {
            try {
                if (zoneFilePathLabel.getText().equals("Zones:")) {
                    fireIncidentSubsystem.readSimZoneFile(new File("./sample_input_files/zones.csv"));
                }
                if (eventsFilePathLabel.getText().equals("Events:")) {
                    fireIncidentSubsystem.readSimEventFile(new File("./sample_input_files/events.csv"));
                }
                String drones = dronesField.getText();
                DroneSubsystem.setNumberOfDrones(Integer.parseInt(drones));

                String agentCapacity = agentCapacityField.getText();
                AgentTank.setCapacity(Float.parseFloat(agentCapacity));

                String maxSpeed = maxSpeedField.getText();
                Drone.setTopSpeed(Float.parseFloat(maxSpeed));

                setSimulationFrame();
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Display the configuration window
        configFrame.setLocationRelativeTo(null); // Center the window
        configFrame.setVisible(true);
    }

    /**
     * Method to choose a file and update the label text.
     *
     * @param label The label to update
     */
    private static void chooseFile(JLabel label) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (label.getText().contains("Zones:")) {
                File selectedFile = fileChooser.getSelectedFile();
                fireIncidentSubsystem.readSimZoneFile(selectedFile);
                label.setText("Zones: " + selectedFile.getName());
            }
            else {
                File selectedFile = fileChooser.getSelectedFile();
                fireIncidentSubsystem.readSimEventFile(selectedFile);
                label.setText("Events: " + selectedFile.getName());
            }

        }
    }

    public static void setSimulationFrame() throws IOException {
        configFrame.dispose();

        droneSubsystemThread.start();
        schedulerSubsystemThread.start();
        fireIncidentSubsystemThread.start();

        simulationFrame = new JFrame("Simulation");
        simulationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        simulationFrame.setSize(1200, 900);

        // Optionally, set a layout manager (BorderLayout is the default)
        simulationFrame.setLayout(new BorderLayout());
        simulationFrame.setLocationRelativeTo(null); // Center the window
        simulationFrame.setVisible(true);

        addMap();
        addLegend();
        addStats();

        simulationFrame.revalidate();
        simulationFrame.repaint();
    }

    public static void addMap() {
        mapPanel = new JPanel();
        mapPanel.setLayout(null); // absolute positioning
        mapPanel.setBorder(BorderFactory.createTitledBorder("MapUI"));
        mapPanel.setPreferredSize(new Dimension(900, 900));
        mapPanel.setSize(900, 900);
        mapPanel.setVisible(true);

        simulationFrame.getContentPane().add(mapPanel, BorderLayout.CENTER);
        simulationFrame.revalidate();
        simulationFrame.repaint();

        // 🔁 Timer for refreshing zone panels every second
        Timer timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Remove all zone panels (but not the label)
                Component[] components = mapPanel.getComponents();
                for (int i = components.length - 1; i >= 0; i--) {
                    if (components[i] instanceof JPanel && components[i] != mapPanel) {
                        mapPanel.remove(components[i]);
                    }
                }

                JPanel basePanel = new JPanel();
                basePanel.add(new JLabel("BASE" ));
                basePanel.setBounds(BASE - 50, BASE - 50, 100, 100);
                basePanel.setBackground(new Color(20, 20, 20, 73));
                basePanel.setBorder(blackline);
                basePanel.setVisible(true);
                mapPanel.add(basePanel);

                updateDronesMap();
                updateZonesMap();

                mapPanel.revalidate();
                mapPanel.repaint();
            }
        });

        timer.start();
    }

    public static void addLegend() {
        JPanel legendPanel = new JPanel(new GridLayout(2, 1)); // Two columns side by side
        legendPanel.setBorder(blackline);

        // 🔥 Fire Severity Legend
        JPanel fireLegend = new JPanel();
        fireLegend.setLayout(new BoxLayout(fireLegend, BoxLayout.X_AXIS));
        fireLegend.setBorder(BorderFactory.createTitledBorder("Fire Severity"));
        fireLegend.add(makeLegendItem(new Color(255, 0, 0, 50), "HIGH"));
        fireLegend.add(makeLegendItem(new Color(255, 165, 0, 50), "MODERATE"));
        fireLegend.add(makeLegendItem(new Color(255, 255, 0, 50), "LOW"));
        fireLegend.add(makeLegendItem(new Color(0, 128, 0, 50), "CLEAR"));

        // ✈️ Drone State Legend
        JPanel droneLegend = new JPanel();
        droneLegend.setLayout(new BoxLayout(droneLegend, BoxLayout.X_AXIS));
        droneLegend.setBorder(BorderFactory.createTitledBorder("Drone States"));
        droneLegend.add(makeLegendItem(Color.GRAY, "BASE"));
        droneLegend.add(makeLegendItem(new Color(245, 137, 227, 255), "TAKEOFF"));
        droneLegend.add(makeLegendItem(new Color(211, 40, 208, 255), "ACCELERATING"));
        droneLegend.add(makeLegendItem(new Color(208, 0, 255, 255), "FLYING"));
        droneLegend.add(makeLegendItem(new Color(179, 0, 255, 255), "DECELERATING"));
        droneLegend.add(makeLegendItem(new Color(153, 0, 255, 225), "LANDING"));
        droneLegend.add(makeLegendItem(new Color(152, 208, 119, 225), "ARRIVED"));
        droneLegend.add(makeLegendItem(new Color(43, 190, 255, 225), "RELEASING_AGENT"));
        droneLegend.add(makeLegendItem(new Color(255, 57, 57, 225), "FAULT"));
        droneLegend.add(makeLegendItem(new Color(255, 222, 8, 225), "IDLE"));

        // Add both to main panel
        legendPanel.add(fireLegend);
        legendPanel.add(droneLegend);

        // Add to frame
        simulationFrame.getContentPane().add(legendPanel, BorderLayout.SOUTH);
    }

    public static void addStats() {
        JPanel panel = new JPanel();
        mapPanel.setPreferredSize(new Dimension(500, 900));
        mapPanel.setSize(500, 900);
        panel.setBorder(blackline);
        simulationFrame.getContentPane().add(panel, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        droneSubsystemThread = new Thread(droneSubsystem, "DroneSubsystem");
        schedulerSubsystemThread = new Thread(schedulerSubsystem, "SchedulerSubsystem");
        fireIncidentSubsystemThread = new Thread(fireIncidentSubsystem, "FireIncidentSubsystem");

        setConfigFrame();
    }

    private static JPanel makeLegendItem(Color color, String label) {
        JPanel itemPanel = new JPanel(new GridLayout(2, 1));

        JLabel colorBox = new JLabel();
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel textLabel = new JLabel(label);

        itemPanel.add(colorBox);
        //itemPanel.add(Box.createHorizontalStrut(5)); // spacing
        itemPanel.add(textLabel);

        return itemPanel;
    }

    private static void updateZonesMap() {
        HashMap<Integer, Zone> fireZones = fireIncidentSubsystem.getFireZones();
        for (Zone zone : fireZones.values()) {
            int[] zc = zone.getZoneCoordinates();

            JPanel zonePanel = new JPanel();
            zonePanel.add(new JLabel("Zone#" + zone.getId()));
            zonePanel.setBounds((zc[0] / MAP_SCALE) + BASE, (zc[1] / MAP_SCALE) + BASE, (zc[2] - zc[0]) / MAP_SCALE, (zc[3] - zc[1]) / MAP_SCALE);
            zonePanel.setBackground(zone.getZoneColor());
            zonePanel.setBorder(blackline);
            zonePanel.setVisible(true);
            mapPanel.add(zonePanel);
        }

        HashMap<Integer, Zone> clearZones = fireIncidentSubsystem.getClearZones();
        for (Zone zone : clearZones.values()) {
            int[] zc = zone.getZoneCoordinates();

            JPanel zonePanel = new JPanel();
            zonePanel.add(new JLabel("Zone#" + zone.getId()));
            zonePanel.setBounds((zc[0] / MAP_SCALE) + BASE, (zc[1] / MAP_SCALE) + BASE, (zc[2] - zc[0]) / MAP_SCALE, (zc[3] - zc[1]) / MAP_SCALE);
            zonePanel.setBackground(zone.getZoneColor());
            zonePanel.setBorder(blackline);
            zonePanel.setVisible(true);
            mapPanel.add(zonePanel);
        }
    }

    private static void updateDronesMap() {
        // Re-add updated drone panels
        HashMap<Integer, Drone> allDrones = DroneSubsystem.getAllDrones();
        for (Drone drone : allDrones.values()) {
            int x = (int) drone.getPosition().getX() / MAP_SCALE;
            int y = (int) drone.getPosition().getY() / MAP_SCALE;

            JPanel zonePanel = new JPanel();
            zonePanel.add(new JLabel("D#" + drone.getId()));
            zonePanel.setBounds(x - 15 + BASE, y - 15 + BASE, 30 , 30);
            zonePanel.setBackground(drone.getStateColor());
            zonePanel.setBorder(blackline);
            zonePanel.setVisible(true);
            mapPanel.add(zonePanel);
        }
    }

}
