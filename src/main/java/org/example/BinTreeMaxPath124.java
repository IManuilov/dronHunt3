package org.example;

public class BinTreeMaxPath124 {

  static class TreeNode {
      int val;
      TreeNode left;
      TreeNode right;
      TreeNode() {}
      TreeNode(int val) { this.val = val; }
      TreeNode(int val, TreeNode left, TreeNode right) {
          this.val = val;
          this.left = left;
          this.right = right;
      }
  }


  public static void main(String[] args) {

      TreeNode root = new TreeNode(
          1,
          new TreeNode(-2,
              new TreeNode(1),
              new TreeNode(3)
          ),
          new TreeNode(-3,
              new TreeNode(-2),
              new TreeNode(-1)
          )
      );

      int res = new BinTreeMaxPath124().maxPathSum(root);


       System.out.println(res);
  }



    int fullmax = -2000;
    public int maxPathSum(TreeNode root) {

        int mx = max(root);
        return Math.max(fullmax, mx);

    }

      int max(TreeNode node) {
        int l = node.left != null ? max(node.left) : -2000;
        int r = node.right != null ? max(node.right) : -2000;
        int v = node.val;

        //l = Math.max(0, l);
        //r = Math.max(0, r);

        // l lv v vr lvr
        //int[] array = {l, v, r, l+v, v+r, l+v+r};
//        int max = array[0];
//        for (int i = 1; i < array.length; i++) {
//            if (array[i] > max) {
//                max = array[i];
//            }
//        }
        int branchmax = Math.max(v, Math.max(v+l, v+r));
        int max = Math.max(l+v+r, Math.max(branchmax, Math.max(l, r)));

        fullmax = max > fullmax ? max : fullmax;

        return branchmax;

    }

}
