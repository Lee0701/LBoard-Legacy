package me.blog.hgl1002.lboard.ime;

import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;

public class LBoardInputMethod {
	String name;

	protected SoftKeyboard softKeyboard;
	protected HardKeyboard hardKeyboard;
	protected CharacterGenerator characterGenerator;

	public LBoardInputMethod(String name, SoftKeyboard softKeyboard, HardKeyboard hardKeyboard, CharacterGenerator characterGenerator) {
		this.name = name;
		this.softKeyboard = softKeyboard;
		this.hardKeyboard = hardKeyboard;
		this.characterGenerator = characterGenerator;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SoftKeyboard getSoftKeyboard() {
		return softKeyboard;
	}

	public void setSoftKeyboard(SoftKeyboard softKeyboard) {
		this.softKeyboard = softKeyboard;
	}

	public HardKeyboard getHardKeyboard() {
		return hardKeyboard;
	}

	public void setHardKeyboard(HardKeyboard hardKeyboard) {
		this.hardKeyboard = hardKeyboard;
	}

	public CharacterGenerator getCharacterGenerator() {
		return characterGenerator;
	}

	public void setCharacterGenerator(CharacterGenerator characterGenerator) {
		this.characterGenerator = characterGenerator;
	}
}