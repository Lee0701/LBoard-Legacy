package me.blog.hgl1002.lboard.ime.hardkeyboard;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;

public class DefaultHardKeyboard implements HardKeyboard {

	protected LBoard parent;

	protected boolean consumeDownEvent;

	protected boolean shiftPressing;
	protected boolean altPressing;

	public DefaultHardKeyboard(LBoard parent) {
		this.parent = parent;
	}

	@Override
	public boolean onKeyEvent(KeyEvent event, KeyEventInfo info) {
		switch (event.getAction()) {
		case KeyEvent.ACTION_UP:
			if(!consumeDownEvent) return false;
			else onKeyUp(event, info);
			return true;

		case KeyEvent.ACTION_DOWN:
			consumeDownEvent = onKeyDown(event, info);
			return consumeDownEvent;
		}
		return false;
	}

	public boolean onKeyDown(KeyEvent event, KeyEventInfo info) {
		InputConnection ic = parent.getCurrentInputConnection();
		EditorInfo editorInfo = parent.getCurrentInputEditorInfo();
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_SPACE:
			return false;

		case KeyEvent.KEYCODE_DEL:
			return false;

		case KeyEvent.KEYCODE_ENTER:
			return false;

		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			shiftPressing = true;
			return true;

		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
			altPressing = true;
			return true;

		default:
			if(event.isPrintingKey()) {
				char keyChar = (char) event.getUnicodeChar(
						(shiftPressing || event.isShiftPressed() ? KeyEvent.META_SHIFT_ON : 0) | (altPressing ? KeyEvent.META_ALT_ON : 0));
				ic.commitText(new String(new char[] {keyChar}), 1);
				return true;
			}
		}
		return false;
	}

	public boolean onKeyUp(KeyEvent event, KeyEventInfo info) {
		switch(event.getKeyCode()) {
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			shiftPressing = false;
			return true;

		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
			altPressing = false;
			return true;

		}
		return true;
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void closing() {

	}
}
