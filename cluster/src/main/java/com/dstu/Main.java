package com.dstu;

import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        int cnt = 0;
        while (true) {
            try (Socket socket = new Socket(args[0], Integer.valueOf(args[1]));
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                out.write("CLUSTER" + args[2] + "\r\n");
                out.flush();

                Cluster cluster = new Cluster(in, out);

                while (true) {
                    cluster.execute();

                }


            } catch (Exception e) {
                System.out.println(++cnt + "wait");
            }
        }
    }
}
