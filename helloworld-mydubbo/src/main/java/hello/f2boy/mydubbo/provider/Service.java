package hello.f2boy.mydubbo.provider;

public class Service {
    private String interfaceName;
    private Object bean;
    private String providerIp;

    public Service(String interfaceName, Object bean) {
        this.interfaceName = interfaceName;
        this.bean = bean;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getProviderIp() {
        return providerIp;
    }

    public void setProviderIp(String providerIp) {
        this.providerIp = providerIp;
    }
}
