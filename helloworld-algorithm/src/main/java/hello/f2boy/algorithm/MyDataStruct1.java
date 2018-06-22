package hello.f2boy.algorithm;

/**
 * 最简单版本，不排序
 */
public class MyDataStruct1 implements IMyDataStruct {

    private MyData[] nodes = new MyData[MAX_LENTH];

    private int length = 0;

    @Override
    public MyData get(int key) {

        for (int i = 0; i < length; i++) {
            if (nodes[i].key == key)
                return nodes[i];
        }

        return null;
    }

    @Override
    public void add(MyData node) {
        if (length == MAX_LENTH) {
            throw new RuntimeException("达到最大长度啦");
        }

        nodes[length++] = node;
    }

    @Override
    public void delete(int key) {
        int idx = -1;
        for (int i = 0; i < length; i++) {
            if (nodes[i].key == key) {
                idx = i;
                break;
            }
        }

        if (idx == -1) return;

        for (int i = idx; i < length - 1; i++) {
            nodes[i] = nodes[i + 1];
        }

        nodes[length - 1] = null;
        length--;
    }

    @Override
    public void iterate() {
        for (int i = 0; i < length; i++) {
            System.out.println(nodes[i].key + " - " + nodes[i].name);
        }
        System.out.println();
    }

}
