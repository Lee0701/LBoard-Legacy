package me.blog.hgl1002.lboard.ime.charactergenerator;

public class BasicCodeSystem implements CodeSystem {

	public static final long H3 = 0x0003000000000000L;
	public static final long H2 = 0x0002000000000000L;

	public static final long MASK_CODE_TYPE = 0xffff000000000000L;

	public static final long MASK_CHO  = 0x0000ffff00000000L;
	public static final long MASK_JUNG = 0x00000000ffff0000L;
	public static final long MASK_JONG = 0x000000000000ffffL;

	public static char[] CHO_TABLE = {
			'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
			'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
			'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
	};
	public static char[] JUNG_TABLE = {
			'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ',
			'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
			'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ',
			'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ',
			'ㅣ', 'ㆍ', 'ㆎ', 'ᆢ',
	};
	public static char[] JONG_TABLE = {
			' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ',
			'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
			'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ',
			'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ',
			'ㅌ', 'ㅍ', 'ㅎ'
	};

	public static String convertToUnicode(long code) {
		if(isH3(code) || isH2(code)) {
			if(isCho(code)) {
				char converted = CHO_TABLE[getCho(code)-1];
				if(converted == 0) return new String(Character.toChars(getCho(code) + 0x10ff));
				return new String(Character.toChars(converted));
			} else if(isJung(code)) {
				char converted = JUNG_TABLE[getJung(code)-1];
				if(converted == 0) return new String(Character.toChars(getJung(code) + 0x1160));
				return new String(Character.toChars(converted));
			} else if(isJong(code)) {
				char converted = JONG_TABLE[getJong(code)];
				if(converted == 0) return new String(Character.toChars(getJong(code) + 0x11a7));
				return new String(Character.toChars(converted));
			} else if(hasCho(code) && hasJung(code)) {
				int cho = getCho(code) - 1;
				int jung = getJung(code) - 1;
				int jong = getJong(code);
				if(cho <= 0x12 && jung <= 0x14 && jong <= 0x1a) {
					return new String(Character.toChars(0xac00 + (cho * 588) + (jung * 28) + jong));
				}
			}
			int cho = getCho(code) + 0x10ff;
			int jung = getJung(code) + 0x1160;
			int jong = getJong(code) + 0x11a7;
			if(cho == 0x10ff) cho = 0x115f;
			String result = new String(Character.toChars(cho));
			result += new String(Character.toChars(jung));
			if(jong != 0x11a7) result += new String(Character.toChars(jong));
			return result;
		}
		return new String(Character.toChars((char) code));
	}

	public static boolean isH3(long code) {
		return (code & H3) != 0;
	}

	public static boolean isH2(long code) {
		return (code & H2) != 0;
	}

	public static boolean hasCho(long code) {
		return (code & MASK_CHO) != 0;
	}

	public static boolean hasJung(long code) {
		return (code & MASK_JUNG) != 0;
	}

	public static boolean hasJong(long code) {
		return (code & MASK_JONG) != 0;
	}

	public static boolean isCho(long code) {
		return hasCho(code) && !hasJung(code) && !hasJong(code);
	}

	public static boolean isJung(long code) {
		return !hasCho(code) && hasJung(code) && !hasJong(code);
	}

	public static boolean isJong(long code) {
		return !hasCho(code) && !hasJung(code) && hasJong(code);
	}

	public static int getCho(long code) {
		return (int) ((code & MASK_CHO) >> 0x20);
	}

	public static int getJung(long code) {
		return (int) ((code & MASK_JUNG) >> 0x10);
	}

	public static int getJong(long code) {
		return (int) ((code & MASK_JONG) >> 0x00);
	}

	public static long fromCho(int cho) {
		return (long) cho << 0x20 & MASK_CHO;
	}

	public static long fromJung(int jung) {
		return (long) jung << 0x10 & MASK_JUNG;
	}

	public static long fromJong(int jong) {
		return (long) jong << 0x00 & MASK_JONG;
	}

}
