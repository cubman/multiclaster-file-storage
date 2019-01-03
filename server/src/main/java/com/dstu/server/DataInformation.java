package com.dstu.server;

public class DataInformation {
    private Integer clusterId;
    private int part;

    public DataInformation(Integer clusterId, int part) {
        this.clusterId = clusterId;
        this.part = part;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public int getPart() {
        return part;
    }
}
