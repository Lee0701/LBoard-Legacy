package me.blog.hgl1002.lboard.expression;

import me.blog.hgl1002.lboard.expression.nodes.BinaryTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.ConstantTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.Operator;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;
import me.blog.hgl1002.lboard.expression.nodes.TrinominalTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.UnaryTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.VariableTreeNode;

public interface TreeBuilder {
	public TreeNode build(Object o);
}
