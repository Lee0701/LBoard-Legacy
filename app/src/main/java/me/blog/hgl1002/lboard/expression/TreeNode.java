package me.blog.hgl1002.lboard.expression;

public abstract class TreeNode {
	Operator operator;
	public TreeNode(Operator operator) {
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
}
