# Firefighting Drone Swarm

A control system and simulator for a firefighting drone swarm.

## 📂 Project Structure

| **File**                         | **Description**                                                                                 |
|----------------------------------|-------------------------------------------------------------------------------------------------|
| `FireIncidentSubsystem.java`     | Handles fire events and drone requests, communicating with `Scheduler`                          |
| `FireIncidentBuffer.java`        | Represents the buffer used between the fire incident subsystem and the scheduler.               |
| `Scheduler.java`                 | Processes fire incidents and drone requests.                                                    |
| `MissionQueue.java`              | Represents the queue of missions for the drones.                                                |
| `SimEvent.java`                  | Represents a fire event message.                                                                |
| `Task.java`                      | Represents a task used to aid in the generation of messages passed between scheduler and drone. |
| `Drone.java`                     | Represents a drone in the simulation.                                                           |
| `DroneBuffer.java`               | Represents the buffer used between drone adn scheduler.                                         |
| `DroneStatus.java`               | Enum defining the possible statuses the drone can be in.                                        |
| `Zone.java`                      | Represents a zone, including position and severity of any fire occurring there.                 |
| `AgentTank.java`                 | Represents the tank of the drone.                                                               |
| `Position.java`                  | Represents x, y coordinates of a zone.                                                          |
| `FireSeverity.java`              | Enum defining `NO_FIRE`, `LOW`, `MODERATE`, and `HIGH` severity levels.                         |
| `Main.java`                      | Entry point for running the simulation.                                                         |
| `FireIncidentSubsystemTest.java` | Unit tests for `FireIncidentSubsystem`.                                                         |
| `SchedulerTest.java`             | Unit tests for `Scheduler`.                                                                     |
| `ZoneTest.java`                  | Unit tests for `Zone`.                                                                          |
| `AgentTankTest.java`             | Unit tests for `AgentTank`.                                                                     |
| `PositionTest.java`              | Unit tests for `Position`.                                                                      |
| `sample_input_files/zones.csv`   | Sample test file containing zone definitions.                                                   |
| `sample_input_files/events.csv`  | Sample test file containing fire incident events.                                               |

## ⚙️ Setup Instructions

### **Prerequisites**
- Java 11+ installed.
- IntelliJ IDEA or any Java IDE.
- Ensure that the following test files exist in sample_input_files/: `zones.csv`, `events.csv`.

### **1. Clone the Repository**
```sh
git clone https://github.com/hubertdang/fire-drone-swarm.git `
cd fire-drone-swarm
```
### **2. Open the Project in IntelliJ IDEA**
1. Open IntelliJ IDEA.
2. Click on `Open`.
3. Compile and run `Main.java` inside your IDE.
4. To run unit tests, click on unit test file and run the test

## Developer Workflow

1. Checkout the project in IntelliJ. Follow the instructions listed [here](https://www.jetbrains.com/help/idea/manage-projects-hosted-on-github.html#clone-from-GitHub).

2. Create a branch to work on an issue. Follow the instructions listed [here](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/creating-a-branch-for-an-issue).

3. Checkout your new remote branch that you just created in step 2.
    ```bash
    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (master)
    $ git fetch    # DO THIS COMMAND
    From https://github.com/hubertdang/fire-drone-swarm
     * [new branch]      hubert-issue -> origin/hubert-issue

    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (master)
    $ git checkout hubert-issue    # DO THIS COMMAND
    Switched to a new branch 'hubert-issue'
    branch 'hubert-issue' set up to track 'origin/hubert-issue'.
    ```

4. Make your code changes **locally**.

5. Remember to add helpful comments and format your code. Try to adhere to [Java Code Conventions](https://www.oracle.com/docs/tech/java/codeconventions.pdf) and follow the instructions listed [here](https://www.jetbrains.com/help/idea/reformat-and-rearrange-code.html#reformat_file).

6. Verify that any additionally required tests have been added and that all tests pass.

7. Add and commit your changes **locally**.
    ```bash
    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (hubert- issue)
    $ git add src/Hubert.java    # DO THIS COMMAND

    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (hubert-issue)
    $ git commit -m "Adding Hubert class to solve Hubert issue."    # DO THIS COMMAND
    [hubert-issue 5fd319a] Adding Hubert class to solve Hubert issue.
     1 file changed, 2 insertions(+)
     create mode 100644 src/Hubert.java
    ```

8. Once you're finished making your changes, push them to your remote branch.
    ```bash
    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (hubert-issue)
    $ git push    # DO THIS COMMAND
    Enumerating objects: 5, done.
    Counting objects: 100% (5/5), done.
    Delta compression using up to 8 threads
    Compressing objects: 100% (2/2), done.
    Writing objects: 100% (4/4), 357 bytes | 357.00 KiB/s, done.
    Total 4 (delta 1), reused 0 (delta 0), pack-reused 0
    remote: Resolving deltas: 100% (1/1), completed with 1 local object.
    To https://github.com/hubertdang/fire-drone-swarm.git
       406eb44..5fd319a  hubert-issue -> hubert-issue
    ```

9. Confirm your remote branch on GitHub has your newest changes.
10. Create your pull request. Follow the instructions listed [here](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request#creating-the-pull-request).
11. Once your pull request is ready to merge, please **squash and merge** to limit the number of commits to master.
![image](https://github.com/user-attachments/assets/e829f914-5a12-4ec0-a8e7-1ee3f6358397)

## Breakdown of Responsibilities
In general, the team approached the project collaboratively, working together to solve problems and make decisions. However, each team member was responsible for specific tasks and components of the project. 
The breakdown of responsibilities is as follows:
- Amilesh: `FireIncidentSubsystem`, `FireIncidentSubsystemTest`, `SimEvent`
- Hubert: UML Diagrams
- David: UML Diagrams, `Task`, `DroneBuffer`, `FireIncidentBuffer`
- Aashna: `Zone`, `ZoneTest`, `Position`, `PositionTest`, `AgentTank`, `AgentTankTest`
- Shenhao Gong: `Drone`, `DroneStatus`, `FireSeverity`
- Manit: `Scheduler`, `MissionQueue`, `SchedulerTest`

