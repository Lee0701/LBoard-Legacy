package me.blog.hgl1002.lboard.ime.hardkeyboard.basic;

import java.util.Map;

public class AutomataTable {

	protected Map<Long, AutomataRule> table;

	public AutomataTable() {

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
