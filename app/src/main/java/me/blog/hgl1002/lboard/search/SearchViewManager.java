package me.blog.hgl1002.lboard.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

/**
 * SearchViewManager
 *
 * 검색 뷰를 관리하는 매니저.
 *
 * @author Hyegyu Lee
 */
public interface SearchViewManager {

	public View createView(Context context);

	public View getView();

	public void setText(CharSequence text);

	public void search();

	public void reset();

	public String getUrl();

	public void setPreferences(SharedPreferences pref);

	public void closing();
}
