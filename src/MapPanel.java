import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MapPanel extends JPanel {
    private FireIncidentSubsystem fireIncidentSubsystem;
    private DroneSubsystem droneSubSystem;

    public MapPanel(FireIncidentSubsystem fireIncidentSubsystem, DroneSubsystem droneSubSystem) {
        this.fireIncidentSubsystem = fireIncidentSubsystem;
        this.droneSubSystem = droneSubSystem;
        setLayout(null);
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.WHITE);
        Timer timer = new Timer(500, e -> repaint());
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for(Zone zone : fireIncidentSubsystem.getFireZones().values()){
            //updateZone(g, zone);

        }
    }

    private void updateZone(Zone zone){
        int[] coors = zone.getZoneCoordinates();
        //g.setColor(Color.RED);
        //g.fillRect(coors[0],coors[1],coors[2]-coors[0],coors[3]-coors[1]);

    }

}
