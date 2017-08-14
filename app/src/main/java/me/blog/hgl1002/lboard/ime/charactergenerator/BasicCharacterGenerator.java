package me.blog.hgl1002.lboard.ime.charactergenerator;

import java.util.Map;
import java.util.Stack;

import me.blog.hgl1002.lboard.expression.TreeParser;
import me.blog.hgl1002.lboard.ime.charactergenerator.basic.AutomataTable;

import static me.blog.hgl1002.lboard.ime.charactergenerator.BasicCodeSystem.*;

public class BasicCharacterGenerator implements CharacterGenerator {

	protected TreeParser parser;

	protected CharacterGeneratorListener listener;

	protected State currentState;
	protected Stack<State> previousStates;

	protected AutomataTable automataTable;

	protected static class State {
		long syllable;
		int status;
		int H;
		int iCho, iJung, iJong;
		int cho, jung, jong;

	}

	public BasicCharacterGenerator(TreeParser parser) {
		this.parser = parser;
		previousStates = new Stack<>();
		currentState = new State();
	}

	@Override
	public boolean onCode(long code) {
		if(isH3(code)) {
			State previousState = currentState;
			previousStates.push(currentState);
			currentState = new State();
			currentState.cho = previousState.cho;
			currentState.jung = previousState.jung;
			currentState.jong = previousState.jong;
			currentState.syllable = previousState.syllable;

			currentState.H = 3;
			int cho, jung, jong;
			cho = jung = jong = 0;
			if(hasCho(code)) {
				cho = (int) ((code & MASK_CHO) >> 0x20);
				currentState.iCho = cho;
				currentState.syllable &= ~MASK_CHO;
			}
			if(hasJung(code)) {
				jung = (int) ((code & MASK_JUNG) >> 0x10);
				currentState.iJung = jung;
				currentState.syllable &= ~MASK_JUNG;
			}
			if(hasJong(code)) {
				jong = (int) ((code & MASK_JONG) >> 0x00);
				currentState.iJong = jong;
				currentState.syllable &= ~MASK_JONG;
			}
			// TODO: evaluate normal automata.
			long syllable = currentState.syllable;
			if(cho != 0) {
				if(currentState.cho != 0) {
					// TODO: try to combine jamo.
				}
				syllable &= ~MASK_CHO;
				syllable |= cho << 0x20;
			}
			if(jung != 0) {
				if(currentState.jung != 0) {
				}
				syllable &= ~MASK_JUNG;
				syllable |= jung << 0x10;
			}
			if(jong != 0) {
				if(currentState.jong != 0) {
				}
				syllable &= ~MASK_JONG;
				syllable |= jong << 0x00;
			}
			// TODO check hangul range.
			if(cho != 0) currentState.cho = cho;
			if(jung != 0) currentState.jung = jung;
			if(jong != 0) currentState.jong = jong;
			currentState.syllable = syllable;

			String composing = convertToUnicode(currentState.syllable);
			if(listener != null) listener.onCompose(this, composing);
			return true;
		}
		return false;
	}

	@Override
	public boolean backspace() {
		if(previousStates.isEmpty()) {
			return false;
		} else {
			currentState = previousStates.pop();
			String composing = convertToUnicode(currentState.syllable);
			if(listener != null) listener.onCompose(this, composing);
			return true;
		}
	}

	@Override
	public void resetComposing() {
		previousStates.clear();
		currentState = new State();
		listener.onCommit(this);
	}

	@Override
	public String getStroke() {
		return null;
	}

	@Override
	public Map<String, Long> getVariables() {
		return null;
	}

	@Override
	public void setListener(CharacterGeneratorListener listener) {
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		this.listener = null;
	}
}
