package me.blog.hgl1002.lboard;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import static me.blog.hgl1002.lboard.ime.charactergenerator.BasicCodeSystem.*;

public class ExpressionTest {

	@Test
	public void expressionTest() {
		TreeOptimizer optimizer = new RecursionTreeOptimizer();
		TreeBuilder builder = new StringRecursionTreeBuilder();
		builder.setConstants(CONSTANTS);
		TreeParser parser = new TreeParser();

//		parser.setVariables(new HashMap<String, Long>() {{put("a", 1L); put("b", 2L); put("c", 1L);}});
		long startTime;
		startTime = System.currentTimeMillis();
//		TreeNode node = builder.build("a = ((a = 2) + (a * (a += 2)) + a * 3)");
		TreeNode node = builder.build("H3|J_");
		System.out.println("Build finished in: " + (System.currentTimeMillis() - startTime) + " ms.");

		startTime = System.currentTimeMillis();
		node = optimizer.optimize(node);
		System.out.println("Optimization finished in: " + (System.currentTimeMillis() - startTime) + " ms.");

		startTime = System.currentTimeMillis();
		long result = parser.parse(node);
		System.out.println("Parse finished in: " + (System.currentTimeMillis() - startTime) + " ms.");
		System.out.println("result: " + result);

		try {
			TreeExporter exporter = new BinaryTreeExporter(new FileOutputStream("d:\\aoeu.bin"));
			exporter = new StringTreeExporter();
			((StringTreeExporter) exporter).setConstantHandler(new StringTreeExporter.ConstantHandler() {
				@Override
				public String onConstant(long constant) {
					String result = "0x" + Long.toHexString(constant);
					if(isH3(constant)) result = "H3";
					if(isH2(constant)) result = "H2";
					if(isH3(constant) || isH2(constant)) {
						if(hasCho(constant)) {
							result += "|";
							String cho = R_CONSTANTS.get(constant & MASK_CHO);
							if(cho == null) result += "0x" + Long.toHexString(constant & MASK_CHO);
							else result += cho;
						}
						if(hasJung(constant)) {
							result += "|";
							String jung = R_CONSTANTS.get(constant & MASK_JUNG);
							if(jung == null) result += "0x" + Long.toHexString(constant & MASK_JUNG);
							else result += jung;
						}
						if(hasJong(constant)) {
							result += "|";
							String jong = R_CONSTANTS.get(constant & MASK_JONG);
							if(jong == null) result += "0x" + Long.toHexString(constant & MASK_JONG);
							else result += jong;
						}
					}
					return result;
				}
			});
			startTime = System.currentTimeMillis();
			String str = (String) exporter.export(node);
			System.out.println("Export finished in: " + (System.currentTimeMillis() - startTime) + " ms.");
			System.out.println("Result: " + str);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
}
