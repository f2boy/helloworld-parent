package hello.f2boy.dubbox;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Bootstrap {

    public static void main(String[] args) throws Exception {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-provider.xml");

        context.start();
        System.in.read(); // 按任意键退出
    }

}
