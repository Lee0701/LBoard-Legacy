package me.blog.hgl1002.lboard;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import me.blog.hgl1002.lboard.expression.BinaryIterationTreeBuilder;
import me.blog.hgl1002.lboard.expression.StringRecursionTreeBuilder;
import me.blog.hgl1002.lboard.expression.TreeBuilder;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;
import me.blog.hgl1002.lboard.expression.TreeParser;

public class ExpressionTest {

	@Test
	public void expressionTest() {
		TreeBuilder builder = new StringRecursionTreeBuilder();
		TreeParser parser = new TreeParser();

//		parser.setVariables(new HashMap<String, Long>() {{put("a", 1L); put("b", 2L); put("c", 1L);}});
		int a;
		long startTime;
		startTime = System.currentTimeMillis();
		TreeNode node = builder.build("(10, a) = 10");
		System.out.println("Build finished in: " + (System.currentTimeMillis() - startTime) + " ms.");

		startTime = System.currentTimeMillis();
		long result = parser.parse(node);
		System.out.println("Parse finished in: " + (System.currentTimeMillis() - startTime) + " ms.");
		System.out.println(result);

		builder = new BinaryIterationTreeBuilder();

		startTime = System.currentTimeMillis();
		try {
			node = builder.build(new FileInputStream("d:\\asdf.bin"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Build finished in: " + (System.currentTimeMillis() - startTime) + " ms.");
		startTime = System.currentTimeMillis();
		result = parser.parse(node);
		System.out.println("Parse finished in: " + (System.currentTimeMillis() - startTime) + " ms.");
		System.out.println(result);

	}
}
