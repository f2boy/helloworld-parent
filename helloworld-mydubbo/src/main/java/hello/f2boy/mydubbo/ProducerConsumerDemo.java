package hello.f2boy.mydubbo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生产者-消费者demo
 * 用一个厕所管理员和多个上厕所的人做例子
 * 生产者 - 厕所管理员，给厕所加厕纸
 * 消费者 - 上厕所的人，使用厕纸
 */
public class ProducerConsumerDemo {

    static long shitTime = 1000L;

    /**
     * 厕所，表示竞争的锁对象
     */
    static class Toilet {
        int toiletPaper = 0; // 厕纸
    }

    /**
     * 用户线程，急于上厕所的人
     */
    static class People extends Thread {

        private final Toilet toilet;

        public People(Toilet toilet, int no) {
            super();
            this.toilet = toilet;
            this.setName("people-" + no);
        }

        @Override
        public void run() {
            synchronized (toilet) {
                String currentThread = Thread.currentThread().getName();
                log(currentThread + " enter the toilet.");
                while (toilet.toiletPaper <= 0) {
                    log(currentThread + " now to be go out and waiting.");
                    try {
                        toilet.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                toilet.toiletPaper--;
                // 模拟shit
                try {
                    Thread.sleep(shitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log(currentThread + " finished shit.");
            }
        }
    }

    static class Manager extends Thread {
        private final Toilet toilet;

        public Manager(Toilet toilet) {
            super();
            this.toilet = toilet;
            this.setName("[provider]-manager");
        }

        @Override
        public void run() {
            synchronized (toilet) {
                toilet.toiletPaper = 10;
                toilet.notifyAll();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // 厕所，代表线程要竞争的锁
        Toilet toilet = new Toilet();

        // 急于上厕所的用户线程
        People[] peoples = new People[3];
        for (int i = 0; i < peoples.length; i++) {
            peoples[i] = new People(toilet, i + 1);
        }

        // 厕所管理员线程，给厕所加纸
        Manager manager = new Manager(toilet);

        for (People people : peoples) {
            people.start();
        }
        log("At first, states: " + allStates(peoples));

        Thread.sleep(10);
        log("When they entered the toilet, states: " + allStates(peoples));

        manager.start();

        Thread.sleep(10);
        log("After manager added paper, states: " + allStates(peoples));

        for (People ignored : peoples) {
            Thread.sleep(shitTime);
            log("After One of People finished shit, states: " + allStates(peoples));
        }
    }

    private static String allStates(People[] peoples) {
        StringBuilder sb = new StringBuilder("[");
        for (People people : peoples) {
            sb.append(people.getState().name()).append(", ");
        }

        return sb.substring(0, sb.length() - 2) + "]";
    }

    private static void log(String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        System.out.println(sdf.format(new Date()) + " - " + content);
    }

}
