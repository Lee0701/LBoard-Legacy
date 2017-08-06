package me.blog.hgl1002.lboard.search;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.R;

public class DefaultSearchViewManager implements SearchViewManager, SearchEngine.SearchEngineListener {

	LBoard parent;

	ViewGroup mainView;

	ViewGroup searchBox;
	TextView searchText;

	ViewGroup resultsView;
	DefaultWebSearchView webView;

	SearchEngine searchEngine;

	String url;

	public DefaultSearchViewManager(LBoard parent, SearchEngine searchEngine) {
		this.parent = parent;
		this.searchEngine = searchEngine;
		searchEngine.setListener(this);
	}

	@Override
	public View createView(Context context) {
		LinearLayout mainView = new LinearLayout(context);
		mainView.setOrientation(LinearLayout.VERTICAL);

		searchBox = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.search_box, mainView);
		searchText = (TextView) searchBox.findViewById(R.id.search_text);

		resultsView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.search_result, mainView);
		webView = (DefaultWebSearchView) resultsView.findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setDefaultTextEncodingName("utf-8");
		webView.setHorizontalScrollBarEnabled(false);
		webView.setVerticalScrollBarEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@TargetApi(21)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				DefaultSearchViewManager.this.url = request.getUrl().toString();
				reset();
				view.loadUrl(url);
				return false;
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				DefaultSearchViewManager.this.url = url;
				reset();
				view.loadUrl(url);
				return false;
			}
		});

		final Button sendButton = (Button) resultsView.findViewById(R.id.send_result);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(parent, sendButton);
				popup.getMenuInflater().inflate(R.menu.send_button_popup, popup.getMenu());

				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch(item.getItemId()) {
						case R.id.send_link:
							parent.sendSearchResult(url);
							parent.hideSearchView();
							break;
						case R.id.send_capture:
							parent.sendSearchResult(webView.screenshot());
							parent.hideSearchView();
							break;
						}
						return true;
					}
				});
				popup.show();
			}
		});

		this.mainView = mainView;
		return mainView;
	}

	@Override
	public View getView() {
		return mainView;
	}

	@Override
	public void setText(CharSequence text) {
		searchText.setText(text);
	}

	@Override
	public void search() {
		searchEngine.search(searchText.getText());
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void reset() {
		if(Build.VERSION.SDK_INT < 18) {
			webView.clearView();
		} else {
			webView.loadUrl("about:blank");
		}
	}

	@Override
	public void onSearchComplete(String url, String result) {
		this.url = url;
		webView.loadDataWithBaseURL(url, result, "text/html; charset=UTF-8", "UTF-8", null);
	}

	@Override
	public void setPreferences(SharedPreferences pref) {

	}

	@Override
	public void closing() {

	}

}
