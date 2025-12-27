package ru.open.cu.student.cli.impl;

import ru.open.cu.student.cli.api.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class DefaultClient implements Client {
    private final String host;
    private final int port;

    public DefaultClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server at " + host + ":" + port);

            String welcomeMessage = readResponse(in);
            System.out.println(welcomeMessage);

            while (true) {
                System.out.print("SQL> ");
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
                    out.println(command);
                    System.out.println("Goodbye!");
                    break;
                }

                out.println(command);

                String response = readResponse(in);
                if (!response.isEmpty()) {
                    System.out.println(response);
                }
            }
        }
    }

    private String readResponse(BufferedReader in) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals("END")) {
                break;
            }
            response.append(line).append("\n");
        }
        return response.toString().trim();
    }
}
