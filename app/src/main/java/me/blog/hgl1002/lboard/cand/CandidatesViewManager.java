package me.blog.hgl1002.lboard.cand;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public interface CandidatesViewManager {

	public View createView(Context context);

	public View getView();

	public void setCandidates(Object[] candidates);

	public void setPreferences(SharedPreferences pref, EditorInfo info);

	public void close();

	public void setListener(CandidatesViewListener listener);
	public void removeListener();

	public static interface CandidatesViewListener {
		public void onSelect(Object candidate);
	}

}
