package me.blog.hgl1002.lboard.cand;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.HorizontalScrollView;
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
		HorizontalScrollView scrollView = new HorizontalScrollView(context);
		layout = new LinearLayout(context);
		layout.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
		scrollView.addView(layout);
		return scrollView;
	}

	@Override
	public View getView() {
		return layout;
	}

	@Override
	public void setCandidates(Object[] candidates) {
		layout.removeAllViews();
		if(candidates == null || candidates.length == 0) {
			TextView textView = new TextView(context);
			textView.setText("null");
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
			textView.setPadding(20, 10, 20, 10);
			layout.addView(textView);
			return;
		}
		for(Object o : candidates) {
			String str = o.toString();
			TextView textView = new TextView(context);
			textView.setText(str);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
			textView.setPadding(20, 10, 20, 10);
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(listener != null) listener.onSelect(((TextView) v).getText());
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
