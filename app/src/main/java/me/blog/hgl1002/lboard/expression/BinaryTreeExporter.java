package me.blog.hgl1002.lboard.expression;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import me.blog.hgl1002.lboard.expression.nodes.*;

import static me.blog.hgl1002.lboard.expression.nodes.TreeNode.*;

public class BinaryTreeExporter implements TreeExporter {

	OutputStream out;

	public BinaryTreeExporter(OutputStream out) {
		this.out = out;
	}

	@Override
	public Object export(TreeNode node) {
		return export(node, out);
	}

	private Object export(final TreeNode node, final OutputStream out) {
		return new Object() {

			DataOutputStream dos = new DataOutputStream(out);

			public Object export() {
				try {
					export(node);
				} catch(Exception e) {
					return e;
				}
				return 1;
			}

			public void export(TreeNode node) throws IOException {
				if (node instanceof ConstantTreeNode) {
					dos.writeByte(TreeNode.TYPE_CONSTANT);
					dos.writeLong(((ConstantTreeNode) node).getValue());
				} else if (node instanceof VariableTreeNode) {
					dos.writeByte(TreeNode.TYPE_VARIABLE);
					for(char c : ((VariableTreeNode) node).getName().toCharArray()) {
						dos.writeByte((byte) c);
					}
					dos.writeByte(0);
				} else if (node instanceof UnaryTreeNode) {
					UnaryTreeNode unaryTreeNode = (UnaryTreeNode) node;
					export(unaryTreeNode.getCenter());
					dos.writeByte(TreeNode.TYPE_UNARY);
					dos.writeByte(unaryTreeNode.getOperator());
				} else if (node instanceof BinaryTreeNode) {
					BinaryTreeNode binaryTreeNode = (BinaryTreeNode) node;
					export(binaryTreeNode.getLeft());
					export(binaryTreeNode.getRight());
					dos.writeByte(TreeNode.TYPE_BINARY);
					dos.writeByte(binaryTreeNode.getOperator());
				} else if (node instanceof TernaryTreeNode) {
					TernaryTreeNode ternaryTreeNode = (TernaryTreeNode) node;
					export(ternaryTreeNode.getLeft());
					export(ternaryTreeNode.getCenter());
					export(ternaryTreeNode.getRight());
					dos.writeByte(TreeNode.TYPE_TERNARY);
					dos.write(ternaryTreeNode.getOperator());
				} else if (node instanceof ListTreeNode) {
					ListTreeNode listTreeNode = (ListTreeNode) node;
					List<TreeNode> nodes = listTreeNode.getNodes();
					dos.writeByte(nodes.size());
					for(TreeNode n : nodes) {
						export(n);
					}
					dos.write(TreeNode.TYPE_LIST);
					dos.write(listTreeNode.getOperator());
				} else {
					throw new RuntimeException("Unsupported node type.");
				}
			}

		}.export();
	}

}
