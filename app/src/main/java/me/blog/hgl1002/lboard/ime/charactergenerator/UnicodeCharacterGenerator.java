package me.blog.hgl1002.lboard.ime.charactergenerator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.EmptyStackException;
import java.util.Stack;

public class UnicodeCharacterGenerator implements CharacterGenerator {

	public static final String COMBINATION_MAGIC_NUMBER = "LCOM1";

	// 유니코드 낱자를 표시하기 위한 테이블.
	public static int[] CHO_TABLE = {
			'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
			'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
			'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
	};
	public static int[] JUNG_TABLE = {
			'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ',
			'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
			'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ',
			'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ',
			'ㅣ', 'ㆍ', 'ㆎ', 'ᆢ',
	};
	public static int[] JONG_TABLE = {
			' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ',
			'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
			'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ',
			'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ',
			'ㅌ', 'ㅍ', 'ㅎ'
	};

	// 호환용 한글 자모를 표준 한글 자모로 변환하기 위한 테이블.
	public static int[] CHO_CONVERT = {
			0x1100, 0x1101, 0x0000, 0x1102, 0x0000, 0x0000, 0x1103,		// 0x3130
			0x1104, 0x1105, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3138
			0x111a, 0x1106, 0x1107, 0x1108, 0x0000, 0x1109, 0x110a, 0x110b,		// 0x3140 (0x111a: traditional)
			0x110c, 0x110d, 0x110e, 0x110f, 0x1110, 0x1111, 0x1112, 0x0000,		// 0x3148
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3150
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3158
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x1114, 0x1115, 0x0000,		// 0x3160
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x111c, 0x0000,		// 0x3168
			0x0000, 0x111d, 0x111e, 0x1120, 0x1122, 0x1123, 0x1127, 0x1129,		// 0x3170
			0x112b, 0x112c, 0x112d, 0x112e, 0x112f, 0x1132, 0x1136, 0x1140,		// 0x3178
			0x1147, 0x114c, 0x0000, 0x0000, 0x1157, 0x1158, 0x1159,				// 0x3180
	};

	public static int[] JONG_CONVERT = {
			0x11a8, 0x11a9, 0x11aa, 0x11ab, 0x11ac, 0x11ad, 0x11ae,		// 0x3130
			0x0000, 0x11af, 0x11b0, 0x11b1, 0x11b2, 0x11b3, 0x11b4, 0x11b5,		// 0x3138
			0x11b6, 0x11b7, 0x11b8, 0x0000, 0x11b9, 0x11ba, 0x11bb, 0x11bc,		// 0x3140
			0x11bd, 0x0000, 0x11be, 0x11bf, 0x11c0, 0x11c1, 0x11c2, 0x0000,		// 0x3148
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3150
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3158
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x11c6, 0x11c7,		// 0x3160
			0x11c8, 0x11cc, 0x11ce, 0x11d3, 0x11d7, 0x11d9, 0x11dc, 0x11dd,		// 0x3168
			0x11df, 0x11e2, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3170
			0x11e6, 0x0000, 0x11e7, 0x0000, 0x11e8, 0x11ea, 0x0000, 0x11eb,		// 0x3178
			0x0000, 0x11f0, 0x11f1, 0x11f2, 0x11f4, 0x0000, 0x11f9				// 0x3180
	};

	// 호환용 옛한글 자모를 표준 옛한글 자모로 변환하기 위한 테이블.
	public static int[] TRAD_JUNG_CONVERT = {
			0x1184, 0x1185, 0x1188, 0x1191, 0x1192, 0x1194, 0x119e, 0x11a1		// 0x3187
	};

	protected CharacterGeneratorListener listener;

	protected int cho, jung, jong;
	protected int last;
	protected int beforeJong;
	protected String composing;

	protected boolean moachigi;
	protected boolean firstMidEnd;

	protected int[][] combinationTable;

