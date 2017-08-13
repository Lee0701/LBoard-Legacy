package me.blog.hgl1002.lboard.ime.hardkeyboard;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;

public class EmptyHardKeyboard implements HardKeyboard {

	protected LBoard parent;

	protected boolean shiftPressing;

	public EmptyHardKeyboard(LBoard parent) {
		this.parent = parent;
	}

	@Override
	public boolean onKeyEvent(KeyEvent event, KeyEventInfo info) {
		switch (event.getAction()) {
		case KeyEvent.ACTION_UP:
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				shiftPressing = false;
				break;
			}
			break;

		case KeyEvent.ACTION_DOWN:
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				shiftPressing = true;
				break;
			}
			break;
		}
		if(shiftPressing) {
			event = new KeyEvent(event.getDownTime(), event.getEventTime(),
					event.getAction(), event.getKeyCode(), event.getRepeatCount(),
					event.getMetaState() | KeyEvent.META_SHIFT_ON,
					event.getDeviceId(), event.getScanCode(),
					event.getFlags(), event.getSource());
		}
		parent.getCurrentInputConnection().sendKeyEvent(event);
		return true;
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void closing() {

	}
}
