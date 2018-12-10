package hello.f2boy.mydubbo.provider;

import hello.f2boy.mydubbo.registry.Registry;
import hello.f2boy.mydubbo.rpc.server.Acceptor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Provider {

    private Registry registry;

    public Provider(Registry registry) {
        this.registry = registry;
    }

    public void registerService(Service service) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                Acceptor.listen(20880);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();

        registry.registerService(service.getInterfaceName(), service.getProviderIp());

//        Class clazz;
//        try {
//             clazz = Class.forName(service.getInterfaceName());
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            return;
//        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
