package me.blog.hgl1002.lboard.ime.charactergenerator.basic;

public class Combination {

	public static final int TYPE_CHO = 1;
	public static final int TYPE_JUNG = 2;
	public static final int TYPE_JONG = 3;

	protected int type;
	protected int a, b;
	protected int result;

	public Combination(int type, int a, int b, int result) {
		this.type = type;
		this.a = a;
		this.b = b;
		this.result = result;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
}
