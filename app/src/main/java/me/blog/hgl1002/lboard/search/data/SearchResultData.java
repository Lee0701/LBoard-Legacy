package me.blog.hgl1002.lboard.search.data;

public interface SearchResultData {

	public static final int TYPE_URL_STRING = 1;
	public static final int TYPE_IMAGE = 2;
	public static final int TYPE_GIF = 3;

	public Object getData();
	public String getUrl();
	public String getDescription();

	public int getType();
}
