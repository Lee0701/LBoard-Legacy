package me.blog.hgl1002.lboard.expression;

import me.blog.hgl1002.lboard.expression.nodes.BinaryTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.ConstantTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.ListTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.TernaryTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;
import me.blog.hgl1002.lboard.expression.nodes.UnaryTreeNode;
import me.blog.hgl1002.lboard.expression.nodes.VariableTreeNode;

import static me.blog.hgl1002.lboard.expression.nodes.Operator.*;

public class RecursionTreeOptimizer implements TreeOptimizer {
	@Override
	public TreeNode optimize(TreeNode node) {
		if (node instanceof ConstantTreeNode) {
			return node;
		} else if (node instanceof VariableTreeNode) {
			return node;
		} else if (node instanceof UnaryTreeNode) {
			UnaryTreeNode unaryTreeNode = (UnaryTreeNode) node;
			return optimizeUnary(unaryTreeNode);
		} else if (node instanceof BinaryTreeNode) {
			BinaryTreeNode binaryTreeNode = (BinaryTreeNode) node;
			return optimizeBinary(binaryTreeNode);
		} else if (node instanceof TernaryTreeNode) {
			return node;
		} else if (node instanceof ListTreeNode) {
			return node;
		} else {
			throw new RuntimeException("Unsupported node type.");
		}
	}

	public TreeNode optimizeUnary(UnaryTreeNode node) {
		if(!(node.getCenter() instanceof ConstantTreeNode)) {
			return node;
		}
		long center = ((ConstantTreeNode) node.getCenter()).getValue();
		long result;
		switch(node.getOperator()) {
		case PLUS:
			result = center;
			break;

		case MINUS:
			result = -center;
			break;

		case NOT:
			result = (center == 0) ? 1 : 0;
			break;

		case INVERT:
			result = ~center;
			break;

		default:
			return node;
		}
		return new ConstantTreeNode(result);
	}

	public TreeNode optimizeBinary(BinaryTreeNode node) {
		if(!(node.getLeft() instanceof ConstantTreeNode
				&& node.getRight() instanceof ConstantTreeNode)) {
			return node;
		}
		long left = ((ConstantTreeNode) node.getLeft()).getValue();
		long right = ((ConstantTreeNode) node.getRight()).getValue();
		long result;
		switch(node.getOperator()) {
		case ADDITION:
			result = left + right;
			break;

		case SUBTRACTION:
			result = left - right;
			break;

		case MULTIPLICATION:
			result = left * right;
			break;

		case DIVISION:
			result = left / right;
			break;

		case MOD:
			result = left % right;
			break;

		case SHIFT_LEFT:
			result = left << right;
			break;

		case SHIFT_RIGHT:
			result = left >> right;
			break;

		case COMPARE_GREATER:
			result = (left > right) ? 1 : 0;
			break;

		case COMPARE_SMALLER:
			result = (left < right) ? 1 : 0;
			break;

		case COMPARE_GREATER_OR_EQUAL:
			result = (left >= right) ? 1 : 0;
			break;

		case COMPARE_SMALLER_OR_EQUAL:
			result = (left <= right) ? 1 : 0;
			break;

		case EQUALS:
			result = (left == right) ? 1 : 0;
			break;

		case NOT_EQUALS:
			result = (left != right) ? 1 : 0;
			break;

		case BITWISE_AND:
			result = left & right;
			break;

		case BITWISE_OR:
			result = left | right;
			break;

		case BITWISE_XOR:
			result = left ^ right;
			break;

		case LOGICAL_AND:
			result = ((left & right) == 0) ? 0 : 1;
			break;

		case LOGICAL_OR:
			result = ((left | right) == 0) ? 0 : 1;
			break;

		default:
			return node;
		}
		return new ConstantTreeNode(result);
	}

}
