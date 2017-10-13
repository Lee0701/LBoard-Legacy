package me.blog.hgl1002.lboard.event;

public class CharacterCompositionEvent extends LBoardEvent {

	protected String composing;

	public CharacterCompositionEvent(String composing) {
		this.composing = composing;
	}

	public String getComposing() {
		return composing;
	}

}
