package me.blog.hgl1002.lboard.engine;

public class Word {

	String candidate;
	String stroke;
	int frequency;
	int attribute;

	public Word() {
		this("", "", 0, 0);
	}

	public Word(String candidate, String stroke) {
		this(candidate, stroke, 0, 0);
	}

	public Word(String candidate, String stroke, int frequency) {
		this(candidate, stroke, frequency, 0);
	}

	public Word(String candidate, String stroke, int frequency, int attribute) {
		this.candidate = candidate;
		this.stroke = stroke;
		this.frequency = frequency;
		this.attribute = attribute;
	}

	@Override
	public String toString() {
		return candidate;
	}

	public String getCandidate() {
		return candidate;
	}

	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}

	public String getStroke() {
		return stroke;
	}

	public void setStroke(String stroke) {
		this.stroke = stroke;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}
}