	/**
	 * History
	 * 한글 입력 기록을 저장하기 위한 객체의 클래스.
	 */
	private static class History {
		int cho, jung, jong;
		int last;
		int beforeJong;
		String composing;
		public History(int cho, int jung, int jong, int last, int beforeJong, String composing) {
			super();
			this.cho = cho;
			this.jung = jung;
			this.jong = jong;
			this.last = last;
			this.beforeJong = beforeJong;
			this.composing = composing;
		}
	}

	/**
	 * 한글 입력 기록을 저장하기 위한 스택. 백스페이스 처리에 이용된다.
	 */
	Stack<History> histories = new Stack<History>();

	public UnicodeCharacterGenerator() {
		resetComposing();
	}

	@Override
	public boolean onCode(long originalCode) {
		// 입력 기록을 업데이트한다.
		if(composing == "") histories.clear();
		else histories.push(new History(cho, jung, jong, last, beforeJong, composing));

		int code = (int) originalCode;
		boolean result;
		int combination;
		// 세벌식 한글 초성.
		if(isCho(code)) {
			int choCode = code - 0x1100;
				if (!moachigi && !isCho(last) && !isJung(last)) resetComposing();
				if (!moachigi && !isCho(last)) resetComposing();
			if(isCho(last)) {
				int source = this.cho + 0x1100;
				// 낱자 조합을 실햳한다.
				if((combination = getCombination(source, code)) != -1) {
					choCode = combination - 0x1100;
					this.cho = choCode;
				} else {
					resetComposing();
					this.cho = choCode;
				}
				// 낱자 조합 불가능 / 실패시
			} else {
				// 낱자 조합에 실패했을 경우 (이미 초성이 입력되어 있음) 조합을 종료한다.
				if(this.cho != -1) resetComposing();
				this.cho = choCode;
			}
			result = true;
			last = code;
			// 세벌식 한글 중성.
		} else if(code >= 0x1161 && code <= 0x11a7) {
			int jungCode = code - 0x1161;
			if(!moachigi && !isCho(last) && !isJung(last)) resetComposing();
			if(isJung(last)) {
				int source = this.jung + 0x1161;
				if((combination = getCombination(source, code)) != -1) {
					jungCode = combination - 0x1161;
					this.jung = jungCode;
				} else {
					resetComposing();
					this.jung = jungCode;
				}
			} else {
				if(this.jung != -1) resetComposing();
				this.jung = jungCode;
			}
			result = true;
			last = code;
			// 세벌식 한글 종성.
		} else if(code >= 0x11a8 && code <= 0x11ff) {
			int jongCode = code - 0x11a7;
			if(!moachigi && (jung == -1 || cho == -1)) resetComposing();
			if(isJong(last)) {
				int source = this.jong + 0x11a7;
				if((combination = getCombination(source, code)) != -1) {
					jongCode = combination - 0x11a7;
					this.jong = jongCode;
				} else {
					resetComposing();
					this.jong = jongCode;
				}
			} else {
				if(this.jong != -1) resetComposing();
				this.jong = jongCode;
			}
			result = true;
			last = code;
		}
		// 두벌식 한글 초/종성.
		else if(code >= 0x3131 && code <= 0x314e || code >= 0x3165 && code <= 0x3186) {
			// 초, 중성이 모두 있을 경우
			if(this.cho != -1 && this.jung != -1) {
				int jongCode = JONG_CONVERT[code - 0x3131] - 0x11a7;
				// 종성이 이미 존재할 경우.
				if(isJong(last) && this.jong != -1) {
					// 이미 있는 종성과 낱자 조합을 시도.
					if((combination = getCombination(this.jong+0x11a7, jongCode+0x11a7)) != -1) {
						this.beforeJong = this.jong;
						this.jong = combination - 0x11a7;
						last = jongCode + 0x11a7;
						// 낱자 조합 불가/실패시
					} else {
						this.beforeJong = 0;
						resetComposing();
						this.cho = CHO_CONVERT[code - 0x3131] - 0x1100;
						// 대응하는 초성이 존재하지 않을 경우 비운다.
						if(this.cho == -0x1100) this.cho = -1;
						last = CHO_CONVERT[code - 0x3131];
					}
					// 종성이 없을 경우
				} else {
					// 해당하는 종성이 없을 경우 (ㄸ, ㅉ, ㅃ 등)
					if(jongCode == -0x11a7) {
						// 조합을 종료하고 새로운 초성으로 조합을 시작한다.
						resetComposing();
						this.cho = CHO_CONVERT[code - 0x3131] - 0x1100;
						if(this.cho == -0x1100) this.cho = -1;
						last = CHO_CONVERT[code - 0x3131];
						// 해당하는 종성이 있을 경우, 종성을 조합한다.
					} else {
						this.beforeJong = 0;
						this.jong = jongCode;
						last = jongCode + 0x11a7;
					}
				}
				// 조/중성이 없을 경우
			} else {
				int choCode = CHO_CONVERT[code - 0x3131] - 0x1100;
				// 초성이 이미 존재할 경우
				if(isCho(last) && this.cho != -1) {
					// 이미 있는 초성과 낱자 결합을 시도한다.
					if((combination = getCombination(this.cho+0x1100, choCode+0x1100)) != -1) {
						this.cho = combination - 0x1100;
					} else {
						// 낱자 결합 불가 / 실패시 새로운 초성으로 조합 시작.
						resetComposing();
						this.cho = choCode;
						if(this.cho == -0x1100) this.cho = -1;
					}
					// 초성이 없을 경우, 새로운 초성으로 조합 시작.
				} else {
					if(!moachigi) resetComposing();
					this.cho = choCode;
				}
				last = choCode + 0x1100;
				// 해당하는 초성이 없을 경우 초성을 비운다.
				if(this.cho == -0x1100) this.cho = -1;
			}
			result = true;
			// 두벌식 중성.
		} else if(code >= 0x314f && code <= 0x3163 || code >= 0x3187 && code <= 0x318e) {
			// 조합 중인 종성이 없을 경우.
			if(this.jong == -1) {
				// 표준 한글 자모의 중성과 호환용 한글 자모의 중성은 배열 순서가 같음.
				int jungCode = code - 0x314f;
				// 옛한글 중성일 경우 따로 변환한다.
				if(code >= 0x3187 && code <= 0x318e) jungCode = TRAD_JUNG_CONVERT[code - 0x3187] - 0x1161;
				//조합 중인 중성이 존재할 경우.
				if(isJung(last) && this.jung != -1) {
					// 중성 낱자 결합을 시도한다.
					if((combination = getCombination(this.jung+0x1161, jungCode+0x1161)) != -1) {
						this.jung = combination - 0x1161;
					} else {
						// 조합 불가 / 실패시 새로운 중성으로 조합 시도.
						resetComposing();
						this.jung = jungCode;
					}
				} else {
					// 조합 중인 중성이 없을 경우 새로운 중성으로 조합을 시작한다.
					if(this.jung != -1) resetComposing();
					this.jung = jungCode;
				}
				last = jungCode + 0x1161;
				// 조합 중인 종성이 존재할 경우 (도깨비불 발생)
			} else {
				int jungCode = code - 0x314f;
				if(code >= 0x3187 && code <= 0x318e) jungCode = TRAD_JUNG_CONVERT[code - 0x3187] - 0x1161;
				if(this.jong != -1 && this.cho != -1) {
					// 종성이 두 개 이상 결합되었을 경우
					if(beforeJong != 0) {
						// 앞 종성을 앞 글자의 종성으로 한다.
						this.jong = beforeJong;
						this.composing = getVisible(this.cho, this.jung, this.jong);
						if(listener != null) listener.onCompose(composing);
						// 그리고 조합을 종료한 뒤,
						resetComposing();
						// 뒷 종성을 초성으로 변환하여 적용한다.
						this.cho = convertToCho(last) - 0x1100;
						composing = getVisible(this.cho, this.jung, this.jong);
						// 도깨비불이 일어났으므로 기록을 하나 더 남긴다.
						histories.push(new History(cho, jung, jong, last, beforeJong, composing));
						this.jung = jungCode;
						// 결합된 종성이 아니었을 경우
					} else {
						// 종성을 초성으로 변환해서 적용한다.
						int convertedCho;
						if((convertedCho = convertToCho(this.jong+0x11a7)) >-1) {
							this.jong = -1;
							this.composing = getVisible(this.cho, this.jung, this.jong);
							if(listener != null) listener.onCompose(composing);
							resetComposing();
							this.cho = convertedCho - 0x1100;
							composing = getVisible(this.cho, this.jung, this.jong);
							histories.push(new History(cho, jung, jong, last, beforeJong, composing));
							this.jung = jungCode;
						}
					}
					// 예외 상황에는 조합을 종료하고 새로운 중성으로 조합을 시작한다.
				} else {
					resetComposing();
					this.jung = jungCode;
				}
				last = jungCode + 0x1161;
			}
			result = true;
		}
		// 한글 낱자가 아닐 경우
		else {
			// 조합을 중단하고 처리 안함을 돌려준다.
			resetComposing();
			last = code;
			return false;
		}

		// 화면에 표시되는 문자를 계산해서 표시를 요청한다.
		this.composing = getVisible(this.cho, this.jung, this.jong);
		if(listener != null) listener.onCompose(composing);

		return result;
	}

