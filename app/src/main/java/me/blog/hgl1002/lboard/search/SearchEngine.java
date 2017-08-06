package me.blog.hgl1002.lboard.search;

import java.util.List;

/**
 * SearchEngine
 *
 * 검색 엔진을 나타내는 인터페이스.
 *
 * @author Hyegyu Lee
 */
public interface SearchEngine {

	public void search(Object query);

	public void setListener(SearchEngineListener listener);

	public void removeListener();

	public static interface SearchEngineListener {
		public void onSearchComplete(String url, String result);
	}

}
