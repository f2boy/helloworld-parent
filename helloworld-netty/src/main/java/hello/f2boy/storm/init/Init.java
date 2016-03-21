package hello.f2boy.storm.init;

import hello.f2boy.storm.tcp.TcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class Init {
    private static final Logger logger = LoggerFactory.getLogger(Init.class);

    /**
     * 启动服务
     *
     * @param args 参数
     */
    public static void main(String[] args) throws FileNotFoundException {
        Config.initConfig();
        new TcpServer().start();
    }

}
