package me.blog.hgl1002.lboard.engine;

public interface PredictionEngine {

	public void start(boolean restarting);

	public void composeChar(String composingChar);

	public void commitComposingChar();

	public boolean backspace();

	public void appendStroke(String stroke);

	public void appendText(CharSequence text);

	public void halfCommitWord();

	public void commitSentence();

	public String getComposing();

	public interface PredictionEngineListener {
		public void onNextWord(Word[] words);
		public void onCurrentWord(Word[] words);
	}

	public void setListener(PredictionEngineListener listener);

}
