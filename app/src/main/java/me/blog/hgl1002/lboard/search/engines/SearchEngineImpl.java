package me.blog.hgl1002.lboard.search.engines;

import me.blog.hgl1002.lboard.search.SearchEngine;

public abstract class SearchEngineImpl implements SearchEngine {

	SearchEngineListener listener;

	@Override
	public void setListener(SearchEngineListener listener) {
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		this.listener = null;
	}
}
