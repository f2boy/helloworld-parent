package hello.f2boy.mydubbo.provider;

import hello.f2boy.mydubbo.io.server.NioServer3;
import hello.f2boy.mydubbo.registry.Registry;

import java.io.IOException;

public class Provider {
    
    private Registry registry;

    public Provider(Registry registry) {
        this.registry = registry;
    }

    public  void  registerService(Service service){

        registry.registerService(service.getInterfaceName(), service.getProviderIp());

        try {
            NioServer3.listen(20880);
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        Class clazz;
        try {
             clazz = Class.forName(service.getInterfaceName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        
    }
    
}
