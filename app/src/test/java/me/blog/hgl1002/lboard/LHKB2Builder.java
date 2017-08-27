package me.blog.hgl1002.lboard;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import me.blog.hgl1002.lboard.expression.BinaryIterationTreeBuilder;
import me.blog.hgl1002.lboard.expression.BinaryTreeExporter;
import me.blog.hgl1002.lboard.expression.RecursionTreeOptimizer;
import me.blog.hgl1002.lboard.expression.StringRecursionTreeBuilder;
import me.blog.hgl1002.lboard.expression.StringTreeExporter;
import me.blog.hgl1002.lboard.expression.TreeBuilder;
import me.blog.hgl1002.lboard.expression.TreeExporter;
import me.blog.hgl1002.lboard.expression.TreeOptimizer;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;
import me.blog.hgl1002.lboard.expression.TreeParser;
import me.blog.hgl1002.lboard.ime.charactergenerator.BasicCodeSystem;

import static me.blog.hgl1002.lboard.ime.charactergenerator.BasicCodeSystem.*;

public class LHKB2Builder {

	@Test
	public void expressionTest() {
		File input = new File("D:/input.txt");
		File output = new File("D:/layout_shin_1995.lhkb");

		StringRecursionTreeBuilder builder = new StringRecursionTreeBuilder();
		builder.setConstants(BasicCodeSystem.CONSTANTS);

		TreeNode[][] mappings = new TreeNode[0x100][2];

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			String line;
			while((line = br.readLine()) != null) {
				String[] keyValue = line.split("\t");
				int code = Integer.parseInt(keyValue[0]);
				TreeNode normal = builder.build(keyValue[1]);
				TreeNode shift = builder.build(keyValue[2]);
				mappings[code][0] = normal;
				mappings[code][1] = shift;
			}

			DataOutputStream dos = new DataOutputStream(new FileOutputStream(output));
			BinaryTreeExporter exporter = new BinaryTreeExporter(dos);

			for(char c : "LHKB2".toCharArray()) {
				dos.writeByte((byte) c);
			}
			for(int i = "LHKB2".length() ; i < 0x10 ; i++) {
				dos.writeByte(0x00);
			}

			for(int i = 0 ; i < mappings.length ; i++) {
				TreeNode normal = mappings[i][0];
				TreeNode shift = mappings[i][1];
				if(normal != null) exporter.export(normal);
				dos.writeByte(0);
				if(shift != null) exporter.export(shift);
				dos.writeByte(0);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