	@Override
	public boolean backspace() {
		try {
			// 입력 기록을 하나 빼 온다.
			History history = histories.pop();
			// 현재 상태에 적용한다.
			this.cho = history.cho;
			this.jung = history.jung;
			this.jong = history.jong;
			this.last = history.last;
			this.beforeJong = history.beforeJong;
			this.composing = history.composing;

		} catch(EmptyStackException e) {
			// 스택이 비었을 경우 (입력된 낱자가 없을 경우)
			if(composing == "") {
				// 일반적인 백스페이스로 동작.
				return false;
			}
			// 마지막 하나가 남았을 걥우 비운다.
			else resetComposing();
			return false;
		}
		// 백스페이스를 실행한 결과를 표시한다.
		if(listener != null) listener.onCompose(composing);
		return true;
	}

	@Override
	public void resetComposing() {
		if(listener != null) listener.onCommit();
		cho = jung = jong = -1;
		composing = "";
		histories.clear();
	}

	int getCombination(int a, int b) {
		if(combinationTable == null) return -1;
		for(int[] item : combinationTable) {
			if(item[0] == a && item[1] == b) return item[2];
		}
		return -1;
	}

	/**
	 * 종성 코드를 초성 코드로 변환한다.
	 */
	int convertToCho(int jong) {
		for(int i = 0 ; i < JONG_CONVERT.length ; i++) {
			if(JONG_CONVERT[i] == jong) return CHO_CONVERT[i];
		}
		return -1;
	}

