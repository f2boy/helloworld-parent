package hello.f2boy.dubbox;

import hello.f2boy.dubbox.service.HelloService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Consumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-consumer.xml");

        HelloService helloService = context.getBean(HelloService.class); // 获取远程服务代理
        String hello = helloService.find(1);

        System.out.println(hello); // 显示调用结果
    }

}
