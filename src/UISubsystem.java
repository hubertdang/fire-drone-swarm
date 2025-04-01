import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class UISubsystem {
    private static final DroneSubsystem droneSubsystem = new DroneSubsystem();
    private static final SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem();
    private static final FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem();

    private static Thread droneSubsystemThread;
    private static Thread schedulerSubsystemThread;
    private static Thread fireIncidentSubsystemThread;

    private static Border blackline = BorderFactory.createLineBorder(Color.black);

    private static JFrame configFrame;
    private static JFrame simulationFrame;

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
        JPanel panel = new JPanel();
        panel.add(new JLabel("Map UI"));
        panel.setBorder(blackline);
        simulationFrame.getContentPane().add(panel, BorderLayout.WEST);
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
}
