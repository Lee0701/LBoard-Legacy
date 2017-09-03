package me.blog.hgl1002.lboard.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.webkit.WebView;

public class DefaultWebSearchView extends WebView {

	public DefaultWebSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Bitmap screenshot() {
//		this.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
//				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//		this.layout(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
		this.setDrawingCacheEnabled(true);
		this.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		int iWidth = bitmap.getWidth();
		int iHeight = bitmap.getHeight();
		canvas.translate(-getScrollX(), -getScrollY());
		canvas.drawBitmap(bitmap, iWidth, iHeight, paint);
		this.draw(canvas);
		return bitmap;
	}

}
