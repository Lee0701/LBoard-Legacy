package me.blog.hgl1002.lboard.ime.charactergenerator;

import java.util.HashMap;
import java.util.Map;

public class BasicCodeSystem implements CodeSystem {

	public static final long H3 = 0x0003000000000000L;
	public static final long H2 = 0x0002000000000000L;

	public static final long MASK_CODE_TYPE = 0xffff000000000000L;

	public static final long MASK_CHO  = 0x0000ffff00000000L;
	public static final long MASK_JUNG = 0x00000000ffff0000L;
	public static final long MASK_JONG = 0x000000000000ffffL;

	public static final long G_ = 0x0000000100000000L;
	public static final long GG_= 0x0000000200000000L;
	public static final long N_ = 0x0000000300000000L;
	public static final long D_ = 0x0000000400000000L;
	public static final long DD_= 0x0000000500000000L;
	public static final long R_ = 0x0000000600000000L;
	public static final long M_ = 0x0000000700000000L;
	public static final long B_ = 0x0000000800000000L;
	public static final long BB_= 0x0000000900000000L;
	public static final long S_ = 0x0000000a00000000L;
	public static final long SS_= 0x0000000b00000000L;
	public static final long Q_ = 0x0000000c00000000L;
	public static final long J_ = 0x0000000d00000000L;
	public static final long JJ_= 0x0000000e00000000L;
	public static final long C_ = 0x0000000f00000000L;
	public static final long K_ = 0x0000001000000000L;
	public static final long T_ = 0x0000001100000000L;
	public static final long P_ = 0x0000001200000000L;
	public static final long H_ = 0x0000001300000000L;

	public static final long A_ = 0x0000000000010000L;
	public static final long AE = 0x0000000000020000L;
	public static final long YA = 0x0000000000030000L;
	public static final long YAE= 0x0000000000040000L;
	public static final long EO = 0x0000000000050000L;
	public static final long E_ = 0x0000000000060000L;
	public static final long YEO= 0x0000000000070000L;
	public static final long YE = 0x0000000000080000L;
	public static final long O_ = 0x0000000000090000L;
	public static final long WA = 0x00000000000a0000L;
	public static final long WAE= 0x00000000000b0000L;
	public static final long OI = 0x00000000000c0000L;
	public static final long YO = 0x00000000000d0000L;
	public static final long U_ = 0x00000000000e0000L;
	public static final long UEO= 0x00000000000f0000L;
	public static final long UE = 0x0000000000100000L;
	public static final long WI = 0x0000000000110000L;
	public static final long YU = 0x0000000000120000L;
	public static final long EU = 0x0000000000130000L;
	public static final long EUI= 0x0000000000140000L;
	public static final long I_ = 0x0000000000150000L;

	public static final long _G = 0x0000000000000001L;
	public static final long _GG =0x0000000000000002L;
	public static final long _GS =0x0000000000000003L;
	public static final long _N = 0x0000000000000004L;
	public static final long _NJ =0x0000000000000005L;
	public static final long _NH =0x0000000000000006L;
	public static final long _D = 0x0000000000000007L;
	public static final long _R = 0x0000000000000008L;
	public static final long _RG =0x0000000000000009L;
	public static final long _RM =0x000000000000000aL;
	public static final long _RB =0x000000000000000bL;
	public static final long _RS =0x000000000000000cL;
	public static final long _RT =0x000000000000000dL;
	public static final long _RP =0x000000000000000eL;
	public static final long _RH =0x000000000000000fL;
	public static final long _M = 0x0000000000000010L;
	public static final long _B = 0x0000000000000011L;
	public static final long _BS =0x0000000000000012L;
	public static final long _S = 0x0000000000000013L;
	public static final long _SS =0x0000000000000014L;
	public static final long _Q = 0x0000000000000015L;
	public static final long _J = 0x0000000000000016L;
	public static final long _C = 0x0000000000000017L;
	public static final long _K = 0x0000000000000018L;
	public static final long _T = 0x0000000000000019L;
	public static final long _P = 0x000000000000001aL;
	public static final long _H = 0x000000000000001bL;

	public static final Map<String, Long> CONSTANTS = new HashMap<String, Long>() {{
		put("H3", H3);
		put("H2", H2);

		put("G_", G_);
		put("GG_", GG_);
		put("N_", N_);
		put("D_", D_);
		put("DD_", DD_);
		put("R_", R_);
		put("M_", M_);
		put("B_", B_);
		put("BB_", BB_);
		put("S_", S_);
		put("SS_", SS_);
		put("Q_", Q_);
		put("J_", J_);
		put("JJ_", JJ_);
		put("C_", C_);
		put("K_", K_);
		put("T_", T_);
		put("P_", P_);
		put("H_", H_);

		put("A_", A_);
		put("AE", AE);
		put("YA", YA);
		put("YAE", YAE);
		put("EO", EO);
		put("E_", E_);
		put("YEO", YEO);
		put("YE", YE);
		put("O_", O_);
		put("WA", WA);
		put("WAE", WAE);
		put("OI", OI);
		put("YO", YO);
		put("U_", U_);
		put("UEO", UEO);
		put("UE", UE);
		put("WI", WI);
		put("YU", YU);
		put("EU", EU);
		put("EUI", EUI);
		put("I_", I_);

		put("_G", _G);
		put("_GG", _GG);
		put("_GS", _GS);
		put("_N", _N);
		put("_NJ", _NJ);
		put("_NH", _NH);
		put("_D", _D);
		put("_R", _R);
		put("_RG", _RG);
		put("_RM", _RM);
		put("_RB", _RB);
		put("_RS", _RS);
		put("_RT", _RT);
		put("_RP", _RP);
		put("_RH", _RH);
		put("_M", _M);
		put("_B", _B);
		put("_BS", _BS);
		put("_S", _S);
		put("_SS", _SS);
		put("_Q", _Q);
		put("_J", _J);
		put("_C", _C);
		put("_K", _K);
		put("_T", _T);
		put("_P", _P);
		put("_H", _H);
	}};

	public static final Map<Long, String> R_CONSTANTS = new HashMap<Long, String>() {{
		for(String key : CONSTANTS.keySet()) {
			put(CONSTANTS.get(key), key);
		}
	}};

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
		return (code & MASK_CODE_TYPE) == H3;
	}

	public static boolean isH2(long code) {
		return (code & MASK_CODE_TYPE) == H2;
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