	/**
	 * 초, 중, 종성 코드를 한글 음절 코드로 합친다.
	 */
	int combineHangul(int cho, int jung, int jong) {
		return 0xac00 + (cho * 588) + (jung * 28) + jong;
	}

	/**
	 * 현재 화면에 표시되는 문자를 계산한다.
	 */
	String getVisible(int cho, int jung, int jong) {
		String visible;
		// 옛한글 성분이 포함된 경우 첫가끝으로 조합한다.
		if(cho > 0x12 || jung > 0x14 || jong > 0x1b) {
			if(cho != -1 && jung == -1 && jong == -1) {
				visible = new String(new char[] {(char) (cho + 0x1100)});
			} else if(cho == -1 && jung != -1 && jong == -1) {
				visible = new String(new char[] {(char) (jung + 0x1161)});
			} else if(cho == -1 && jung == -1 && jong != -1) {
				visible = new String(new char[] {(char) (jong + 0x11a8 - 1)});
			} else {
				if(cho == -1) cho = 0x5f;
				visible = new String(new char[] {(char) (cho + 0x1100)})
						+ new String(new char[] {(char) (jung + 0x1161)});
				if(jong != -1) visible += new String(new char[] {(char) (jong + 0x11a8 - 1)});
			}
			// 초성 + 중성 + 종성
		} else if(cho != -1 && jung != -1 && jong != -1) {
			visible = String.valueOf((char) combineHangul(cho, jung, jong));
			// 초성 + 중성
		} else if(cho != -1 && jung != -1) {
			visible = String.valueOf((char) combineHangul(cho, jung, 0));
			// 첫가끝 조합이 가능한 경우
		} else if(firstMidEnd) {
			// 초성
			if(cho != -1 && jung == -1 && jong == -1) {
				visible = String.valueOf((char) CHO_TABLE[cho]);
				// 중성
			} else if(cho == -1 && jung != -1 && jong == -1) {
				visible = String.valueOf((char) JUNG_TABLE[jung]);
				// 종성
			} else if(cho == -1 && jung == -1 && jong != -1) {
				visible = String.valueOf((char) JONG_TABLE[jong]);
				// 나머지 경우에는 첫가끝 조합.
			} else {
				if (cho == -1) cho = 0x5f;
				visible = new String(new char[]{(char) (cho + 0x1100)})
						+ new String(new char[]{(char) (jung + 0x1161)});
				if (jong != -1) visible += new String(new char[]{(char) (jong + 0x11a8 - 1)});
			}
			// 첫가끝 조합을 사용하지 않도록 한 경우
		} else {
			// 초성이 있으면 초성 표시
			if (cho != -1) {
				visible = String.valueOf((char) CHO_TABLE[cho]);
				// 중성이 있으면 중성 표시
			} else if (jung != -1) {
				visible = String.valueOf((char) JUNG_TABLE[jung]);
				// 종성이 있으면 종성 표시
			} else if (jong != -1) {
				visible = String.valueOf((char) JONG_TABLE[jong]);
				// 예외 경우에는 아무것도 표시 안함.
			} else {
				visible = "";
			}
		}
		return visible;
	}

