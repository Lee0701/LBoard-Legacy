package me.blog.hgl1002.lboard.search;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
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

import java.io.ByteArrayInputStream;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.R;
import me.blog.hgl1002.lboard.search.data.ImageData;
import me.blog.hgl1002.lboard.search.data.SearchResultData;
import me.blog.hgl1002.lboard.search.data.UrlStringData;

public class DefaultSearchViewManager implements SearchViewManager, SearchEngine.SearchEngineListener {

	LBoard parent;

	ViewGroup mainView;

	ViewGroup searchBox;
	TextView searchText;

	ViewGroup resultsView;
	DefaultWebSearchView webView;

	SearchEngine searchEngine;

	String url;

	WebView.HitTestResult hitTestResult;

	PopupMenu.OnMenuItemClickListener sendButtonListener = new PopupMenu.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			SearchResultData resultData;
			switch(item.getItemId()) {
			case R.id.send_link:
				resultData = new UrlStringData(url, webView.getTitle());
				parent.sendSearchResult(resultData);
				parent.hideSearchView();
				break;
			case R.id.send_capture:
				resultData = new ImageData(webView.screenshot(), url, webView.getTitle());
				parent.sendSearchResult(resultData);
				parent.hideSearchView();
				break;
			}
			return true;
		}
	};

	PopupMenu.OnMenuItemClickListener sendImageListener = new PopupMenu.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			SearchResultData resultData;
			if(hitTestResult == null) return true;
			switch(item.getItemId()) {
			case R.id.send_image:
				if(hitTestResult.getExtra().startsWith("data:")) {
					try {
						String datauri = hitTestResult.getExtra();
						byte[] decoded = Base64.decode(datauri.substring(datauri.indexOf("base64,") + 7), Base64.DEFAULT);
						Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
						resultData = new ImageData(bitmap, hitTestResult.getExtra(), webView.getTitle());
					} catch (IllegalArgumentException e) {
						resultData = new ImageData(null, hitTestResult.getExtra(), webView.getTitle());
					}
				} else {
					resultData = new ImageData(null, hitTestResult.getExtra(), webView.getTitle());
				}
				parent.sendSearchResult(resultData);
				break;
			case R.id.send_image_url:
				if(hitTestResult.getExtra().startsWith("data:")) {
					resultData = new UrlStringData(url, webView.getTitle());
				} else {
					resultData = new UrlStringData(hitTestResult.getExtra(), webView.getTitle());
				}
				parent.sendSearchResult(resultData);
				break;
			}
			return true;
		}
	};

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
		webView.setLongClickable(true);
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
				popup.setOnMenuItemClickListener(sendButtonListener);
				popup.show();
			}
		});

		webView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				switch (webView.getHitTestResult().getType()) {
				case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				case WebView.HitTestResult.IMAGE_TYPE:
					hitTestResult = webView.getHitTestResult();
					PopupMenu popup = new PopupMenu(parent, sendButton);
					popup.getMenuInflater().inflate(R.menu.send_image_popup, popup.getMenu());
					popup.setOnMenuItemClickListener(sendImageListener);
					popup.show();
					break;

				}
				return false;
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
