package me.blog.hgl1002.lboard.expression;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import me.blog.hgl1002.lboard.expression.nodes.*;

import static me.blog.hgl1002.lboard.expression.nodes.TreeNode.*;

public class BinaryIterationTreeBuilder implements TreeBuilder {

	@Override
	public TreeNode build(Object o) {
		if(o instanceof InputStream) {
			try {
				final InputStream is = (InputStream) o;
				byte[] bytes = new byte[is.available()];
				is.read(bytes);
				return build(ByteBuffer.wrap(bytes));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if(o instanceof byte[]) {
			byte[] bytes = (byte[]) o;
			return build(ByteBuffer.wrap(bytes));
		} else if(o instanceof ByteBuffer) {
			return build((ByteBuffer) o);
		} else {
			throw new RuntimeException("Type not supported.");
		}
	}

	private TreeNode build(final ByteBuffer bb) {
		return new Object() {
			Stack<TreeNode> operands = new Stack<TreeNode>();
			TreeNode build() {
				TreeNode current = null;
				while(bb.hasRemaining()) {
					byte type = bb.get();
					switch(type) {
					case 0: {
						break;
					}
					case TYPE_CONSTANT: {
						long value = bb.getLong();
						current = new ConstantTreeNode(value);
						operands.push(current);
						break;
					}
					case TYPE_CONSTANT_REP: {
						StringBuilder name = new StringBuilder();
						byte b;
						while((b = bb.get()) != 0) {
							name.append((char) b);
						}
						current = new ConstantRepresentationTreeNode(name.toString());
						operands.push(current);
						break;
					}
					case TYPE_VARIABLE: {
						StringBuilder name = new StringBuilder();
						byte b;
						while((b = bb.get()) != 0) {
							name.append((char) b);
						}
						current = new VariableTreeNode(name.toString());
						operands.push(current);
						break;
					}
					case TYPE_UNARY: {
						byte operator = bb.get();
						TreeNode center = operands.pop();
						current = new UnaryTreeNode(operator, center);
						operands.push(current);
						break;
					}
					case TYPE_BINARY: {
						byte operator = bb.get();
						TreeNode right = operands.pop(), left = operands.pop();
						current = new BinaryTreeNode(operator, left, right);
						operands.push(current);
						break;
					}
					case TYPE_TERNARY: {
						byte operator = bb.get();
						TreeNode right = operands.pop(), center = operands.pop(), left = operands.pop();
						current = new TernaryTreeNode(operator, left, center, right);
						operands.push(current);
						break;
					}
					case TYPE_LIST: {
						byte operator = bb.get();
						byte size = bb.get();
						List<TreeNode> nodes = new ArrayList<TreeNode>();
						for (byte i = 0 ; i < size ; i++) {
							nodes.add(0, operands.pop());
						}
						current = new ListTreeNode(operator, nodes);
						operands.push(current);
						break;
					}
					}
				}
				return current;
			}
		}.build();
	}

}
