/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.rSybl.dataProcessingUnit.api.model;

import java.util.HashMap;

/**
 *
 * @author Georgiana
 */
public class MonitoringSnapshot implements Comparable<MonitoringSnapshot> {
    private HashMap<String, String> ongoingActions=new HashMap<>();
    private HashMap<String, ServicePartMonitor> serviceParts = new HashMap<>();
    private long timestamp;
     /**
     * @param ongoingActions the ongoingActions to set
     */
    public void setOngoingActions(HashMap<String, String> ongoingActions) {
        this.ongoingActions = ongoingActions;
    }
/**
     * @param ongoingActions the ongoingActions to set
     */
    public void addOngoingActions(String servicePart, String action) {
       ongoingActions.put(servicePart, action);
    }
        /**
     * @return the ongoingActions
     */
    public HashMap<String, String> getOngoingActions() {
        return ongoingActions;
    }

    /**
     * @return the serviceParts
     */
    public HashMap<String, ServicePartMonitor> getServiceParts() {
        return serviceParts;
    }

    /**
     * @param serviceParts the serviceParts to set
     */
    public void setServiceParts(HashMap<String, ServicePartMonitor> serviceParts) {
        this.serviceParts = serviceParts;
    }
    public void addServicePart(String servicePart, ServicePartMonitor monitor){
        serviceParts.put(servicePart, monitor);
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(MonitoringSnapshot o) {
        if (timestamp>o.timestamp) {
            return 1;
        }
        if (timestamp<o.timestamp) {
            return -1;
        }
        return 0;
    }
    
}
