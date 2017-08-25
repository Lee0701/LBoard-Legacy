package me.blog.hgl1002.lboard.ime;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.expression.StringRecursionTreeBuilder;
import me.blog.hgl1002.lboard.expression.TreeBuilder;
import me.blog.hgl1002.lboard.expression.TreeParser;
import me.blog.hgl1002.lboard.ime.charactergenerator.BasicCharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.UnicodeCharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.basic.AutomataRule;
import me.blog.hgl1002.lboard.ime.charactergenerator.basic.AutomataTable;
import me.blog.hgl1002.lboard.ime.hardkeyboard.BasicHardKeyboard;
import me.blog.hgl1002.lboard.ime.hardkeyboard.DefaultHardKeyboard;
import me.blog.hgl1002.lboard.ime.hardkeyboard.lhkb.LHKB1;
import me.blog.hgl1002.lboard.ime.hardkeyboard.lhkb.LHKB2;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

public class InternalInputMethodLoader implements InputMethodLoader {

	public static final String MAGIC_NUMBER = "LIME";

	public static final String FILENAME_DEFAULT_SOFT_DEF = "soft_default.properties";
	public static final String XML = "xml";
	public static final String KEY_DEFAULT_SOFT_MAIN = "layout.main";
	public static final String KEY_DEFAULT_SOFT_MAIN_SHIFT = "layout.main.shift";
	public static final String KEY_DEFAULT_SOFT_LOWER = "layout.lower";

	public static final String FILENAME_DEFAULT_HARD = "hard_default.lhkb";
	public static final String FILENAME_BASIC_HARD = "hard_basic.lhkb";

	public static final byte SOFT_BLANK = 0;
	public static final byte SOFT_DEFAULT = 1;
	public static final byte SOFT_BASIC = 2;
	public static final byte SOFT_ADVANCED = 3;

	public static final byte HARD_BLANK = 0;
	public static final byte HARD_DEFAULT = 1;
	public static final byte HARD_BASIC = 2;
	public static final byte HARD_ADVANCED = 3;

	public static final byte CG_BLANK = 0;
	public static final byte CG_UNICODE = 1;
	public static final byte CG_BASIC = 2;

	LBoard parent;

	public InternalInputMethodLoader(LBoard parent) {
		this.parent = parent;
	}

	@Override
	public LBoardInputMethod load(Object o) {
		String name = "";
		SoftKeyboard softKeyboard = null;
		HardKeyboard hardKeyboard = null;
		CharacterGenerator characterGenerator = null;
		TreeParser parser = new TreeParser();
		LBoardInputMethod result = null;
		if(o instanceof File) {
			try {
				File file = (File) o;
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				for(int i = 0 ; i < MAGIC_NUMBER.length() ; i++) {
					char c = (char) dis.readByte();
					if(MAGIC_NUMBER.charAt(i) != c) {
						throw new RuntimeException("Input Method file must start with magic number '" + MAGIC_NUMBER + "'.");
					}
				}
				byte b;
				while((b = dis.readByte()) != 0) {
					name += new String(Character.toChars(b));
				}
				// Soft keyboard.
				b = dis.readByte();
				switch(b) {
				case SOFT_DEFAULT: {
					DefaultSoftKeyboard defaultSoftKeyboard = new DefaultSoftKeyboard(parent);
					Properties properties = new Properties();
					properties.load(new FileInputStream(new File(file.getParentFile().getAbsolutePath(), FILENAME_DEFAULT_SOFT_DEF)));
					String packageName = parent.getPackageName();
					int main = parent.getResources().getIdentifier(properties.getProperty(KEY_DEFAULT_SOFT_MAIN), XML, packageName);
					int mainShift = parent.getResources().getIdentifier(properties.getProperty(KEY_DEFAULT_SOFT_MAIN_SHIFT), XML, packageName);
					int lower = parent.getResources().getIdentifier(properties.getProperty(KEY_DEFAULT_SOFT_LOWER), XML, packageName);
					defaultSoftKeyboard.createKeyboards(parent, main, mainShift, lower);
					softKeyboard = defaultSoftKeyboard;
					break;
				}
				}
				// Hard keyboard.
				boolean generator = false;
				b = dis.readByte();
				switch(b) {
				case HARD_DEFAULT: {
					DefaultHardKeyboard defaultHardKeyboard = new DefaultHardKeyboard(parent);
					defaultHardKeyboard.setMappings(LHKB1.loadMappings(new FileInputStream(new File(file.getParentFile(), FILENAME_DEFAULT_HARD))));
					hardKeyboard = defaultHardKeyboard;
					generator = true;
					break;
				}
				case HARD_BASIC: {
					BasicHardKeyboard basicHardKeyboard = new BasicHardKeyboard(parent);
					basicHardKeyboard.setParser(parser);
					basicHardKeyboard.setMappings(LHKB2.loadMappings(new FileInputStream(new File(file.getParentFile(), FILENAME_BASIC_HARD))));
					hardKeyboard = basicHardKeyboard;
					generator = true;
					break;
				}
				}
				// Character generator.
				if(generator) {
					b = dis.readByte();
					switch (b) {
					case CG_UNICODE: {
						UnicodeCharacterGenerator unicodeCharacterGenerator = new UnicodeCharacterGenerator();
						characterGenerator = unicodeCharacterGenerator;
						break;
					}
					case CG_BASIC: {
						BasicCharacterGenerator basicCharacterGenerator = new BasicCharacterGenerator(parser);
						// Temporary code for testing.
						TreeBuilder builder = new StringRecursionTreeBuilder();
						builder.setConstants(new HashMap<String, Long>());
						AutomataTable table = new AutomataTable(new AutomataRule(0, builder.build("A ? 1 : B ? 2 : C ? 3 : 0"), 0, ""));
						table.set(1, new AutomataRule(0, builder.build("A ? 1 : B ? 2 : C ? 3 : 0"), 0, ""));
						table.set(2, new AutomataRule(0, builder.build("B ? 2 : C ? 3 : 0"), 0, ""));
						table.set(3, new AutomataRule(0, builder.build("C ? 3 : 0"), 0, ""));
						basicCharacterGenerator.setAutomataTable(table);
						characterGenerator = basicCharacterGenerator;
						break;
					}
					}
					if(hardKeyboard instanceof DefaultHardKeyboard) {
						((DefaultHardKeyboard) hardKeyboard).setCharacterGenerator(characterGenerator);
					}
					if(hardKeyboard instanceof BasicHardKeyboard) {
						((BasicHardKeyboard) hardKeyboard).setCharacterGenerator(characterGenerator);
					}
				}
				System.out.println(characterGenerator);
				result = new LBoardInputMethod(name, softKeyboard, hardKeyboard, characterGenerator);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return result;
	}
}