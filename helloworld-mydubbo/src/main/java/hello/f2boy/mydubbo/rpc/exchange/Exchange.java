package hello.f2boy.mydubbo.rpc.exchange;


public class Exchange<T> {
    
    private int headLenth;
    private int bodyLength;
    
    private T body;

    public int getHeadLenth() {
        return headLenth;
    }

    public void setHeadLenth(int headLenth) {
        this.headLenth = headLenth;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
