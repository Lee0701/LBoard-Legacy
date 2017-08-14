package me.blog.hgl1002.lboard.ime.charactergenerator.basic;

import java.util.ArrayList;
import java.util.List;

public class CombinationTable {

	protected List<Combination> choCombination;
	protected List<Combination> jungCombination;
	protected List<Combination> jongCombination;

	public CombinationTable() {
		this.choCombination = new ArrayList<>();
		this.jungCombination = new ArrayList<>();
		this.jongCombination = new ArrayList<>();
	}

	public Combination getCho(int a, int b) {
		for(Combination combination : choCombination) {
			if(combination.a == a && combination.b == b) {
				return combination;
			}
		}
		return null;
	}

	public Combination getJung(int a, int b) {
		for(Combination combination : jungCombination) {
			if(combination.a == a && combination.b == b) {
				return combination;
			}
		}
		return null;
	}

	public Combination getJong(int a, int b) {
		for(Combination combination : jongCombination) {
			if(combination.a == a && combination.b == b) {
				return combination;
			}
		}
		return null;
	}

	public List<Combination> getAllCho() {
		return choCombination;
	}

	public List<Combination> getAllJung() {
		return jungCombination;
	}

	public List<Combination> getAllJong() {
		return jongCombination;
	}

}
