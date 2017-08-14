package me.blog.hgl1002.lboard.ime.charactergenerator.basic;

import java.util.Map;

import me.blog.hgl1002.lboard.expression.nodes.ConstantTreeNode;

public class AutomataTable {

	protected Map<Long, AutomataRule> table;

	public AutomataTable() {
		AutomataRule initial = new AutomataRule();
		initial.setInitialState(0);
		initial.setTargetState(new ConstantTreeNode(0L));
		table.put(0L, initial);
	}

	public AutomataTable(AutomataRule initial) {
		table.put(0L, initial);
	}

	public AutomataRule get(long index) {
		return table.get(index);
	}

	public void set(long index, AutomataRule rule) {
		table.put(index, rule);
	}

	public Map<Long, AutomataRule> getAll() {
		return table;
	}

}
