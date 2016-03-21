package hello.f2boy.storm.init;

import hello.f2boy.storm.tcp.TcpServer;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;

public class Init {
    private static final Logger logger = LoggerFactory.getLogger(Init.class);
    public static ClassPathXmlApplicationContext context;

    /**
     * 启动服务
     *
     * @param args 参数
     */
    public static void main(String[] args) throws FileNotFoundException {
        context = new ClassPathXmlApplicationContext();
        PropertyConfigurator.configure(Init.class.getClassLoader().getResource("log4j.properties"));
        Config.initConfit();
        TcpServer tcpServer = new TcpServer();
        tcpServer.start();
    }
}
