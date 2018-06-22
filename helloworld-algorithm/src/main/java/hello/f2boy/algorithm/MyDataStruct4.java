package hello.f2boy.algorithm;

/**
 * 二叉排序树
 */
public class MyDataStruct4 implements IMyDataStruct {

    MySortTreeNode root;

    class MySortTreeNode extends MyData {
        MySortTreeNode left;
        MySortTreeNode right;

        public MySortTreeNode(MyData myNode) {
            super(myNode.key, myNode.name);
        }
    }

    @Override
    public MyData get(int key) {

        return get(root, key);
    }

    private MyData get(MySortTreeNode root, int key) {
        if (root == null) return null;

        if (key < root.key) return get(root.left, key);
        if (key > root.key) return get(root.right, key);

        return root;
    }

    @Override
    public void add(MyData myNode) {
        if (root == null) {
            root = new MySortTreeNode(myNode);
            return;
        }

        add(root, new MySortTreeNode(myNode));
    }

    private void add(MySortTreeNode root, MySortTreeNode node) {
        if (node.key < root.key) {
            if (root.left == null) {
                root.left = node;
            } else {
                add(root.left, node);
            }
        } else if (node.key > root.key) {
            if (root.right == null) {
                root.right = node;
            } else {
                add(root.right, node);
            }
        } else {
            root.name = node.name;
        }
    }

    @Override
    public void delete(int key) {
        delete(null, root, key);
    }

    private void delete(MySortTreeNode parent, MySortTreeNode nowNode, int key) {
        if (nowNode == null) return;
        if (key < nowNode.key) {
            delete(nowNode, nowNode.left, key);
        } else if (key > nowNode.key) {
            delete(nowNode, nowNode.right, key);
        } else {

            MySortTreeNode replaceNode = null;
            if (nowNode.left != null) {
                MySortTreeNode[] result = findMax(nowNode, nowNode.left);
                MySortTreeNode replaceParentNode = result[0];
                replaceNode = result[1];
                replaceNode.right = nowNode.right;
                if (replaceNode != nowNode.left) {
                    replaceParentNode.right = replaceNode.left;
                    replaceNode.left = nowNode.left;
                }
            } else if (nowNode.right != null) {
                MySortTreeNode[] result = findMin(nowNode, nowNode.right);
                MySortTreeNode replaceParentNode = result[0];
                replaceNode = result[1];
                replaceNode.left = nowNode.left;
                if (replaceNode != nowNode.right) {
                    replaceParentNode.left = replaceNode.right;
                    replaceNode.right = nowNode.right;
                }
            }

            if (parent == null) {
                root = replaceNode;
            } else {
                boolean isLeft = parent.left == nowNode;
                if (isLeft) {
                    parent.left = replaceNode;
                } else {
                    parent.right = replaceNode;
                }
            }
        }
    }

    private MySortTreeNode[] findMin(MySortTreeNode parent, MySortTreeNode current) {
        if (current.left == null) return new MySortTreeNode[]{parent, current};
        return findMin(current, current.left);
    }

    private MySortTreeNode[] findMax(MySortTreeNode parent, MySortTreeNode current) {
        if (current.right == null) return new MySortTreeNode[]{parent, current};
        return findMax(current, current.right);
    }

    @Override
    public void iterate() {
        printNode(root);
        System.out.println();
    }

    private void printNode(MySortTreeNode parent) {
        if (parent == null) return;

        printNode(parent.left);
        System.out.println(parent);
        printNode(parent.right);

    }

}
