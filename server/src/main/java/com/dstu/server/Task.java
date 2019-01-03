package com.dstu.server;

public class Task {
    private String command;
    private String solution;

    public Task(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}
