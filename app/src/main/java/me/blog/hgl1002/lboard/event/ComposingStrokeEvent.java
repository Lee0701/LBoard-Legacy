package me.blog.hgl1002.lboard.event;

public abstract class ComposingStrokeEvent extends LBoardEvent {

	String composingStroke;

	public ComposingStrokeEvent(String composingStroke) {
		this.composingStroke = composingStroke;
	}

	public String getComposingStroke() {
		return composingStroke;
	}
}
