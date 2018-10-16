package hello.f2boy.mydubbo.io.server;

import hello.f2boy.mydubbo.PrintUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BioServer2 {

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private static ServerSocket serverSocket;
    private static List<Socket> sockets = new ArrayList<>();

    private static void start() {

        int backlog = 1;
        try {
            serverSocket = new ServerSocket(1234, backlog);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {

            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            sockets.add(socket);

            executor.execute(() -> {

                InetSocketAddress remoteAddress = ((InetSocketAddress) socket.getRemoteSocketAddress());
                String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

                try {
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();
                    String message = "";
                    int c;
                    while ((c = is.read()) != -1) {
                        message += (char) c;
                        if (c == '\n') {
                            PrintUtils.print(client + " send message: " + message);
                            os.write(("your message is: " + message).getBytes());
                            if (message.equals("bye\n")) {
                                os.write(message.getBytes());
                                socket.close();
                                break;
                            }
                            message = "";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            PrintUtils.println("executor.getActiveCount(): " + executor.getActiveCount());
        }
    }

    private static void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                PrintUtils.println("executor did not shutdown gracefully within 5 seconds.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Socket socket : sockets) {
            if (socket == null || socket.isClosed()) continue;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread(BioServer2::stop));

        new Thread(BioServer2::start).start();

        Thread.sleep(1000 * 60 * 10);
        System.exit(0);
    }

}
