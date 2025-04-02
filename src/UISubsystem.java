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

    private static final int MAP_SCALE = 2;

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
        startButton.setEnabled(false);
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
            File selectedFile = fileChooser.getSelectedFile();
            if (label.getText().contains("Zones:")) {
                fireIncidentSubsystem.readSimZoneFile(selectedFile);
                label.setText("Zones: " + selectedFile.getName());
            }
            else {
                fireIncidentSubsystem.readSimEventFile(selectedFile);
                label.setText("Events: " + selectedFile.getName());
            }
            checkIfReadyToRun();

        }
    }

    /**
     * Method to check if all fields are filled and files are uploaded.
     */
    private static void checkIfReadyToRun() {
        boolean fileUploaded = !zoneFilePathLabel.getText().equals("Zones: ") &&
                !eventsFilePathLabel.getText().equals("Events: ");
        boolean allFieldsFilled = !dronesField.getText().trim().isEmpty() &&
                !agentCapacityField.getText().trim().isEmpty() &&
                !maxSpeedField.getText().trim().isEmpty();

        startButton.setEnabled(fileUploaded && allFieldsFilled);
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
        mapPanel.setBorder(blackline);
        mapPanel.setPreferredSize(new Dimension(900, 900));
        mapPanel.setSize(900, 900);
        mapPanel.setVisible(true);

        JLabel mapLabel = new JLabel("Map UI");
        mapLabel.setBounds(10, 10, 100, 20);
        mapPanel.add(mapLabel);

        simulationFrame.getContentPane().add(mapPanel, BorderLayout.WEST);
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

                updateDronesMap();
                updateZonesMap();

                mapPanel.revalidate();
                mapPanel.repaint();
            }
        });

        timer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.red);
    }

    public static void addLegend() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Legend"));
        panel.setBorder(blackline);
        simulationFrame.getContentPane().add(panel, BorderLayout.SOUTH);
    }

    public static void addStats() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Stats UI"));
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

    private static void updateZonesMap() {
        HashMap<Integer, Zone> fireZones = fireIncidentSubsystem.getFireZones();
        for (Zone zone : fireZones.values()) {
            int[] zc = zone.getZoneCoordinates();

            JPanel zonePanel = new JPanel();
            zonePanel.add(new JLabel("Zone#" + zone.getId()));
            zonePanel.setBounds(zc[0] / MAP_SCALE, zc[1] / MAP_SCALE, (zc[2] - zc[0]) / MAP_SCALE, (zc[3] - zc[1]) / MAP_SCALE);
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
            zonePanel.setBounds(zc[0] / MAP_SCALE, zc[1] / MAP_SCALE, (zc[2] - zc[0]) / MAP_SCALE, (zc[3] - zc[1]) / MAP_SCALE);
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
            zonePanel.setBounds(x - 10, y - 10, 20, 20);
            zonePanel.setBackground(Color.BLACK);
            zonePanel.setBorder(blackline);
            zonePanel.setVisible(true);
            mapPanel.add(zonePanel);
        }
    }

}
