package hello.f2boy.mydubbo;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class PrintUtils {

    private PrintUtils() {
    }

    public static void print(String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        System.out.print(sdf.format(new Date()) + " " + Thread.currentThread().getName() + " - " + content);
    }

    public static void println(String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        System.out.println(sdf.format(new Date()) + " " + Thread.currentThread().getName() + " - " + content);
    }

}
