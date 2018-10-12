package hello.f2boy.mydubbo.io.bio1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Bio1Server {

    private static void start() throws IOException {

        ServerSocket serverSocket = new ServerSocket(1234);

        while (true) {

            Socket socket = serverSocket.accept();

            new Thread(() -> {
                InetSocketAddress remoteAddress = ((InetSocketAddress) socket.getRemoteSocketAddress());
                String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

                try {
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();
                    String message = "";
                    int c;
                    while ((c = is.read()) != -1) {
                        if (c == '\n') {
                            System.out.println(client + " send message: " + message);
                            os.write(("your message is: " + message + "\n").getBytes());
                            if (message.equals("bye")) {
                                socket.close();
                                break;
                            }
                            message = "";
                        } else {
                            message += (char) c;
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
            }).start();

        }
    }

    public static void main(String[] args) throws Exception {

        Bio1Server.start();
    }

}
