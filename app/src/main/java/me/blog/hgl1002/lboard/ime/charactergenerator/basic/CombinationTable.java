package me.blog.hgl1002.lboard.ime.charactergenerator.basic;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CombinationTable {

	public static final String MAGIC_NUMBER = "LCOM2";

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

	public static CombinationTable load(InputStream in) throws IOException {
		DataInputStream dis;
		if(in instanceof DataInputStream) dis = (DataInputStream) in;
		else dis = new DataInputStream(in);

		CombinationTable combinations = new CombinationTable();

		for(char c : MAGIC_NUMBER.toCharArray()) {
			char d = (char) dis.readByte();
			if(d != c) throw new RuntimeException("Combination file must start with String \"" + MAGIC_NUMBER + "\"!");
		}
		for(int i = MAGIC_NUMBER.length() ; i < 0x10 ; i++) {
			dis.readByte();
		}
		while(dis.available() >= 8) {
			int type = dis.readShort();
			int a = dis.readShort();
			int b = dis.readShort();
			int result = dis.readShort();
			switch(type) {
			case Combination.TYPE_CHO:
				combinations.choCombination.add(new Combination(type, a, b, result));
				break;

			case Combination.TYPE_JUNG:
				combinations.jungCombination.add(new Combination(type, a, b, result));
				break;

			case Combination.TYPE_JONG:
				combinations.jongCombination.add(new Combination(type, a, b, result));
				break;
			}
		}

		return combinations;
	}

}
