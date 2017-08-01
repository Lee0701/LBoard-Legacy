package me.blog.hgl1002.lboard.ime;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.inputmethod.EditorInfo;

/**
 * SoftKeyboard
 *
 * LBoard에서 사용하는 소프트 키보드를 정의하는 인터페이스.
 *
 * @author Hyegyu Lee
 */
public interface SoftKeyboard {

	/**
	 * 입력 뷰를 만든다.
	 * @return 만들어진 입력 뷰. 입력 뷰를 만들 수 없으면 {@code null}.
	 */
	public View createView(Context context);

	/**
	 * 현재 사용하고 있는 입력 뷰를 리턴한다.
	 * @return 사용중인 입력 뷰. 사용중인 입력 뷰가 없으면 {@code null}.
	 */
	public View getView();

	/**
	 * 사용자 설정을 적용한다.
	 * @param pref 설정.
	 * @param info 편집 화면에 대한 정보.
	 */
	public void setPreferences(SharedPreferences pref, EditorInfo info);

	/**
	 * 입력 뷰를 닫는다.
	 */
	public void close();

}
