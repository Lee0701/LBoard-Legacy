package me.blog.hgl1002.lboard.ime;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

/**
 * HardKeyboard
 *
 * LBoard에서 사용하는 하드웨어 키보드.
 * KeyEvent를 받아서 처리한다.
 *
 * @author Hyegyu Lee
 */
public interface HardKeyboard {

	/**
	 * 키 이벤트를 처리한다.
	 * @param event	처리할 키 이벤트.
	 * @param info		키 이벤트의 정보.
	 * @return			키 이벤트를 처리했으면 {@code true}, 처리하지 않고 기본 처리를 요청하려면 {@code false}.
	 */
	public boolean onKeyEvent(KeyEvent event, KeyEventInfo info);

	/**
	 * 사용자 설정을 키 반영한다.
	 * @param pref		사용자 설정.
	 * @param info		입력 화면의 정보.
	 */
	public void setPreferences(SharedPreferences pref, EditorInfo info);

	/**
	 * 입력 뷰가 닫힐 때 호출된다.
	 */
	public void closing();

}
