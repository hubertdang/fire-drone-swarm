import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UISubsystem extends JPanel{

    private static final int MAP_SCALE = 2;

    private static final DroneSubsystem droneSubsystem = new DroneSubsystem();
    private static final SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem();
    private static final FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem();

    private static Thread droneSubsystemThread;
    private static Thread schedulerSubsystemThread;
    private static Thread fireIncidentSubsystemThread;

    private static Border blackline = BorderFactory.createLineBorder(Color.black);

    private static JFrame configFrame;
    private static JFrame simulationFrame;
    private static JPanel mapPanel;

    public static void setConfigFrame() {
        // Create the configuration window
        configFrame = new JFrame("Configuration");
        configFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        configFrame.setSize(300, 200);

        // Create a button and add it to the configuration window
        JButton configButton = new JButton("Run Simulation");
        configFrame.getContentPane().add(configButton);

        fireIncidentSubsystem.readSimZoneFile(new File("./sample_input_files/zones.csv"));
        fireIncidentSubsystem.readSimEventFile(new File("./sample_input_files/events.csv"));

        // Add an ActionListener to the button
        configButton.addActionListener(e -> {
            try {
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

    public static void setSimulationFrame() throws IOException {
        configFrame.dispose();

        droneSubsystemThread.start();
        schedulerSubsystemThread.start();
        fireIncidentSubsystemThread.start();

        simulationFrame = new JFrame("Simulation");
        simulationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        simulationFrame.setSize(800, 600);

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
            zonePanel.setBounds(x - 10, y - 10, 20 , 20);
            zonePanel.setBackground(Color.BLACK);
            zonePanel.setBorder(blackline);
            zonePanel.setVisible(true);
            mapPanel.add(zonePanel);
        }
    }

}
