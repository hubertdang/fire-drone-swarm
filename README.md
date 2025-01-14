# Firefighting Drone Swarm

A control system and simulator for a firefighting drone swarm.

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

5. Remember to add helpful comments and format your code. Follow the instructions listed [here](https://www.jetbrains.com/help/idea/reformat-and-rearrange-code.html#reformat_file).

5. Add and commit your changes **locally**.
    ```bash
    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (hubert- issue)
    $ git add src/Hubert.java    # DO THIS COMMAND

    huber@hubert_laptop MINGW64 ~/OneDrive - Carleton University/w2025/SYSC3303/project/FireDroneSwarm (hubert-issue)
    $ git commit -m "Adding Hubert class to solve Hubert issue."    # DO THIS COMMAND
    [hubert-issue 5fd319a] Adding Hubert class to solve Hubert issue.
     1 file changed, 2 insertions(+)
     create mode 100644 src/Hubert.java
    ```

6. Once you're finished making your changes, push them to your remote branch.
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

7. Confirm your remote branch on GitHub has your newest changes.
8. Create your pull request. Follow the instructions listed [here](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request#creating-the-pull-request).
