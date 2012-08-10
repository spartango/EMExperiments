package edu.harvard.mcb.leschziner.remote;

import com.hazelcast.core.Hazelcast;

public class RemoteMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("[RemoteMain]: Connecting to cluster");
        System.out.println("[RemoteMain]: Cluster Members -> "
                           + Hazelcast.getCluster().getMembers().toString());
        System.out.println("[RemoteMain]: Ready");
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
