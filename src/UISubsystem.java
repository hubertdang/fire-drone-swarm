import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
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

    private static JTable droneTable;
    private static Object[][] droneTableData;
    private static JTable fireTable;
    private static DefaultTableModel fireTableModel;
    private static ArrayList<FireRow> fireRows = new ArrayList<>();

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
        simulationFrame.setSize(1600, 1000);

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

                updateDronesMap();

                updateZonesMap();

                JPanel basePanel = new JPanel();
                basePanel.add(new JLabel("BASE" ));
                basePanel.setBounds(BASE - 30, BASE - 30, 60, 60);
                basePanel.setBackground(new Color(20, 20, 20, 73));
                basePanel.setBorder(blackline);
                basePanel.setVisible(true);
                mapPanel.add(basePanel);



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
        droneLegend.add(makeLegendItem(new Color(5, 87, 138, 225), "EMPTY_TANK"));
        droneLegend.add(makeLegendItem(new Color(255, 57, 57, 225), "FAULT"));
        droneLegend.add(makeLegendItem(new Color(255, 222, 8, 225), "IDLE"));

        // Add both to main panel
        legendPanel.add(fireLegend);
        legendPanel.add(droneLegend);

        // Add to frame
        simulationFrame.getContentPane().add(legendPanel, BorderLayout.SOUTH);
    }

    public static void addStats() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Stats"));

        statsPanel.add(createDroneTablePanel());
        statsPanel.add(createFireTablePanel());

        simulationFrame.getContentPane().add(statsPanel, BorderLayout.EAST);

        Timer statsTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDroneTableData();
                updateFireTableData();
            }
        });
        statsTimer.start();
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

    private static JPanel createDroneTablePanel() {
        String[] columnNames = {"Drone ID", "Zone ID", "State", "Agent Left (L)"};

        droneTableData = new Object[DroneSubsystem.getAllDrones().size()][4];
        updateDroneTableData(); // Fill data

        droneTable = new JTable(droneTableData, columnNames);

        droneTable.getColumnModel().getColumn(2).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object stateValue = table.getValueAt(row, 2);
            if (stateValue instanceof DroneStateID) {
                int droneId = (int) table.getValueAt(row, 0);
                Drone drone = DroneSubsystem.getAllDrones().get(droneId);
                if (drone != null) {
                    c.setBackground(drone.getStateColor());
                    c.setForeground(Color.BLACK);
                }
            }
            return c;
        });

        JScrollPane scrollPane = new JScrollPane(droneTable);

        JPanel dronePanel = new JPanel(new BorderLayout());
        dronePanel.setBorder(BorderFactory.createTitledBorder("Drones"));
        dronePanel.add(scrollPane, BorderLayout.CENTER);

        return dronePanel;
    }

    private static void updateDroneTableData() {
        HashMap<Integer, Drone> allDrones = DroneSubsystem.getAllDrones();
        int i = 0;
        for (Drone drone : allDrones.values()) {
            if (i >= droneTableData.length) break; // prevent index overflow if drone count changes
            droneTableData[i][0] = drone.getId();
            droneTableData[i][1] = (drone.getZoneToService() != null) ? drone.getZoneToService().getId() : "None";
            droneTableData[i][2] = drone.getCurrStateID();
            droneTableData[i][3] = String.format("%.2f", drone.getAgentTankAmount());
            i++;
        }

        if (droneTable != null) droneTable.repaint();
    }

    private static JPanel createFireTablePanel() {
        String[] columns = {"Fire ID", "Zone ID", "Severity", "Appeared At", "Time Taken"};

        fireTableModel = new DefaultTableModel(columns, 0);
        fireTable = new JTable(fireTableModel);

        JScrollPane scrollPane = new JScrollPane(fireTable);
        JPanel firePanel = new JPanel(new BorderLayout());
        firePanel.setBorder(BorderFactory.createTitledBorder("Fires"));
        firePanel.add(scrollPane, BorderLayout.CENTER);

        return firePanel;
    }

    private static class FireRow {
        int zoneId;
        FireSeverity severity;
        long appearedAtMillis;
        Long extinguishedTime = null; // millis since appearance

        public Object[] toTableRow() {
            String formattedTime = TimeUtils.millisecondsToTimestamp(appearedAtMillis);
            return new Object[] {
                    zoneId,
                    zoneId,
                    severity.toString(),
                    formattedTime,
                    (extinguishedTime != null) ? extinguishedTime + "s" : "-"
            };
        }
    }

    private static void updateFireTableData() {
        HashMap<Integer, Zone> fireZones = fireIncidentSubsystem.getFireZones();
        HashMap<Integer, Zone> clearZones = fireIncidentSubsystem.getClearZones();

        // add new fires
        for (Zone zone : fireZones.values()) {
            boolean exists = fireRows.stream().anyMatch(row -> row.zoneId == zone.getId());
            if (!exists) {
                FireRow row = new FireRow();
                row.zoneId = zone.getId();
                row.severity = zone.getSeverity();
                row.appearedAtMillis = TimeUtils.getCurrentTime(); // store raw ms
                fireRows.add(row);
            }
        }

        // update extinguished times
        for (Zone zone : clearZones.values()) {
            for (FireRow row : fireRows) {
                if (row.zoneId == zone.getId() && row.extinguishedTime == null) {
                    long now = TimeUtils.getCurrentTime();
                    row.extinguishedTime = (now - row.appearedAtMillis) / 1000; // in seconds
                }
            }
        }

        // Update table view
        fireTableModel.setRowCount(0); // clear
        for (FireRow row : fireRows) {
            fireTableModel.addRow(row.toTableRow());
        }
    }

}