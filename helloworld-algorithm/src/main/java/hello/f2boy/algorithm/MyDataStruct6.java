package hello.f2boy.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 红黑树
 */
public class MyDataStruct6 implements IMyDataStruct {

    private RedBlackTreeNode root;

    private enum Color {
        red, black
    }

    class RedBlackTreeNode {
        MyData data;
        Color color;
        RedBlackTreeNode left;
        RedBlackTreeNode right;

        boolean isRed() {
            return this.color == Color.red;
        }
    }

    @Override
    public MyData get(int key) {
        return get(root, key);
    }

    private MyData get(RedBlackTreeNode node, int key) {
        if (node == null) return null;

        if (key < node.data.key) return get(node.left, key);
        if (key > node.data.key) return get(node.right, key);
        return node.data;
    }

    @Override
    public void add(MyData data) {
        if (root == null) {
            root = new RedBlackTreeNode();
            root.data = data;
            root.color = Color.black;
            return;
        }

        add(root, data, new ArrayList<RedBlackTreeNode>());
        root.color = Color.black;
    }

    private void add(RedBlackTreeNode nowNode, MyData data, List<RedBlackTreeNode> nodePath) {

        int compare = data.key - nowNode.data.key;

        if (compare == 0) {
            nowNode.data = data;
            return;
        }

        nodePath.add(nowNode);

        if (compare < 0) {
            if (nowNode.left == null) {
                RedBlackTreeNode newNode = new RedBlackTreeNode();
                newNode.data = data;
                newNode.color = Color.red;
                nowNode.left = newNode;
                banlance(nodePath);
            } else {
                add(nowNode.left, data, nodePath);
            }
        } else {
            if (nowNode.right == null) {
                RedBlackTreeNode newNode = new RedBlackTreeNode();
                newNode.data = data;
                newNode.color = Color.red;
                nowNode.right = newNode;
                banlance(nodePath);
            } else {
                add(nowNode.right, data, nodePath);
            }
        }
    }

    private void banlance(List<RedBlackTreeNode> nodePath) {
        if (nodePath == null || nodePath.isEmpty()) return;

        RedBlackTreeNode nowNode = nodePath.remove(nodePath.size() - 1);
        if (nowNode.left == null && nowNode.right == null) {
            banlance(nodePath);
            return;
        }

        RedBlackTreeNode parent = nodePath.isEmpty() ? null : nodePath.get(nodePath.size() - 1);
        boolean isLeft = parent != null && parent.left == nowNode;
        RedBlackTreeNode winNode = null;

        // 右节点为红色的情况
        if (nowNode.right != null && nowNode.right.isRed()) {
            // 左右子节点都是红色，将红色传递到父节点
            if (nowNode.left != null && nowNode.left.isRed()) {
                winNode = flipColor(nowNode);
            }
            // 左子节点不为红色，左旋当前节点
            else {
                winNode = leftRotate(nowNode);
                nodePath.add(winNode);
            }
        }
        // 左节点为红色的情况，只需要考虑当前节点也为红色的情况，此时将父节点进行右旋, 然后弹出红色
        else if (nowNode.left != null && nowNode.left.isRed() && nowNode.isRed()) {
            if (parent != null) {
                winNode = rightRotate(parent);
                winNode = flipColor(winNode);
                // 因父节点右旋，故要选择父节点的父节点，来指向当前节点
                nodePath.remove(nodePath.size() - 1);
                if (nodePath.isEmpty()) {
                    parent = null;
                } else {
                    RedBlackTreeNode pParent = nodePath.get(nodePath.size() - 1);
                    isLeft = pParent.left == parent;
                    parent = pParent;
                }

            }
        }

        if (winNode != null) {
            // 没有父节点，说明当前即是根节点
            if (parent == null) {
                root = winNode;
            } else {
                if (isLeft) {
                    parent.left = winNode;
                } else {
                    parent.right = winNode;
                }
            }
        }

        // 往上递归
        banlance(nodePath);
    }

    private RedBlackTreeNode flipColor(RedBlackTreeNode parent) {
        RedBlackTreeNode left = parent.left;
        RedBlackTreeNode right = parent.right;

        Color parentColor = parent.color;
        parent.color = left.color;
        left.color = parentColor;
        right.color = parentColor;

        return parent;
    }

    private RedBlackTreeNode leftRotate(RedBlackTreeNode parent) {
        RedBlackTreeNode right = parent.right;
        parent.right = right.left;
        right.left = parent;

        Color parentColor = parent.color;
        parent.color = right.color;
        right.color = parentColor;

        return right;
    }

    private RedBlackTreeNode rightRotate(RedBlackTreeNode parent) {
        RedBlackTreeNode left = parent.left;
        parent.left = left.right;
        left.right = parent;

        Color parentColor = parent.color;
        parent.color = left.color;
        left.color = parentColor;

        return left;
    }

    @Override
    public void delete(int key) {
        root.color = Color.red;
        List<RedBlackTreeNode> nodePath = new ArrayList<RedBlackTreeNode>();
        delete(root, key, nodePath);
        banlance(nodePath);
        root.color = Color.black;
    }

    /**
     * 查询子树中的最小节点
     */
    private RedBlackTreeNode[] findMin(RedBlackTreeNode parent, RedBlackTreeNode nowNode) {
        if (nowNode.left == null) {
            return new RedBlackTreeNode[]{parent, nowNode};
        }

        return findMin(nowNode, nowNode.left);
    }

