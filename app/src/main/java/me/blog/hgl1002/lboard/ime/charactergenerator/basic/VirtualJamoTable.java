package me.blog.hgl1002.lboard.ime.charactergenerator.basic;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VirtualJamoTable {

	public static final String MAGIC_NUMBER = "LVJT2";

	protected List<VirtualJamo> choVirtuals;
	protected List<VirtualJamo> jungVirtuals;
	protected List<VirtualJamo> jongVirtuals;

	public VirtualJamoTable() {
		this.choVirtuals = new ArrayList<>();
		this.jungVirtuals = new ArrayList<>();
		this.jongVirtuals = new ArrayList<>();
	}

	public VirtualJamo getCho(int a) {
		for(VirtualJamo item : choVirtuals) {
			if(item.a == a) {
				return item;
			}
		}
		return null;
	}

	public VirtualJamo getJung(int a, int b) {
		for(VirtualJamo item : jungVirtuals) {
			if(item.a == a) {
				return item;
			}
		}
		return null;
	}

	public VirtualJamo getJong(int a, int b) {
		for(VirtualJamo item : jongVirtuals) {
			if(item.a == a) {
				return item;
			}
		}
		return null;
	}

	public static VirtualJamoTable load(InputStream in) throws IOException {
		DataInputStream dis;
		if(in instanceof DataInputStream) dis = (DataInputStream) in;
		else dis = new DataInputStream(in);

		VirtualJamoTable table = new VirtualJamoTable();

		for(char c : MAGIC_NUMBER.toCharArray()) {
			char d = (char) dis.readByte();
			if(d != c) throw new RuntimeException("Virtual Table file must start with String \"" + MAGIC_NUMBER + "\"!");
		}
		for(int i = MAGIC_NUMBER.length() ; i < 0x10 ; i++) {
			dis.readByte();
		}
		while(dis.available() >= 8) {
			int type = dis.readShort();
			int a = dis.readShort();
			int result = dis.readShort();
			dis.readShort();
			switch(type) {
			case Combination.TYPE_CHO:
				table.choVirtuals.add(new VirtualJamo(type, a, result));
				break;

			case Combination.TYPE_JUNG:
				table.jungVirtuals.add(new VirtualJamo(type, a, result));
				break;

			case Combination.TYPE_JONG:
				table.jongVirtuals.add(new VirtualJamo(type, a, result));
				break;
			}
		}

		return table;
	}

}
