package me.blog.hgl1002.lboard.ime.charactergenerator;

public class BasicCodeSystem implements CodeSystem {

	public static final long H3 = 0x0003000000000000L;
	public static final long H2 = 0x0002000000000000L;

	public static final long MASK_CHO = 0x0000ffff00000000L;
	public static final long MASK_JUNG = 0x00000000ffff0000L;
	public static final long MASK_JONG = 0x000000000000ffffL;


	public static final boolean isH3(long code) {
		return (code & H3) != 0;
	}

	public static final boolean isH2(long code) {
		return (code & H2) != 0;
	}

	public static final boolean hasCho(long code) {
		return (code & MASK_CHO) != 0;
	}

	public static final boolean hasJung(long code) {
		return (code & MASK_JUNG) != 0;
	}

	public static final boolean hasJong(long code) {
		return (code & MASK_JONG) != 0;
	}

}
