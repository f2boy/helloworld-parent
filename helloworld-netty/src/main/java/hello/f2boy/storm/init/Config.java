package hello.f2boy.storm.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Properties properties;

    /**
     * 获取配置文件
     */
    public static Properties initConfit() {
        Properties prop = new Properties();
        try {
            Config.properties = prop;
        } catch (Exception e) {
            logger.error("初始化系统配置文件失败", e);
        }
        return prop;
    }

    /**
     * 获取TCP端口
     *
     * @return 端口
     */
    public static int getTcpPort() {
        int port = 9999;
        try {
            String value = Config.properties.getProperty("netty.tcp.server.port");
            port = Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("获取TCP端口失败，使用默认[9999]端口", e);
        }
        return port;
    }

    /**
     * 获取Websocket端口
     *
     * @return 端口
     */
    public static int getWebsocketPort() {
        int port = 6443;
        try {
            String value = Config.properties.getProperty("netty.websocket.server.port");
            port = Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("获取Websocket端口失败", e);
        }
        return port;
    }

    /**
     * 获取Websocket路径
     *
     * @return 路径
     */
    public static String getWebsocketPath() {
        String value = "/websocket";
        try {
            value = Config.properties.getProperty("netty.websocket.server.path");
        } catch (Exception e) {
            logger.error("获取Websocket路径失败", e);
        }
        return value;
    }
}
