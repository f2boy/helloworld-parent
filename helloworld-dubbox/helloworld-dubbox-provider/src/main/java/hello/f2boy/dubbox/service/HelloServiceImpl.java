package hello.f2boy.dubbox.service;

public class HelloServiceImpl implements HelloService {

    @Override
    public String find(int id) {
        return "hello, " + id;
    }

}
