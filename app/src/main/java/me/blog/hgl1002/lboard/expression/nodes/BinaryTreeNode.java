package me.blog.hgl1002.lboard.expression.nodes;

public class BinaryTreeNode extends TreeNode {

	TreeNode left, right;

	public BinaryTreeNode(Operator operator, TreeNode left, TreeNode right) {
		super(operator);
		this.left = left;
		this.right = right;
	}

	public TreeNode getLeft() {
		return left;
	}

	public void setLeft(TreeNode left) {
		this.left = left;
	}

	public TreeNode getRight() {
		return right;
	}

	public void setRight(TreeNode right) {
		this.right = right;
	}
}
