package me.blog.hgl1002.lboard.cand;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.blog.hgl1002.lboard.LBoard;

public class TextCandidatesViewManager implements CandidatesViewManager {

	CandidatesViewListener listener;

	String[] candidates;
	LinearLayout layout;

	Context context;

	public TextCandidatesViewManager() {

	}

	@Override
	public View createView(Context context) {
		this.context = context;
		layout = new LinearLayout(context);
		return layout;
	}

	@Override
	public View getView() {
		return layout;
	}

	@Override
	public void setCandidates(Object[] candidates) {
		layout.removeAllViews();
		if(candidates == null) return;
		for(Object o : candidates) {
			String str = o.toString();
			TextView textView = new TextView(context);
			textView.setText(str);
			textView.setPadding(20, 10, 20, 10);
			textView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(listener != null) listener.onSelect(((TextView) v).getText());
					return false;
				}
			});
			layout.addView(textView);
		}
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void close() {

	}

	@Override
	public void setListener(CandidatesViewListener listener) {
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		this.listener = null;
	}
}
