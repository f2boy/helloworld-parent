package hello.f2boy.algorithm;

/**
 * 基于数组排序
 */
public class MyDataStruct2 implements IMyDataStruct {

    private MyData[] nodes = new MyData[MAX_LENTH];

    private int length = 0;

    @Override
    public MyData get(int key) {
        return binarySearch(key);
    }

    @Override
    public void add(MyData node) {
        if (length == MAX_LENTH) {
            throw new RuntimeException("达到最大长度啦");
        }

        int index = binarySearchForInsert(node.key, 0, length - 1);

        int aa = 0;

        for (int i = length; i > index; i--) {
            nodes[i] = nodes[i - 1];
        }
        nodes[index] = node;
        length++;
    }

    @Override
    public void delete(int key) {
        int idx = binarySearch(key, 0, length - 1);
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
            System.out.println(nodes[i]);
        }
        System.out.println();
    }

    /**
     * 二分法查找
     */
    public MyData binarySearch(int key) {
        int index = binarySearch(key, 0, length - 1);
        if (index >= 0) {
            return nodes[index];
        } else {
            return null;
        }
    }

    /**
     * 二分法查找
     */
    private int binarySearch(int key, int start, int end) {
        if (start > end) return -1;

        if (start == end) {
            if (nodes[start].key == key) return start;
            else return -1;
        }

        if (start == end - 1) {
            if (nodes[start].key == key) return start;
            if (nodes[end].key == key) return end;
            else return -1;
        }

        int middle = (end + start) / 2;
        int r = key - nodes[middle].key;
        if (r == 0) return middle;
        else if (r < 0) return binarySearch(key, start, middle);
        else return binarySearch(key, middle + 1, end);
    }

    /**
     * 二分法查找要插入的位置
     */
    private int binarySearchForInsert(int key, int start, int end) {
        if (start > end) return start;

        if (start == end || end == start + 1) {
            if (key > nodes[end].key) {
                return end + 1;
            } else if (key > nodes[start].key) {
                return start + 1;
            } else if (key < nodes[start].key) {
                return start;
            } else {
                throw new RuntimeException("已经存在，在第[" + start + "]个位置");
            }
        }

        int middle = (end + start) / 2;
        int r = key - nodes[middle].key;
        if (r == 0) throw new RuntimeException("已经存在，在第[" + middle + "]个位置");
        else if (r < 0) return binarySearchForInsert(key, start, middle - 1);
        else return binarySearchForInsert(key, middle, end);
    }

}
