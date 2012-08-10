package edu.harvard.mcb.leschziner.deploy;

import edu.harvard.mcb.leschziner.manage.GuardianManager;

public class Main {

    private static GuardianManager manager;

    public static void main(String[] args) {
        System.out.println("[Main]: Starting Manager");

        // Spin up a Manager
        manager = new GuardianManager();

        System.out.println("[Main]: Manager Ready");

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("[Main]: Interrupted main, dying");
                System.exit(0);
            }
        }

    }
}
