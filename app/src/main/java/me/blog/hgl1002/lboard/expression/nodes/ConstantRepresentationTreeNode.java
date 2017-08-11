package me.blog.hgl1002.lboard.expression.nodes;

public class ConstantRepresentationTreeNode extends TreeNode {

	String name;

	public ConstantRepresentationTreeNode(String name) {
		super(Operator.NONE);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
