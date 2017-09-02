package me.blog.hgl1002.lboard.ime.hardkeyboard;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.LBoardInputMethod;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;

public class DefaultHardKeyboard implements HardKeyboard {

	protected LBoard parent;
	protected LBoardInputMethod method;

	protected long[][] mappings;

	protected CharacterGenerator characterGenerator;

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
			if (!consumeDownEvent) return false;
			else onKeyUp(event, info);
			return true;

		case KeyEvent.ACTION_DOWN:
			consumeDownEvent = onKeyDown(event, info);
			return consumeDownEvent;
		}
		return false;
	}

	public boolean onKeyDown(KeyEvent event, KeyEventInfo info) {
		EditorInfo editorInfo = parent.getCurrentInputEditorInfo();
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_SPACE:
			parent.finishComposing();
			return false;

		case KeyEvent.KEYCODE_DEL:
			return false;

		case KeyEvent.KEYCODE_ENTER:
			parent.finishComposing();
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
			if (event.isPrintingKey()) {
				char keyChar = (char) event.getUnicodeChar(
						(shiftPressing || event.isShiftPressed() ? KeyEvent.META_SHIFT_ON : 0) | (altPressing ? KeyEvent.META_ALT_ON : 0));
				if (mappings != null && characterGenerator != null) {
					int keyCode = event.getKeyCode();
					if (keyCode >= 0 && keyCode < mappings.length) {
						long mappedCode = mappings[keyCode][shiftPressing ? 1 : 0];
						if (mappedCode != 0) {
							boolean ret = characterGenerator.onCode(mappedCode);
							if (ret) {
								parent.appendStroke(new String(Character.toChars(keyChar)));
							} else {
								parent.finishComposing();
								parent.commitText(new String(new char[]{(char) mappedCode}));
							}
						}
						return true;
					}
				}
				parent.finishComposing();
				parent.commitText(new String(new char[]{keyChar}));
				return true;
			}
		}
		return false;
	}

	public boolean onKeyUp(KeyEvent event, KeyEventInfo info) {
		switch (event.getKeyCode()) {
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

	public long[][] getMappings() {
		return mappings;
	}

	public void setMappings(long[][] mappings) {
		this.mappings = mappings;
	}

	@Override
	public void setMethod(LBoardInputMethod method) {
		this.method = method;
	}

	public CharacterGenerator getCharacterGenerator() {
		return characterGenerator;
	}

	public void setCharacterGenerator(CharacterGenerator characterGenerator) {
		this.characterGenerator = characterGenerator;
	}
}
