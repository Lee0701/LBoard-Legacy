package me.blog.hgl1002.lboard.expression;

import java.util.Map;

import me.blog.hgl1002.lboard.expression.nodes.TreeNode;

public interface TreeBuilder {
	public void setConstants(Map<String, Long> constants);
	public TreeNode build(Object o);
}
