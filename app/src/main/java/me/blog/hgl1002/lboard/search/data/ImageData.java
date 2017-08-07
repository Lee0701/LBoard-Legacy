package me.blog.hgl1002.lboard.search.data;

import android.graphics.Bitmap;

public class ImageData implements SearchResultData {

	Bitmap bitmap;
	String url;
	String description;

	public ImageData(Bitmap bitmap, String url, String description) {
		this.bitmap = bitmap;
		this.url = url;
		this.description = description;
	}

	@Override
	public Object getData() {
		return bitmap;
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
		return TYPE_IMAGE;
	}
}
