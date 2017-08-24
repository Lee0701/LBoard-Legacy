package me.blog.hgl1002.lboard.ime.charactergenerator.basic;

import me.blog.hgl1002.lboard.expression.nodes.TreeNode;

public class AutomataRule {
	protected long initialState;
	protected TreeNode targetState;
	protected long failState;
	protected String description;

	public AutomataRule(long initialState, TreeNode targetState, long failState, String description) {
		this.initialState = initialState;
		this.targetState = targetState;
		this.failState = failState;
		this.description = description;
	}

	public AutomataRule() {

	}

	public long getInitialState() {
		return initialState;
	}

	public void setInitialState(long initialState) {
		this.initialState = initialState;
	}

	public TreeNode getTargetState() {
		return targetState;
	}

	public void setTargetState(TreeNode targetState) {
		this.targetState = targetState;
	}

	public long getFailState() {
		return failState;
	}

	public void setFailState(long failState) {
		this.failState = failState;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
