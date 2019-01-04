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
            String outText;

            switch (action) {
                case "SAVE":
                    outText = saveAction();

                    break;
                case "GET":
                    outText = getFile();

                    break;

                case "EXISTS":
                    String fileName = in.readLine();

                    outText = fileName + (fileExists(fileName)
                            ? " существует\r\nEND\r\n" : " не существует\n" +
                            "END\n");

                    break;

                case "DELETE":
                    outText = deleteFile();
                    break;

                case "REPLACE":
                    outText = replaceFragment();
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

        if (!fileExists(fileName)) {
            return fileName + " не существует\r\n" + Server.END;
        }

        List<Pair<Integer, Task>> savedPlaces = new CopyOnWriteArrayList<>();

        for (DataInformation dataInformation : Server.dataStorage.get(fileName)) {
            Integer clusterId = dataInformation.getClusterId();
            Task task = new Task(String.format("GT\r\n%s\r\n%d\r\nEND\r\n",
                    fileName, dataInformation.getPart()));

            Server.tasks.get(clusterId).
                    add(task);

            savedPlaces.add(new Pair<>(clusterId, task));
        }

        String result = getResult(savedPlaces);
        return result.substring(0, result.length() - Server.END.length())
                .replace("\r\n", "")
                .replace("\t", "\n")+ "\r\n" + Server.END;
    }

    private String saveAction() throws IOException, InterruptedException {
        String fileName = in.readLine();

        if (fileExists(fileName)) {
            return "Файл существует, чтобы сохранить, надо удалить!\r\n" + Server.END;
        }

        Server.dataStorage.put(fileName, new CopyOnWriteArrayList<>());

        StringBuilder command = new StringBuilder();

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

        return getResult(savedPlaces);
    }

    private Integer getRandomClusterId() {
        Set<Integer> integers = Server.tasks.keySet();

        if (integers.size() == 0) {
            System.out.println("Не найдено ни одного кластера");
        }
        int count = Math.abs(random.nextInt()) % integers.size();

        for (Integer integer : integers) {
            if (count == 0) {
                return integer;
            }
            --count;
        }

        throw new IndexOutOfBoundsException("Кластер не был найден");
    }

    private boolean fileExists(String fileName) {
        return Server.dataStorage.get(fileName) != null;
    }

    private String getResult(List<Pair<Integer, Task>> savedPlaces) throws InterruptedException {
        while (true) {
            long count = savedPlaces.stream().filter(integerTaskPair -> integerTaskPair.getValue().getSolution() == null).count();
            if (count == 0) {
                break;
            }

            TimeUnit.SECONDS.sleep(1);
        }

        StringBuilder result = new StringBuilder();

        for (Pair<Integer, Task> taskPair : savedPlaces) {
            result.append(taskPair.getValue().getSolution());

            Server.tasks.get(taskPair.getKey()).remove(taskPair.getValue());
        }

        result.append(Server.END);

        return result.toString();
    }

    private String deleteFile() throws IOException, InterruptedException {
        String fileName = in.readLine();

        if (!fileExists(fileName)) {
            return fileName + " не существует\r\n" + Server.END;
        }

        List<Pair<Integer, Task>> savedPlaces = new CopyOnWriteArrayList<>();

        for (DataInformation dataInformation : Server.dataStorage.get(fileName)) {
            Integer clusterId = dataInformation.getClusterId();
            Task task = new Task(String.format("RM\r\n%s\r\n%d\r\nEND\r\n",
                    fileName, dataInformation.getPart()));

            Server.tasks.get(clusterId).
                    add(task);

            savedPlaces.add(new Pair<>(clusterId, task));
        }

        String result = getResult(savedPlaces);

        Server.dataStorage.remove(fileName);

        return result;
    }

    private String replaceFragment() throws IOException, InterruptedException {
        String fileName = in.readLine();

        if (!fileExists(fileName)) {
            return fileName + " не существует\r\n" + Server.END;
        }

        String part = in.readLine();
        int filePart = Integer.parseInt(part);

        DataInformation dataInformation = findDataInformationByPart(fileName, filePart);

        if (dataInformation == null) {
            return fileName + "_" + filePart + " не существует\r\n" + Server.END;
        }

        StringBuilder command = new StringBuilder();

        while (true) {
            String string = in.readLine();

            if ("END".equals(string)) {
                break;
            }

            command.append(string).append("\t");
        }

        String commandString = command.toString();

        if (commandString.length() > BUFFER_SIZE) {
            return "Длина нового текста превышает длину в " + BUFFER_SIZE + " символа\r\n" + Server.END;
        }

        List<Pair<Integer, Task>> savedPlaces = new CopyOnWriteArrayList<>();

        Integer clusterId = dataInformation.getClusterId();
        Task task = new Task(String.format("RP\r\n%s\r\n%d\r\n%s\r\nEND\r\n",
                fileName, dataInformation.getPart(), commandString));

        Server.tasks.get(clusterId).add(task);

        savedPlaces.add(new Pair<>(clusterId, task));

        return getResult(savedPlaces);
    }

    private DataInformation findDataInformationByPart(String fileName, Integer part) {
        List<DataInformation> information = Server.dataStorage.get(fileName);

        for (DataInformation dataInformation : information) {
            if (dataInformation.getPart() == part) {
                return dataInformation;
            }
        }

        return null;
    }
}
