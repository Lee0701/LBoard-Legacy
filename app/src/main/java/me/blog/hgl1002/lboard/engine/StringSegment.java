package me.blog.hgl1002.lboard.engine;

public class StringSegment {

	protected String string;
	protected int from;
	protected int to;

	public StringSegment() {
		this(null, -1, -1);
	}

	public StringSegment(String string) {
		this(string, -1, -1);
	}

	public StringSegment(char[] chars) {
		this(new String(chars), -1, -1);
	}

	public StringSegment(String string, int from, int to) {
		this.string = string;
		this.from = from;
		this.to = to;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}
}
