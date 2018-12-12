package hello.f2boy.mydubbo.io.client;

import hello.f2boy.mydubbo.PrintUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class BioClient2 {

    public static final String BYE = "bye";

    public static void main(String[] args) throws IOException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Socket socket = new Socket("127.0.0.1", 1234);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        new Thread(() -> {
            String input = "";
            char c;
            while (true) {
                try {
                    c = (char) System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                input += c;
                if (c == '\n') {
                    if (input.equals("exit\n")) {
                        countDownLatch.countDown();
                        break;
                    }

                    try {
                        os.write(input.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (input.equals(BYE + "\n")) {
                        break;
                    }
                    input = "";
                }
            }
        }).start();

        new Thread(() -> {
            String resp = "";
            int c;
            try {
                while ((c = is.read()) != -1) {
                    resp += (char) c;
                    if (c == '\n') {
                        PrintUtils.print(resp);
                        if (resp.equals(BYE + "\n")) {
                            countDownLatch.countDown();
                            break;
                        }
                        resp = "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        countDownLatch.await();
        socket.close();
    }

}
