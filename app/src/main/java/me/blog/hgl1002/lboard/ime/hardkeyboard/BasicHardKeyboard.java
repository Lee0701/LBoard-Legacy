package me.blog.hgl1002.lboard.ime.hardkeyboard;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import java.util.Map;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.expression.TreeParser;
import me.blog.hgl1002.lboard.expression.nodes.TreeNode;
import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.LBoardInputMethod;
import me.blog.hgl1002.lboard.ime.charactergenerator.BasicCharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.BasicCodeSystem;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

public class BasicHardKeyboard implements HardKeyboard {

	protected LBoard parent;
	protected LBoardInputMethod method;

	protected TreeParser parser;

	TreeNode[][] mappings;

	protected CharacterGenerator characterGenerator;

	protected boolean consumeDownEvent;

	protected boolean shiftPressing;
	protected boolean altPressing;

	public BasicHardKeyboard(LBoard parent) {
		this.parent = parent;
		this.parser = new TreeParser();
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
				if (mappings != null && characterGenerator != null) {
					int keyCode = event.getKeyCode();
					if (keyCode >= 0 && keyCode < mappings.length) {
						TreeNode node = mappings[keyCode][shiftPressing ? 1 : 0];
						long result = 0;
						Map<String, Long> variables = characterGenerator.getVariables();
						parser.setVariables(variables);
						if(node != null) result = parser.parse(node);
						if (result != 0) {
							boolean ret = characterGenerator.onCode(result);
							if (!ret) {
								parent.finishComposing();
								parent.commitText(new String(new char[]{(char) result}));
							}
						}
						updateLabels();
						return true;
					}
				}
				char keyChar = (char) event.getUnicodeChar(
						(shiftPressing || event.isShiftPressed() ? KeyEvent.META_SHIFT_ON : 0) | (altPressing ? KeyEvent.META_ALT_ON : 0));
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

	public void updateLabels() {
		if(method == null) return;
		int shift = shiftPressing ? 1 : 0;
		Map<String, Long> variables = characterGenerator.getVariables();
		parser.setVariables(variables);
		if(method.getSoftKeyboard() instanceof DefaultSoftKeyboard) {
			CharSequence[][] labels = new CharSequence[mappings.length][2];
			if(characterGenerator instanceof BasicCharacterGenerator) {
				BasicCharacterGenerator generator = (BasicCharacterGenerator) characterGenerator;
				for (int i = 0 ; i < mappings.length ; i++) {
					if (mappings[i][shift] == null) continue;
					long result = parser.parse(mappings[i][shift]);
					result = generator.replaceVirtuals(result);
					labels[i][shift] = BasicCodeSystem.convertToUnicode(result);
				}
			} else {
				for (int i = 0 ; i < mappings.length ; i++) {
					if (mappings[i][shift] == null) continue;
					labels[i][shift] = BasicCodeSystem.convertToUnicode(parser.parse(mappings[i][shift]));
				}
			}
			((DefaultSoftKeyboard) method.getSoftKeyboard()).setLabels(labels);
			((DefaultSoftKeyboard) method.getSoftKeyboard()).updateLabels();
		}
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void closing() {

	}

	public TreeNode[][] getMappings() {
		return mappings;
	}

	public void setMappings(TreeNode[][] mappings) {
		this.mappings = mappings;
	}

	public CharacterGenerator getCharacterGenerator() {
		return characterGenerator;
	}

	public void setCharacterGenerator(CharacterGenerator characterGenerator) {
		this.characterGenerator = characterGenerator;
	}

	@Override
	public void setMethod(LBoardInputMethod method) {
		this.method = method;
	}

	public TreeParser getParser() {
		return parser;
	}

	public void setParser(TreeParser parser) {
		this.parser = parser;
	}
}
