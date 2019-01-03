package com.dstu;

import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        int cnt = 0;
        while (true) {
            try (Socket socket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                out.write("CLUSTER1\r\n");
                out.flush();

                Cluster cluster = new Cluster(in, out);

                while (true) {
                    cluster.execute();

                }


            } catch (IOException e) {
                System.out.println(++cnt + "wait");
            }
        }
    }
}
