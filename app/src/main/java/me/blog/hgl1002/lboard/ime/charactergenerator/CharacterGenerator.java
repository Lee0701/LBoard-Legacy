package me.blog.hgl1002.lboard.ime.charactergenerator;

import java.util.Map;

import me.blog.hgl1002.lboard.engine.StringSegment;
import me.blog.hgl1002.lboard.event.LBoardEventListener;

/**
 * CharacterGenerator.
 *
 * LBoard의 HardKeyboard에서 사용하는 문자 생성기.
 * 한글 코드를 받아 처리한다.
 *
 * @author Hyegyu Lee
 */
public interface CharacterGenerator {

	/**
	 * 한글 코드를 입력받아 처리한다.
	 * @param code		처리할 한글 코드.
	 * @return			처리가 되었으면 {@code true}, 기본 처리를 요청하려면 {@code false}.
	 */
	boolean onCode(long code);

	/**
	 * 백스페이스를 처리한다.
	 * @return			기본 처리를 요청하려면 {@code false}.
	 */
	boolean backspace();

	/**
	 * 진행 중인 한글 조합을 종료한다.
	 */
	void resetComposing();

	/**
	 * 상태 변수를 처리해서 돌려준다.
	 */
	Map<String, Long> getVariables();

	void setListener(LBoardEventListener listener);
	void removeListener();

}
