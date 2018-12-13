package hello.f2boy.mydubbo;

public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String aName, String bName) {
        return aName + ", 你好，我的名字是：" + bName;
    }

}
