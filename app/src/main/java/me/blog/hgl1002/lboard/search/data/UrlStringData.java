package me.blog.hgl1002.lboard.search.data;

public class UrlStringData implements SearchResultData {

	String url;
	String description;

	public UrlStringData(String url, String description) {
		this.url = url;
		this.description = description;
	}

	@Override
	public Object getData() {
		return null;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getType() {
		return TYPE_URL_STRING;
	}
}
