package hello.f2boy.storm.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Properties props;

    /**
     * 获取配置文件
     */
    public static void initConfig() {
        Properties props;
        try {
            Resource resource = new ClassPathResource("netty.properties");
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (Exception e) {
            logger.error("初始化系统配置文件失败", e);
            props = new Properties();
        }

        Config.props = props;
    }

    /**
     * 获取TCP端口
     *
     * @return 端口
     */
    public static int getTcpPort() {
        int port = 9999;
        try {
            String value = props.getProperty("netty.tcp.server.port");
            port = Integer.parseInt(value);
        } catch (Exception e) {
            logger.warn("获取TCP端口失败，使用默认[9999]端口", e);
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
            String value = props.getProperty("netty.websocket.server.port");
            port = Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("获取Websocket端口失败，使用默认[6443]端口", e);
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
            value = props.getProperty("netty.websocket.server.path");
        } catch (Exception e) {
            logger.error("获取Websocket路径失败，使用默认[/websocket]路径", e);
        }

        return value;
    }

}
