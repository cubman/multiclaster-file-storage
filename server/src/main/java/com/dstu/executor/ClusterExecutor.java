package com.dstu.executor;

import com.dstu.server.Server;
import com.dstu.server.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterExecutor implements IExecutor {

    private BufferedReader in;
    private BufferedWriter out;
    private Integer clusterId;

    public ClusterExecutor(Integer id, BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
        this.clusterId = id;
    }

    @Override
    public void execute() {
        while (true) {
            List<Task> tasks = Server.tasks.get(clusterId)
                    .stream().filter(task -> task.getSolution() == null)
                    .collect(Collectors.toList());

            if (!tasks.isEmpty()) {
                try {
                    Task task = tasks.get(0);
                    out.write(task.getCommand());
                    out.flush();

                    task.setSolution(getAnswer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getAnswer() throws IOException {
        StringBuilder resLine = new StringBuilder();

        while (true) {
            String line = in.readLine();

            if ("END".equals(line)) {
                return resLine.toString();
            }

            resLine.append(line + "\r\n");
        }
    }
}
