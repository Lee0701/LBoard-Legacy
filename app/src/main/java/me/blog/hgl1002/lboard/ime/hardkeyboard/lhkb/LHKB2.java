package me.blog.hgl1002.lboard.ime.hardkeyboard.lhkb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import me.blog.hgl1002.lboard.expression.BinaryIterationTreeBuilder;
import me.blog.hgl1002.lboard.expression.TreeBuilder;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;

public class LHKB2 {

	public static final String LAYOUT_MAGIC_NUMBER = "LHKB2";
	public static final int MAPPINGS_SIZE = 0x100;

	public static TreeNode[][] loadMappings(InputStream inputStream) {
		try {
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			return loadMappings(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TreeNode[][] loadMappings(ByteBuffer buffer) {
		for (int i = 0 ; i < LAYOUT_MAGIC_NUMBER.length() ; i++) {
			char c = (char) buffer.get();
			if (c != LAYOUT_MAGIC_NUMBER.charAt(i)) {
				throw new RuntimeException("LHKB1 layout file must start with String \"" + LAYOUT_MAGIC_NUMBER + "\"!");
			}
		}
		for (int i = 0 ; i < 0x10 - LAYOUT_MAGIC_NUMBER.length() ; i++) {
			buffer.get();
		}
		TreeNode[][] layout = new TreeNode[MAPPINGS_SIZE][2];
		TreeBuilder builder = new BinaryIterationTreeBuilder();
		for(int i = 0 ; i < layout.length ; i++) {
			if(buffer.remaining() <= 0) break;
			for(int j = 0 ; j < layout[i].length ; j++) {
				layout[i][j] = builder.build(buffer);
			}
		}
		return layout;
	}

}
