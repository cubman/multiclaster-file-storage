package com.dstu.executor;

import com.dstu.server.DataInformation;
import com.dstu.server.Server;
import com.dstu.server.Task;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ClientExecutor implements IExecutor {
    private BufferedReader in;
    private BufferedWriter out;
    private static final int BUFFER_SIZE = 3;
    private Random random = new Random();

    public ClientExecutor(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void execute() {
        try {
            String action = in.readLine();
            String outText = "";

            switch (action) {
                case "SAVE":
                    outText = saveAction();

                    break;
                case "GET":
                    outText = getFile();

                    break;

                case "EXISTS":
                    String fileName = in.readLine();

                    outText = Server.dataStorage.get(fileName) != null
                            ? "Существует\r\nEND\r\n" : "Не существует\n" +
                            "END\n";

                    break;
                default:
                    throw new IllegalArgumentException(action + " unsupported");
            }

            out.write(outText);
            out.flush();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getFile() throws IOException, InterruptedException {
        String fileName = in.readLine();

        List<DataInformation> information = Server.dataStorage.get(fileName);

        if (information == null) {
            return fileName + " не существует\r\n" + Server.END;
        }

        List<Pair<Integer, Task>> savedPlaces = new CopyOnWriteArrayList<>();

        for (DataInformation dataInformation : information) {
            Integer clusterId = dataInformation.getClusterId();
            Task task = new Task(String.format("GT\r\n%s\r\n%d\r\nEND\r\n",
                    fileName, dataInformation.getPart()));

            Server.tasks.get(clusterId).
                    add(task);

            savedPlaces.add(new Pair<>(clusterId, task));
        }

        while (true) {
            long count = savedPlaces.stream().filter(integerTaskPair -> integerTaskPair.getValue().getSolution() == null).count();
            if (count == 0) {
                break;
            }

            TimeUnit.SECONDS.sleep(2);
        }

        StringBuilder result = new StringBuilder();

        for (Pair<Integer, Task> taskPair : savedPlaces) {
            result.append(taskPair.getValue().getSolution());

            Server.tasks.get(taskPair.getKey()).remove(taskPair.getValue());
        }

        result.append(Server.END);

        return result.toString();
    }

    private String saveAction() throws IOException, InterruptedException {
        StringBuilder command = new StringBuilder();

        String fileName = in.readLine();

        Server.dataStorage.put(fileName, new CopyOnWriteArrayList<>());

        while (true) {
            String string = in.readLine();

            if ("END".equals(string)) {
                break;
            }

            command.append(string).append("\t");
        }

        List<Pair<Integer, Task>> savedPlaces = new CopyOnWriteArrayList<>();

        String commandToSplit = command.toString();
        for (int i = 0; i <= command.length() / BUFFER_SIZE; ++i) {
            if (command.length() <= i * BUFFER_SIZE) {
                break;
            }

            Integer clusterId = getRandomClusterId();
            Task task = new Task(String.format("SV\r\n%s\r\n%d\r\n%s\r\nEND\r\n",
                    fileName, i,
                    commandToSplit.substring(i * BUFFER_SIZE, Math.min(command.length(), (i + 1) * BUFFER_SIZE))));

            Server.tasks.get(clusterId).add(task);

            savedPlaces.add(new Pair<>(clusterId, task));

            Server.dataStorage.get(fileName).add(new DataInformation(clusterId, i));
        }

        while (true) {
            long count = savedPlaces.stream().filter(integerTaskPair -> integerTaskPair.getValue().getSolution() == null).count();
            if (count == 0) {
                break;
            }

            TimeUnit.SECONDS.sleep(2);
        }

        StringBuilder result = new StringBuilder();

        for (Pair<Integer, Task> taskPair : savedPlaces) {
            result.append(taskPair.getValue().getSolution());

            Server.tasks.get(taskPair.getKey()).remove(taskPair.getValue());
        }

        result.append(Server.END);

        return result.toString();
    }

    private Integer getRandomClusterId() {
        Set<Integer> integers = Server.tasks.keySet();

        if (integers.size() == 0) {
            System.out.println("Не найдено ни одного кластера");
        }
        int rand = Math.abs(random.nextInt()) % integers.size();

        int count = rand;

        for (Integer integer : integers) {
            if (count == 0) {
                return integer;
            }
            --count;
        }

        throw new IndexOutOfBoundsException("Кластер не был найден");
    }
}
