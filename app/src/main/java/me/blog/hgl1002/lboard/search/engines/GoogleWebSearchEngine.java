package me.blog.hgl1002.lboard.search.engines;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GoogleWebSearchEngine extends SearchEngineImpl {

	public static final String SEARCH_URL = "https://www.google.co.kr/search?ie=utf8&oe=utf8&q=";

	private String searchQuery;

	@Override
	public void search(Object query) {
		try {
			searchQuery = SEARCH_URL + URLEncoder.encode((String) query, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			searchQuery = SEARCH_URL + (String) query;
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
