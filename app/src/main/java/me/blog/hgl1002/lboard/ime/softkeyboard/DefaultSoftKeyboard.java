package me.blog.hgl1002.lboard.ime.softkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.R;
import me.blog.hgl1002.lboard.ime.SoftKeyboard;

public class DefaultSoftKeyboard implements SoftKeyboard, KeyboardView.OnKeyboardActionListener, DefaultSoftKeyboardView.OnAdvancedActionListener {

	public static final int SHIFT_OFF = 0;
	public static final int SHIFT_ON = 1;

	LBoard parent;

	/**
	 * 키보드 뷰들을 포함하는 메인 뷰.
	 */
	protected ViewGroup mainView;
	/**
	 * 메인 키보드 뷰. 예: 한글 키보드, 영문 키보드.
	 */
	protected DefaultSoftKeyboardView mainKeyboardView;
	/**
	 * 상단 키보드 뷰. 예: 숫자열 키보드, 커서 이동 키보드.
	 */
	protected DefaultSoftKeyboardView upperKeyboardView;
	/**
	 * 하단 키보드 뷰. 예: 스페이스바, 언어 전환 키 등을 포함하는 열.
	 */
	protected DefaultSoftKeyboardView lowerKeyboardView;
	/**
	 * 상태 뷰. 예: 한/영 상태 표시기, Shift/Alt 키 표시기.
	 */
	View statusView;

	protected Keyboard currentMainKeyboard;
	protected Keyboard currentUpperKeyboard;
	protected Keyboard currentLowerKeyboard;

	protected Keyboard[] mainKeyboards;
	protected Keyboard[] upperKeyboards;
	protected Keyboard[] lowerKeyboards;

	protected CharSequence[][] labels;

	protected boolean shiftPressed;
	protected boolean shiftLocked;

	protected long pressTime;

	protected boolean ignore;

	public DefaultSoftKeyboard(LBoard parent) {
		this.parent = parent;
	}

	@Override
	public View createView(Context context) {
		LinearLayout mainView = new LinearLayout(context);
		mainView.setOrientation(LinearLayout.VERTICAL);

		mainKeyboardView = new DefaultSoftKeyboardView(context, null);
		lowerKeyboardView = new DefaultSoftKeyboardView(context, null);

		mainKeyboardView.setOnKeyboardActionListener(this);
		lowerKeyboardView.setOnKeyboardActionListener(this);
		mainKeyboardView.setOnAdvancedActionListener(this);
		lowerKeyboardView.setOnAdvancedActionListener(this);

		mainView.addView(mainKeyboardView);
		mainView.addView(lowerKeyboardView);

		this.mainView = mainView;

		this.shiftPressed = false;
		this.shiftLocked = false;

		updateLabels();
		setDefaultKeyboards();

		return mainView;
	}

	public void updateLabels() {
		if(labels == null) return;
		for(int shift = 0 ; shift < 2 ; shift++) {
			for (Keyboard.Key key : mainKeyboards[shift].getKeys()) {
				int code = key.codes[0];
				try {
					CharSequence label = labels[code][shift];
					if(label != null) key.label = label;
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
	}

	public void setShiftState(boolean shiftOn) {
		this.shiftPressed = shiftOn;
		currentMainKeyboard = mainKeyboards[shiftOn ? SHIFT_ON : SHIFT_OFF];
		setCurrentKeyboards();
		mainKeyboardView.setShifted(shiftOn);
		lowerKeyboardView.setShifted(shiftOn);
	}

	public void setCurrentKeyboards() {
		mainKeyboardView.setKeyboard(currentMainKeyboard);
		lowerKeyboardView.setKeyboard(currentLowerKeyboard);
	}

	public void setDefaultKeyboards() {
		currentMainKeyboard = mainKeyboards[SHIFT_OFF];
		currentLowerKeyboard = lowerKeyboards[0];

		setCurrentKeyboards();
	}

	public void createKeyboards(Context context, int main, int mainShift, int lower) {
		mainKeyboards = new Keyboard[2];
		lowerKeyboards = new Keyboard[1];
		mainKeyboards[SHIFT_OFF] = new Keyboard(context, main);
		mainKeyboards[SHIFT_ON] = new Keyboard(context, mainShift);
		lowerKeyboards[0] = new Keyboard(context, lower);
	}

	@Override
	public View getView() {
		return mainView;
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void close() {

	}

	@Override
	public void onPress(int primaryCode) {
		Vibrator vibrator = (Vibrator) parent.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(30);
		pressTime = System.currentTimeMillis();
	}

	@Override
	public void onRelease(int primaryCode) {
		if(primaryCode == KeyEvent.KEYCODE_DEL) return;
		Vibrator vibrator = (Vibrator) parent.getSystemService(Context.VIBRATOR_SERVICE);
		int duration = (int) (System.currentTimeMillis() - pressTime) / 15;
		if(duration > 20) duration = 20;
		if(duration >= 10) vibrator.vibrate(duration);
		ignore = false;
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		switch(primaryCode) {
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			if(shiftLocked) shiftLocked = false;
			setShiftState(!shiftPressed);
			parent.onKeyEvent(new KeyEvent(shiftPressed ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, primaryCode), false);
			return;
		}
		if(ignore) {
			return;
		}
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode);
		boolean ret = parent.onKeyEvent(event, false);
		parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, primaryCode), false);
		if(!shiftLocked && shiftPressed) {
			setShiftState(false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT), false);
		}
		if(!ret) parent.getCurrentInputConnection().sendKeyEvent(event);
	}

	@Override
	public void onLongPress(int primaryCode) {
		if(primaryCode == KeyEvent.KEYCODE_DEL) {
			return;
		}
		Vibrator vibrator = (Vibrator) parent.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(45);
		if(primaryCode == KeyEvent.KEYCODE_SHIFT_LEFT || primaryCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
			shiftLocked = true;
			setShiftState(true);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT), false);
			return;
		}
		if(!shiftLocked) {
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT), false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode), false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, primaryCode), false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT), false);
		} else {
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT), false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode), false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, primaryCode), false);
			parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT), false);
		}
		ignore = true;
	}

	@Override
	public void onText(CharSequence text) {

	}

	@Override
	public void swipeLeft() {

	}

	@Override
	public void swipeRight() {

	}

	@Override
	public void swipeDown() {

	}

	@Override
	public void swipeUp() {

	}

	public void setOnKeyboardActionListener(KeyboardView.OnKeyboardActionListener listener) {
		mainKeyboardView.setOnKeyboardActionListener(listener);
		lowerKeyboardView.setOnKeyboardActionListener(listener);
	}

	public void setOnAdvancedActionListener(DefaultSoftKeyboardView.OnAdvancedActionListener listener) {
		mainKeyboardView.setOnAdvancedActionListener(listener);
		lowerKeyboardView.setOnAdvancedActionListener(listener);
	}

	public CharSequence[][] getLabels() {
		return labels;
	}

	public void setLabels(CharSequence[][] labels) {
		this.labels = labels;
	}

}
