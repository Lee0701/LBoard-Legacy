package me.blog.hgl1002.lboard.ime.softkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import me.blog.hgl1002.lboard.ime.SoftKeyboard;

public class BasicSoftKeyboard implements SoftKeyboard {
	@Override
	public View createView(Context context) {
		return null;
	}

	@Override
	public View getView() {
		return null;
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void close() {

	}
}
