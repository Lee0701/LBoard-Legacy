package me.blog.hgl1002.lboard.expression;

public class UnaryTreeNode extends TreeNode {

	TreeNode center;

	public UnaryTreeNode(Operator operator, TreeNode center) {
		super(operator);
		this.center = center;
	}

	public TreeNode getCenter() {
		return center;
	}

	public void setCenter(TreeNode center) {
		this.center = center;
	}
}
