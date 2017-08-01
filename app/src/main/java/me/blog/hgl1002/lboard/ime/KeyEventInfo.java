package me.blog.hgl1002.lboard.ime;

public class KeyEventInfo {

	public static final int KEYTYPE_HARDKEY = 0;
	public static final int KEYTYPE_SOFTKEY = 1;

	protected int keyType;

	public int getKeyType() {
		return keyType;
	}

	public void setKeyType(int keyType) {
		this.keyType = keyType;
	}

	public static class Builder {

		private int keyType;

		public KeyEventInfo build() {
			KeyEventInfo info = new KeyEventInfo();
			info.setKeyType(keyType);
			return info;
		}

		public Builder setKeyType(int keyType) {
			this.keyType = keyType;
			return this;
		}
	}
}
