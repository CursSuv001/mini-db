package ru.open.cu.student.cli;

import ru.open.cu.student.cli.impl.DefaultServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 5555; // По умолчанию PostgreSQL порт

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: 5432");
            }
        }

        DefaultServer server = new DefaultServer(port);
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}