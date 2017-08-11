package me.blog.hgl1002.lboard;

import org.junit.Test;

import java.util.HashMap;

import me.blog.hgl1002.lboard.expression.TreeBuilder;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;
import me.blog.hgl1002.lboard.expression.TreeParser;

public class ExpressionTest {

	@Test
	public void expressionTest() {
		TreeBuilder builder = new TreeBuilder();
		TreeParser parser = new TreeParser();
		parser.setVariables(new HashMap<String, Long>() {{put("a", 1L); put("b", 2L); put("c", 1L);}});
		TreeNode node = builder.build("(a=2)&(3|(a|=3))");
//		node = new BinaryTreeNode(
//			Operator.ASSIGNMENT,
//			new VariableTreeNode("a"),
//			new BinaryTreeNode(
//				Operator.ADDITION,
//				new BinaryTreeNode(
//					Operator.ADDITION,
//					new BinaryTreeNode(
//						Operator.ASSIGNMENT,
//						new VariableTreeNode("a"),
//						new ConstantTreeNode(2)
//					),
//					new BinaryTreeNode(
//						Operator.MULTIPLICATION,
//						new VariableTreeNode("a"),
//						new BinaryTreeNode(
//							Operator.ASSIGNMENT_ADDITION,
//							new VariableTreeNode("a"),
//							new ConstantTreeNode(2)
//						)
//					)
//				),
//				new BinaryTreeNode(
//					Operator.MULTIPLICATION,
//					new VariableTreeNode("a"),
//					new ConstantTreeNode(3)
//				)
//			)
//		);
		long result = parser.parse(node);
		System.out.println(result);
		System.out.println(parser.getVariables().get("a"));
	}
}