    private void delete(RedBlackTreeNode nowNode, int key, List<RedBlackTreeNode> nodePath) {

        RedBlackTreeNode parent = nodePath.isEmpty() ? null : nodePath.get(nodePath.size() - 1);
        boolean isLeft = parent != null && parent.left == nowNode;

        // 左节点查找
        if (key < nowNode.data.key) {
            if (nowNode.left == null) {
                return;
            }

            // 如果左节点不是2节点，从父节点传递过来
            if (!nowNode.left.isRed() && (nowNode.left.left == null || !nowNode.left.left.isRed())) {
                RedBlackTreeNode newNode = moveRedToLeft(nowNode);
                if (parent == null) {
                    root = newNode;
                } else {
                    if (isLeft) {
                        parent.left = newNode;
                    } else {
                        parent.right = newNode;
                    }
                }

                // 红色左移后，不是原节点了，说明发生了左旋，返回的是父节点
                if (newNode != nowNode) {
                    nodePath.add(newNode);
                }
            }

            nodePath.add(nowNode);
            delete(nowNode.left, key, nodePath);
        }
        // 当前节点查找到，或者在右节点查找
        else {

            // 在当前节点找到元素
            if (key == nowNode.data.key) {

                // 没有子节点，说明当前是叶子节点，可以直接删除
                if (nowNode.right == null) {

                    // 因为递归的规则，限定每一次递归，都会构造出一个二节点，所以，可以直接删除当前元素，如果当前是黑节点的话，将当前的父节点指向当前的左节点（红色节点）
                    RedBlackTreeNode next = null;
                    if (!nowNode.isRed()) {
                        nowNode.left.color = Color.black;
                        next = nowNode.left;
                    }

                    if (parent == null) {
                        root = next;
                    } else {
                        if (isLeft) {
                            parent.left = next;
                        } else {
                            parent.right = next;
                        }
                    }
                    return;
                }
                // 有子节点（非叶子节点），找到右子树的最小节点，进行替换，然后向右进行递归操作
                else {
                    RedBlackTreeNode[] minNode = findMin(nowNode, nowNode.right);
                    RedBlackTreeNode fParent = minNode[0];
                    RedBlackTreeNode fNode = minNode[1];

                    Color col = nowNode.color;
                    nowNode.color = fNode.color;
                    fNode.color = col;

                    if (parent == null) {
                        root = fNode;
                    } else {
                        if (isLeft) {
                            parent.left = fNode;
                        } else {
                            parent.right = fNode;
                        }
                    }

                    if (fParent == nowNode) {
                        nowNode.right = fNode.right;
                        fNode.right = nowNode;
                    } else {
                        fParent.left = nowNode;
                        RedBlackTreeNode tmp = nowNode.right;
                        nowNode.right = fNode.right;
                        fNode.right = tmp;
                    }
                    fNode.left = nowNode.left;
                    nowNode.left = null;

                    nowNode = fNode;
                }
            }

            if (nowNode.right == null) {
                return;
            }

            // 右节点查找，有可能左节点和父节点组合成一个2-节点，如果红色节点为左节点，则将父节点右旋，使父节点变为红色
            if (nowNode.left != null && nowNode.left.isRed()) {
                RedBlackTreeNode pParent = rightRotate(nowNode);
                if (parent == null) {
                    root = pParent;
                    parent = pParent;
                    isLeft = false;
                } else {
                    parent.right = pParent;
                }
                nodePath.add(pParent);
            }

            // 如果右节点不是2节点，从父节点传递过来
            if (!nowNode.right.isRed() && (nowNode.right.left == null || !nowNode.right.left.isRed())) {
                RedBlackTreeNode newNode = moveRedToRight(nowNode);
                if (parent == null) {
                    root = newNode;
                } else {
                    if (isLeft) {
                        parent.left = newNode;
                    } else {
                        parent.right = newNode;
                    }
                }

                // 红色右移后，不是原节点了，说明发生了左旋，返回的是父节点
                if (newNode != nowNode) {
                    nodePath.add(newNode);
                }
            }

            nodePath.add(nowNode);
            delete(nowNode.right, key, nodePath);
        }
    }

    /**
     * 红色传到左子节点
     */
    private RedBlackTreeNode moveRedToLeft(RedBlackTreeNode nowNode) {
        RedBlackTreeNode newNode = nowNode;
        flipColor(nowNode);
        if (nowNode.right.left != null && nowNode.right.left.isRed()) {
            nowNode.right = rightRotate(nowNode.right);
            newNode = leftRotate(nowNode);
            flipColor(newNode);
        }

        return newNode;
    }

    /**
     * 红色传到右子节点
     */
    private RedBlackTreeNode moveRedToRight(RedBlackTreeNode nowNode) {
        RedBlackTreeNode newNode = nowNode;
        flipColor(nowNode);
        if (nowNode.left.left != null && nowNode.left.left.isRed()) {
            newNode = rightRotate(nowNode);
            flipColor(newNode);
        }

        return newNode;
    }

    @Override
    public void iterate() {
        printNode(root);
        System.out.println();
    }

    private void printNode(RedBlackTreeNode node) {
        if (node == null) return;
        printNode(node.left);
        System.out.println(node.data);
        printNode(node.right);
    }

}
