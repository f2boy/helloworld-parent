package hello.f2boy.mydubbo.io.bio2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Bio2Client {

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
                    input += c;
                    if (c == '\n') {
                        os.write(input.getBytes());
                        if (input.equals(BYE + "\n")) {
                            break;
                        }
                        input = "";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
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
                        System.out.print(resp);
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