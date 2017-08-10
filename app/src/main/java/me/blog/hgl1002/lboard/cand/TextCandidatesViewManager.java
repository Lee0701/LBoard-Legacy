package me.blog.hgl1002.lboard.cand;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.R;

public class TextCandidatesViewManager implements CandidatesViewManager {

	CandidatesViewListener listener;

	ViewGroup entireView;
	LinearLayout layout;

	Context context;

	public TextCandidatesViewManager() {

	}

	@Override
	public View createView(Context context) {
		this.context = context;
		entireView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.text_candidates_view, null);
		layout = (LinearLayout) entireView.findViewById(R.id.candiadtes_list_view);
		return entireView;
	}

	@Override
	public View getView() {
		return entireView;
	}

	@Override
	public void setCandidates(Object[] candidates) {
		if(layout == null) return;
		layout.removeAllViews();
		if(candidates == null || candidates.length == 0) {
			TextView textView = new TextView(context);
			textView.setText("");
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
			textView.setPadding(20, 10, 20, 10);
			layout.addView(textView);
			return;
		}
		for(final Object o : candidates) {
			String str = o.toString();
			TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.text_candidate_item, null);
			textView.setText(str);
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(listener != null) listener.onSelect(o);
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
