package me.blog.hgl1002.lboard.ime.charactergenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import me.blog.hgl1002.lboard.expression.TreeParser;
import me.blog.hgl1002.lboard.ime.charactergenerator.basic.AutomataRule;
import me.blog.hgl1002.lboard.ime.charactergenerator.basic.AutomataTable;
import me.blog.hgl1002.lboard.ime.charactergenerator.basic.CombinationTable;

import static me.blog.hgl1002.lboard.ime.charactergenerator.BasicCodeSystem.*;

public class BasicCharacterGenerator implements CharacterGenerator {

	protected TreeParser parser;

	protected CharacterGeneratorListener listener;

	protected State currentState;
	protected Stack<State> previousStates;

	protected CombinationTable combinationTable;
	protected AutomataTable automataTable;

	protected static class State implements Cloneable {
		long syllable;
		long status;
		int H;
		int iCho, iJung, iJong;
		int cho, jung, jong;
		@Override
		public Object clone() {
			State state = new State();
			state.syllable = syllable;
			state.status = status;
			state.H = H;
			state.cho = cho;
			state.jung = jung;
			state.jong = jong;
			return state;
		}
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
			currentState = (State) currentState.clone();

			currentState.H = 3;
			int cho, jung, jong;
			cho = jung = jong = 0;
			if(hasCho(code)) {
				cho = (int) getCho(code);
				currentState.iCho = cho;
			}
			if(hasJung(code)) {
				jung = (int) getJung(code);
				currentState.iJung = jung;
			}
			if(hasJong(code)) {
				jong = (int) getJong(code);
				currentState.iJong = jong;
			}

			processAutomata();

			long syllable = currentState.syllable;
			if(cho != 0) {
				if(currentState.cho != 0) {
					// TODO: try to combine jamo.
				}
				syllable &= ~MASK_CHO;
				syllable |= fromCho(cho);
			}
			if(jung != 0) {
				if(currentState.jung != 0) {
				}
				syllable &= ~MASK_JUNG;
				syllable |= fromJung(jung);
			}
			if(jong != 0) {
				if(currentState.jong != 0) {
				}
				syllable &= ~MASK_JONG;
				syllable |= fromJong(jong);
			}
			// TODO check hangul range.
			if(cho != 0) currentState.cho = cho;
			if(jung != 0) currentState.jung = jung;
			if(jong != 0) currentState.jong = jong;
			syllable = (syllable & ~MASK_CODE_TYPE) | H3;
			currentState.syllable = syllable;

			String composing = convertToUnicode(currentState.syllable);
			if(listener != null) listener.onCompose(this, composing);
			return true;
		}
		return false;
	}

	public void processAutomata() {
		AutomataRule automata = automataTable.get(currentState.status);
		Map<String, Long> variables = getVariables();
		variables.put("A", (long) currentState.iCho);
		variables.put("B", (long) currentState.iJung);
		variables.put("C", (long) currentState.iJong);
		variables.put("T", 0L);
		parser.setVariables(variables);
		long result = parser.parse(automata.getTargetState());
		currentState.status = result;
		if(result == 0) {
			automata = automataTable.get(0);
			parser.setVariables(getVariables());
			resetComposing();
			previousStates.push(currentState);
			currentState = (State) currentState.clone();
			currentState.status = parser.parse(automata.getTargetState());
		}
	}

	@Override
	public boolean backspace() {
		if(previousStates.isEmpty()) {
			return false;
		} else {
			currentState = previousStates.pop();
			String composing = convertToUnicode(currentState.syllable);
			if(currentState.syllable == 0) composing = "";
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
		Map<String, Long> variables = new HashMap<>();
		variables.put("D", (long) currentState.cho);
		variables.put("E", (long) currentState.jung);
		variables.put("F", (long) currentState.jong);
		variables.put("T", currentState.status);
		return variables;
	}

	@Override
	public void setListener(CharacterGeneratorListener listener) {
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		this.listener = null;
	}

	public TreeParser getParser() {
		return parser;
	}

	public void setParser(TreeParser parser) {
		this.parser = parser;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	public CombinationTable getCombinationTable() {
		return combinationTable;
	}

	public void setCombinationTable(CombinationTable combinationTable) {
		this.combinationTable = combinationTable;
	}

	public AutomataTable getAutomataTable() {
		return automataTable;
	}

	public void setAutomataTable(AutomataTable automataTable) {
		this.automataTable = automataTable;
	}
}
