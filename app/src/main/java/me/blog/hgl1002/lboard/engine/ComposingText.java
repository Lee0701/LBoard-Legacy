package me.blog.hgl1002.lboard.engine;

import java.util.ArrayList;
import java.util.List;

public class ComposingText {

	public static final int MAX_LAYERS = 3;

	protected List<StringSegment>[] layers;
	protected int[] cursors;

	public ComposingText() {
		layers = new ArrayList[MAX_LAYERS];
		cursors = new int[MAX_LAYERS];
		for(int i = 0 ; i < MAX_LAYERS ; i++) {
			layers[i] = new ArrayList<>();
			cursors[i] = 0;
		}
	}

	public void insert(int layer, StringSegment str) {
		int cursor = cursors[layer];
		layers[layer].add(cursor, str);
	}

}
