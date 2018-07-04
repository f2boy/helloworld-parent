package hello.f2boy.mydubbo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生产者-消费者demo
 * 用一个厕所管理员和两个上厕所的人做例子
 * 生产者 - 厕所管理员，给厕所加厕纸
 * 消费者 - 上厕所的人，使用厕纸
 */
public class ProducerConsumerDemo {

    static long shitTime = 1000L;

    /**
     * 厕所，表示竞争的锁对象
     */
    static class Toilet {
        int toiletPaper = 0;

        void shit() {
            try {
                Thread.sleep(shitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
                toilet.shit();
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
        People people1 = new People(toilet, 1);
        People people2 = new People(toilet, 2);

        // 厕所管理员线程，给厕所加纸
        Manager manager = new Manager(toilet);

        people1.start();
        people2.start();
        log("At first, state of people1 is [" + people1.getState() + "], state of people2 is [" + people2.getState()+ "]");

        Thread.sleep(10);
        log("When they enter the toilet , state of people1 is [" + people1.getState() + "], state of people2 is [" + people2.getState()+ "]");

        manager.start();

        Thread.sleep(10);
        log("After manager add paper, state of people1 is [" + people1.getState() + "], state of people2 is [" + people2.getState()+ "]");

        Thread.sleep(shitTime + 10);
        log("After One of People finished shit, state of people1 is [" + people1.getState() + "], state of people2 is [" + people2.getState()+ "]");

        Thread.sleep(shitTime + 10);
        log("After Two People finished shit, state of people1 is [" + people1.getState() + "], state of people2 is [" + people2.getState()+ "]");
    }

    private static void log(String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        System.out.println(sdf.format(new Date()) + " - " + content);
    }

}
