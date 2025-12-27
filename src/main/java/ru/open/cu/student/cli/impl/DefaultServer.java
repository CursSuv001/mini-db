package ru.open.cu.student.cli.impl;

import ru.open.cu.student.cli.api.BackendWorker;
import ru.open.cu.student.cli.api.Engine;
import ru.open.cu.student.cli.api.Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DefaultServer implements Server {
    private final int port;
    private final Engine engine;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public DefaultServer(int port) {
        this.port = port;
        this.engine = new DefaultEngine();
    }

    public DefaultServer(int port, Engine engine) {
        this.port = port;
        this.engine = engine;
    }

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                BackendWorker worker = new DefaultBackendWorker(clientSocket, engine);

                Thread clientThread = new Thread(worker);
                clientThread.setDaemon(true);
                clientThread.start();

            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
        }
    }
}
