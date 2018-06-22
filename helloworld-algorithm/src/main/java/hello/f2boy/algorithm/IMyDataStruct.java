package hello.f2boy.algorithm;

/**
 * 自定义数据结构，满足增删查。简单起见假设数据长度不超过10
 */
public interface IMyDataStruct {

    int MAX_LENTH = 9;
    
    MyData get(int key);

    void add(MyData data);

    void delete(int key);

    void iterate();
}
