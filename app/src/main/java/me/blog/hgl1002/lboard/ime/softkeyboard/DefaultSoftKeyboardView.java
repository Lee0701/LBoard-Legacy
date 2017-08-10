package me.blog.hgl1002.lboard.ime.softkeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.KeyEvent;


public class DefaultSoftKeyboardView extends KeyboardView {

	OnAdvancedActionListener onAdvancedActionListener;

	public DefaultSoftKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean onLongPress(Keyboard.Key popupKey) {
		if(!popupKey.repeatable && onAdvancedActionListener != null)
			onAdvancedActionListener.onLongPress(popupKey.codes[0]);
		return super.onLongPress(popupKey);
	}

	public void setOnAdvancedActionListener(OnAdvancedActionListener onAdvancedActionListener) {
		this.onAdvancedActionListener = onAdvancedActionListener;
	}

	public void deleteOnAdvancedActionListener() {
		this.onAdvancedActionListener = null;
	}

	public interface OnAdvancedActionListener {
		void onLongPress(int primaryCode);
	}

}
