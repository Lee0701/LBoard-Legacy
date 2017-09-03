package me.blog.hgl1002.lboard.search.engines;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WebSearchEngine extends SearchEngineImpl {

	protected String searchUrlPrefix, searchUrlSuffix;

	private String searchQuery;

	public WebSearchEngine(String searchUrlPrefix, String searchUrlSuffix) {
		this.searchUrlPrefix = searchUrlPrefix;
		this.searchUrlSuffix = searchUrlSuffix;
	}

	@Override
	public void search(Object query) {
		try {
			searchQuery = searchUrlPrefix + URLEncoder.encode((String) query, "UTF-8") + searchUrlSuffix;
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			searchQuery = searchUrlPrefix + (String) query + searchUrlSuffix;
		}
		new JsonSearchTask().execute();
	}

	private class JsonSearchTask extends AsyncTask<Void, Void, Void> {

		protected String result;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				result = sendQuery(searchQuery);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			if(listener != null) listener.onSearchComplete(searchQuery, result);
			super.onPostExecute(aVoid);
		}
	}

	private static String sendQuery(String query) throws IOException {
		String result = "";
		URL sURL = new URL(query);

		HttpURLConnection httpURLConnection = (HttpURLConnection) sURL.openConnection();

		if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 8192);

			String line = null;
			while((line = bufferedReader.readLine()) != null) {
				result += line;
			}
			bufferedReader.close();
		}

		return result;
	}

}
