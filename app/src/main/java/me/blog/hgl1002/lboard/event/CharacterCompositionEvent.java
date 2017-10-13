package me.blog.hgl1002.lboard.event;

public class CharacterCompositionEvent extends LBoardEvent {

	protected String composing, composingStroke;

	public CharacterCompositionEvent(String composing, String composingStroke) {
		this.composing = composing;
		this.composingStroke = composingStroke;
	}

	public String getComposing() {
		return composing;
	}

	public String getComposingStroke() {
		return composingStroke;
	}
}
