package me.blog.hgl1002.lboard.ime.hardkeyboard.lhkb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class LHKB1 {

	public static final String LAYOUT_MAGIC_NUMBER = "LHKB1";
	public static final int MAPPINGS_SIZE = 0x100;

	public static long[][] loadMappings(InputStream inputStream) {
		try {
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			for (int i = 0 ; i < LAYOUT_MAGIC_NUMBER.length() ; i++) {
				char c = (char) buffer.get();
				if (c != LAYOUT_MAGIC_NUMBER.charAt(i)) {
					throw new RuntimeException("LHKB1 layout file must start with String \"" + LAYOUT_MAGIC_NUMBER + "\"!");
				}
			}
			for (int i = 0 ; i < 0x10 - LAYOUT_MAGIC_NUMBER.length() ; i++) {
				buffer.get();
			}
			long[][] layout = new long[MAPPINGS_SIZE][2];
			for (int i = 0 ; i < layout.length ; i++) {
				if (buffer.remaining() < 0x08) break;
				long normal = buffer.getLong();
				long shift = buffer.getLong();
				layout[i][0] = normal;
				layout[i][1] = shift;
			}
			return layout;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
