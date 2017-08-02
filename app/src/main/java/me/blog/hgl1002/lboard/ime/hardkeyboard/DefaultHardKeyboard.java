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
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;

public class DefaultHardKeyboard implements HardKeyboard {

	public static final String LAYOUT_MAGIC_NUMBER = "LHKB1";
	public static final int MAPPINGS_SIZE = 0x100;

	protected LBoard parent;

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
			if(event.isPrintingKey()) {
				if(mappings != null && characterGenerator != null) {
					int keyCode = event.getKeyCode();
					if(keyCode >= 0 && keyCode < MAPPINGS_SIZE) {
						long mappedCode = mappings[keyCode][shiftPressing ? 1 : 0];
						if(mappedCode != 0) {
							boolean ret = characterGenerator.onCode(mappedCode);
							if(!ret) {
								parent.finishComposing();
								parent.commitText(new String(new char[] {(char) mappedCode}));
							}
						}
						return true;
					}
				}
				char keyChar = (char) event.getUnicodeChar(
						(shiftPressing || event.isShiftPressed() ? KeyEvent.META_SHIFT_ON : 0) | (altPressing ? KeyEvent.META_ALT_ON : 0));
				parent.finishComposing();
				parent.commitText(new String(new char[] {keyChar}));
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

	public static long[][] loadMappings(InputStream inputStream) {
		try {
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			for(int i = 0 ; i < LAYOUT_MAGIC_NUMBER.length() ; i++) {
				char c = (char) buffer.get();
				if(c != LAYOUT_MAGIC_NUMBER.charAt(i)) {
					throw new RuntimeException("Layout file must start with String \"" + LAYOUT_MAGIC_NUMBER + "\"!");
				}
			}
			for(int i = 0 ; i < 0x10 - LAYOUT_MAGIC_NUMBER.length() ; i++) {
				buffer.get();
			}
			long[][] layout = new long[MAPPINGS_SIZE][2];
			for(int i = 0 ; i < layout.length ; i++) {
				if(buffer.remaining() < 0x08) break;
				long normal = buffer.getLong();
				long shift = buffer.getLong();
				layout[i][0] = normal;
				layout[i][1] = shift;
			}
			return layout;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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

	public CharacterGenerator getCharacterGenerator() {
		return characterGenerator;
	}

	public void setCharacterGenerator(CharacterGenerator characterGenerator) {
		this.characterGenerator = characterGenerator;
	}
}