	/**
	 * 유니코드 낱자가 한글 세벌식 초성인지 확인한다.
	 */
	public boolean isCho(int code) {
		return code >= 0x1100 && code <= 0x115f;
	}

	/**
	 * 유니코드 낱자가 한글 세벌식 중성인지 확인한다.
	 */
	public boolean isJung(int code) {
		return code >= 0x1161 && code <= 0x11a7;
	}

	/**
	 * 유니코드 낱자가 한글 세벌식 종성인지 확인한다.
	 */
	public boolean isJong(int code) {
		return code >= 0x11a8 && code <= 0x11ff;
	}

	@Override
	public void setListener(CharacterGeneratorListener listener) {
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		this.listener = null;
	}

	public static int[][] loadCombinationTable(InputStream inputStream) {
		try {
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			for(int i = 0 ; i < COMBINATION_MAGIC_NUMBER.length() ; i++) {
				char c = (char) buffer.get();
				if(c != COMBINATION_MAGIC_NUMBER.charAt(i)) {
					throw new RuntimeException("Layout file must start with String \"" + COMBINATION_MAGIC_NUMBER + "\"!");
				}
			}
			for(int i = 0 ; i < 0x10 - COMBINATION_MAGIC_NUMBER.length() ; i++) {
				buffer.get();
			}
			int[][] combinations = new int[buffer.remaining() / 4 / 2][3];
			for(int i = 0 ; i < combinations.length ; i++) {
				if(buffer.remaining() < 0x08) break;
				buffer.getChar();
				int a = buffer.getChar();
				int b = buffer.getChar();
				int result = buffer.getChar();
				combinations[i][0] = a;
				combinations[i][1] = b;
				combinations[i][2] = result;
			}
			return combinations;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getComposing() {
		return composing;
	}

	public void setComposing(String composing) {
		this.composing = composing;
	}

	public boolean isMoachigi() {
		return moachigi;
	}

	public void setMoachigi(boolean moachigi) {
		this.moachigi = moachigi;
	}

	public boolean isFirstMidEnd() {
		return firstMidEnd;
	}

	public void setFirstMidEnd(boolean firstMidEnd) {
		this.firstMidEnd = firstMidEnd;
	}

	public int[][] getCombinationTable() {
		return combinationTable;
	}

	public void setCombinationTable(int[][] combinationTable) {
		this.combinationTable = combinationTable;
	}
}
