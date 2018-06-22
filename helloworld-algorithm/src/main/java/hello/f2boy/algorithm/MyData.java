package hello.f2boy.algorithm;

public class MyData {
    int key;
    String name;

    public MyData(int key, String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String toString() {
        return key + " - " + name;
    }

}
