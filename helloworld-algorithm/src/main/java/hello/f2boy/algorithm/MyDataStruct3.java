package hello.f2boy.algorithm;

/**
 * 基于链表排序
 */
public class MyDataStruct3 implements IMyDataStruct {

    class MyListNode extends MyData {
        MyListNode next;

        public MyListNode(MyData myNode) {
            super(myNode.key, myNode.name);
        }
    }

    private MyListNode first;

    @Override
    public MyData get(int key) {
        if (first == null) {
            return null;
        }

        if (first.key == key) {
            return first;
        }

        MyListNode smaller = first;
        while (smaller.next != null && smaller.next.key < key) {
            smaller = smaller.next;
        }

        if (smaller.next != null && smaller.next.key == key) {
            return smaller.next;
        }

        return null;
    }

    @Override
    public void add(MyData myNode) {

        MyListNode node = new MyListNode(myNode);
        if (first == null) {
            first = node;
            return;
        }

        if (node.key < first.key) {
            node.next = first;
            first = node;
            return;
        }

        if (node.key == first.key) {
            node.next = first.next;
            first = node;
            return;
        }

        MyListNode smaller = first;
        while (smaller.next != null && smaller.next.key < node.key) {
            smaller = smaller.next;
        }

        if (smaller.next == null) {
            smaller.next = node;
        } else if (smaller.next.key == node.key) {
            node.next = smaller.next.next;
            smaller.next = node;
        } else {
            node.next = smaller.next;
            smaller.next = node;
        }
    }

    @Override
    public void delete(int key) {
        if (first == null) {
            return;
        }

        if (first.key == key) {
            MyListNode node = first;
            first = node.next;
            node.next = null;
        }

        MyListNode smaller = first;
        while (smaller.next != null && smaller.next.key < key) {
            smaller = smaller.next;
        }

        if (smaller.next != null && smaller.next.key == key) {
            MyListNode node = smaller.next;
            smaller.next = node.next;
            node.next = null;
        }

    }

    @Override
    public void iterate() {
        if (first == null) {
            return;
        }

        System.out.println(first);

        MyListNode smaller = first;
        while (smaller.next != null) {
            System.out.println(smaller.next);
            smaller = smaller.next;
        }

        System.out.println();
    }

}
