package com.dstu;

import java.io.*;
import java.net.Socket;

public class Cluster {
    private BufferedReader in;
    private BufferedWriter out;

    public Cluster(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    public void execute() throws IOException {
        String serverWord = in.readLine();

        switch (serverWord) {
            case "SV":
                saveCommand();
                break;
            case "GT":
                getCommand();
                break;
            case "RM":
                removeCommand();
                break;
        }
    }

    private void saveCommand() throws IOException {
        StringBuilder resString = new StringBuilder();

        String fileName = in.readLine();
        String part = in.readLine();

        while (true) {
            String textWords = in.readLine();

            if ("END".equals(textWords)) {
                System.out.println(resString);
                break;
            }
            resString.append(textWords);
        }

        File directory = new File("files");
        if (!directory.exists()) {
            directory.mkdir();
        }

        saveFile("files/" + fileName + "_" + part + ".txt", resString.toString());

        out.write("OK." + fileName + "_" + part + " file was saved\r\nEND\r\n");
        out.flush();
    }

    private void saveFile(String fileName, String text) {
        try (PrintStream out = new PrintStream(new FileOutputStream(fileName))) {
            out.print(text);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getCommand() throws IOException {
        StringBuilder resString = new StringBuilder();

        String fileName = in.readLine();
        String part = in.readLine();

        String file = "files/" + fileName + "_" + part + ".txt";

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (true) {
                String textWords = reader.readLine();

                if (textWords == null) {
                    System.out.println(resString);
                    break;
                }
                resString.append(textWords);
            }
        }

        out.write(resString + "\r\nEND\r\n");
        out.flush();
    }

    private void removeCommand() throws IOException {
        String fileName = in.readLine();
        String part = in.readLine();

        File file = new File("files/" + fileName + "_" + part + ".txt");

        if (file.delete()) {
            out.write(fileName + "_" + part + " был удален\r\nEND\r\n");
        } else {
            out.write(fileName + "_" + part + " не был удален\r\nEND\r\n");
        }
        out.flush();
    }
}
