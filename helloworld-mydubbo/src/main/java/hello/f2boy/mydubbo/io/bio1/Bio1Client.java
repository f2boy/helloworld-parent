package hello.f2boy.mydubbo.io.bio1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Bio1Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("127.0.0.1", 1234);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        os.write((Thread.currentThread().getName() + "\n").getBytes());
        os.write("111\n".getBytes());
        os.write("222\n".getBytes());
        os.write("bye\n".getBytes());

        String input = "";
        int c;
        while ((c = is.read()) != -1) {
            if (c == '\n') {
                System.out.println(input);
                input = "";
            } else {
                input += (char) c;
            }
        }

        socket.close();
    }

}
