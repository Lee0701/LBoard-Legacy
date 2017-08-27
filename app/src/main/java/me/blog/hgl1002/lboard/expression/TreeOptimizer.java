package me.blog.hgl1002.lboard.expression;

import me.blog.hgl1002.lboard.expression.nodes.TreeNode;

public interface TreeOptimizer {
	public TreeNode optimize(TreeNode node);
}
