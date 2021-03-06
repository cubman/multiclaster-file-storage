package com.dstu;

import java.io.*;
import java.net.Socket;

public class ClientGetFile {
    public static void main(String[] args) {
        int cnt = 0;
        while (true) {
            try (Socket socket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                out.write("CLIENT GET FILE\r\n");
                out.flush();
                out.write("GET\r\nHW\r\nEND\r\n");
                out.flush();

                StringBuilder resString = new StringBuilder();

                while (true) {
                    String serverWord = in.readLine();

                    if (serverWord == null) {
                        System.out.println("Connection closed");
                        return;
                    }

                    if ("END".equals(serverWord)) {
                        System.out.println(resString);
                        return;
                    }

                    resString.append(serverWord).append('\n');
                }

            } catch (IOException e) {
                System.out.println(++cnt + "wait");
            }
        }
    }
}
