package hello.f2boy.mydubbo.rpc;

import java.io.Serializable;

public class Response implements Serializable {

    private String code;

    private Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
