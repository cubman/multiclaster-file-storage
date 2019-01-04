package com.dstu.server;

import com.dstu.executor.ClientExecutor;
import com.dstu.executor.ClusterExecutor;
import com.dstu.executor.IExecutor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int MAX_THREAD_COUNT = 10;
    private static final int PORT = 8080;

    public static final String END = "END\r\n";

    ServerSocket serverSocket;
    public static final Map<Integer, List<Task>> tasks = new ConcurrentHashMap<>();

    public static final Map<String, List<DataInformation>> dataStorage = new ConcurrentHashMap<>();

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void start() throws IOException {
        ExecutorService service = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

        while (true) {
            service.submit(new Executor(serverSocket.accept()));
        }
    }

    private static class Executor implements Runnable {
        Socket socket;

        public Executor(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {

                String line = in.readLine();

                IExecutor executor;
                if (line.startsWith("CLUSTER")) {
                    Integer clusterId = Integer.parseInt(line.substring("CLUSTER".length()));
                    Server.tasks.putIfAbsent(clusterId, new CopyOnWriteArrayList<>());

                    executor = new ClusterExecutor(clusterId, in, out);
                } else {
                    executor = new ClientExecutor(in, out);
                }
                System.out.println(line);

                executor.execute();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(Thread.currentThread().getId() + " thread dead");
            }
        }
    }
}
