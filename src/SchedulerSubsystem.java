public class SchedulerSubsystem implements Runnable{
    @Override
    public void run() {
        Scheduler scheduler = new Scheduler();

        FireEventHandler fireEventHandler = new FireEventHandler(7000, scheduler);
        DroneRequestHandler droneRequestHandler = new DroneRequestHandler(7001, scheduler);

        Thread fireEventHandlerThread = new Thread(fireEventHandler, "📅FEH");
        Thread droneRequestHandlerThread = new Thread(droneRequestHandler, "📅DRH");


        fireEventHandlerThread.start();
        droneRequestHandlerThread.start();
    }
}
