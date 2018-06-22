package hello.f2boy.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 2-3 树
 */
public class MyDataStruct5 implements IMyDataStruct {

    My23TreeNode root;

    class My23TreeNode {
        MyData smaller;
        MyData bigger;
        MyData tempData;

        My23TreeNode left;
        My23TreeNode middle;
        My23TreeNode right;
        My23TreeNode tempRight; // 临时子节点，放在最右端，用于在插入第3个元素的时候
    }

    @Override
    public MyData get(int key) {
        return get(root, key);
    }

    private MyData get(My23TreeNode parent, int key) {
        if (parent == null) {
            return null;
        }
        MyData smaller = parent.smaller;
        MyData bigger = parent.bigger;
        if (key == smaller.key) {
            return smaller;
        } else if (key < smaller.key) {
            return get(parent.left, key);
        } else {
            if (bigger == null) {
                return get(parent.right, key);
            } else {
                if (key == bigger.key) {
                    return bigger;
                } else if (key < bigger.key) {
                    return get(parent.middle, key);
                } else {
                    return get(parent.right, key);
                }
            }
        }

    }

    @Override
    public void add(MyData data) {
        if (root == null) {
            root = new My23TreeNode();
            root.smaller = data;
            return;
        }

        List<My23TreeNode> nodePath = new ArrayList<>();
        nodePath.add(root);
        insert(root, data, nodePath);
    }

    /**
     * 插入数据
     *
     * @param nowNode  当前节点
     * @param data     要插入的数据
     * @param nodePath 递归时，记录整个节点的路径，因为要向上分裂
     */
    private void insert(My23TreeNode nowNode, MyData data, List<My23TreeNode> nodePath) {

        int key = data.key;
        MyData smaller = nowNode.smaller;
        MyData bigger = nowNode.bigger;

        if (key == smaller.key) {
            nowNode.smaller = data;
            return;
        }

        if (key < smaller.key) {
            if (nowNode.left != null) {
                nodePath.add(nowNode.left);
                insert(nowNode.left, data, nodePath);
            } else {
                insertNode(nodePath, data);
            }
            return;
        }

        if (bigger == null || key > bigger.key) {
            if (nowNode.right != null) {
                nodePath.add(nowNode.right);
                insert(nowNode.right, data, nodePath);
            } else {
                insertNode(nodePath, data);
            }
        } else if (key == bigger.key) {
            nowNode.bigger = data;
        } else if (key < bigger.key) {
            if (nowNode.middle != null) {
                nodePath.add(nowNode.middle);
                insert(nowNode.middle, data, nodePath);
            } else {
                insertNode(nodePath, data);
            }
        }

    }

    private void insertNode(List<My23TreeNode> nodePath, MyData data) {
        My23TreeNode node = nodePath.remove(nodePath.size() - 1);
        My23TreeNode parent = null;
        if (!nodePath.isEmpty()) {
            parent = nodePath.get(nodePath.size() - 1);
        }

        // 节点只有一个元素，直接插入即可
        if (node.bigger == null) {
            if (data.key > node.smaller.key) {
                node.bigger = data;
            } else {
                node.bigger = node.smaller;
                node.smaller = data;
            }
            return;
        }

        // 插入类型 1：最小  2：中间  3：最大
        int insType;

        // 节点有两个元素，插入之后变为3个，要把中间的弹出去，同时分裂
        if (data.key < node.smaller.key) {
            node.tempData = node.smaller;
            node.smaller = data;
            insType = 1;
        } else if (data.key > node.bigger.key) {
            node.tempData = node.bigger;
            node.bigger = data;
            insType = 3;
        } else {
            node.tempData = data;
            insType = 2;
        }

        // 分裂当前节点
        My23TreeNode left = new My23TreeNode();
        left.smaller = node.smaller;
        My23TreeNode right = new My23TreeNode();
        right.smaller = node.bigger;

        // 如果临时节点不为null，说明是递归传递上来的，已经指定好子节点了
        if (node.tempRight != null) {
            left.left = node.left;
            left.right = node.middle;
            right.left = node.right;
            right.right = node.tempRight;
        } else {
            if (insType == 1) {
                left.right = node.middle;
                right.right = node.right;
            } else if (insType == 2) {
                left.left = node.left;
                right.right = node.right;
            } else {
                left.left = node.left;
                right.left = node.middle;
            }
        }

        // 如果父节点为null，说明当前节点为根节点，往下分裂两个之后，根节点弹高一层
        if (parent == null) {
            node.left = left;
            node.right = right;
            node.middle = null;
            node.tempRight = null;
            node.smaller = node.tempData;
            node.bigger = null;
            node.tempData = null;
        }
        // 弹出中间数据到父节点
        else {

            // 如果父节点只有1个元素，则直接写入
            if (parent.bigger == null) {
                if (node.tempData.key < parent.smaller.key) {
                    parent.bigger = parent.smaller;
                    parent.smaller = node.tempData;
                    parent.left = left;
                    parent.middle = right;
                } else {
                    parent.bigger = node.tempData;
                    parent.middle = left;
                    parent.right = right;
                }
            }
            // 父节点有2个元素，标记父节点的临时节点，然后递归调用插入方法
            else {
                if (node == parent.left) {
                    parent.left = left;
                    parent.middle = right;
                    parent.tempRight = parent.right;
                    parent.right = parent.middle;
                } else if (node == parent.middle) {
                    parent.tempRight = parent.right;
                    parent.right = right;
                    parent.middle = left;
                } else {
                    parent.right = left;
                    parent.tempRight = right;
                }

                insertNode(nodePath, node.tempData);
            }

        }
    }

    @Override
    public void delete(int key) {

    }

    private My23TreeNode findForDelete(My23TreeNode parent, My23TreeNode nowDate, int key) {

        if (nowDate == null) {
            return null;
        }

        MyData smaller = nowDate.smaller;
        MyData bigger = nowDate.bigger;
        MyData tempData = nowDate.tempData;

        My23TreeNode next;

        if (key == smaller.key) {
            return nowDate;
        } else if (key < smaller.key) {
            next = nowDate.left;
        } else {
            if (tempData != null) {
                if (key == tempData.key) {
                    return nowDate;
                } else if (key < tempData.key) {
                    next = nowDate.middle;
                } else if (key == bigger.key) {
                    next = nowDate;
                } else if (key < bigger.key) {
                    next = nowDate.right;
                } else {
                    next = nowDate.tempRight;
                }
            } else {
                if (key == bigger.key) {
                    next = nowDate;
                } else if (key < bigger.key) {
                    next = nowDate.middle;
                } else {
                    next = nowDate.right;
                }
            }
        }

        return null;

    }

    @Override
    public void iterate() {
        printTree(root);
        System.out.println();
    }

    private void printTree(My23TreeNode node) {
        if (node == null) return;

        printTree(node.left);
        System.out.println(node.smaller);
        if (node.bigger == null) {
            printTree(node.right);
        } else {
            printTree(node.middle);
            System.out.println(node.bigger);
            printTree(node.right);
        }
    }

}
