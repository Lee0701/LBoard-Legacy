package me.blog.hgl1002.lboard.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SQLiteDictionary implements LBoardDictionary {

	protected SQLiteDatabase dbDictionary;

	protected static final String TABLE_NAME_DIC = "dic";
	protected static final String TABLE_NAME_CHAINS = "chain";

	protected static final String COLUMN_NAME_ID = "id";
	protected static final String COLUMN_NAME_STROKE = "stroke";
	protected static final String COLUMN_NAME_CANDIDATE = "candidate";
	protected static final String COLUMN_NAME_PREVIOUS = "previous";
	protected static final String COLUMN_NAME_FREQUENCY = "frequency";

	public SQLiteDictionary(String dbFilePath) {
		if(dbFilePath != null) {
			this.dbDictionary = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
			createDictionaryTable(TABLE_NAME_DIC);
			createChainTable(TABLE_NAME_CHAINS);
		}
	}

	protected void createDictionaryTable(String name) {
		String sql = "create table if not exists " + name + " "
				+ "(" + COLUMN_NAME_ID + " integer primary key autoincrement, "
				+ COLUMN_NAME_STROKE + " text, "
				+ COLUMN_NAME_CANDIDATE + " text)";
		if(dbDictionary != null) {
			dbDictionary.execSQL(sql);
		}
	}

	protected void createChainTable(String name) {
		String sql = "create table if not exists " + name + " "
				+ "(" + COLUMN_NAME_PREVIOUS + " text, "
				+ COLUMN_NAME_CANDIDATE + " text)";
		if(dbDictionary != null) {
			dbDictionary.execSQL(sql);
		}
	}

	protected void closeDatabase() {
		if(dbDictionary != null) {
			dbDictionary.close();
			dbDictionary = null;
		}
	}

	@Override
	public Word[] searchCurrentWord(int operation, int order, String keyString) {
		return null;
	}

	@Override
	public Word[] searchNextWord(int operation, int order, String keyString, Word[] previousWords) {
		String previous = "";
		for(Word word : previousWords) {
			previous += word.getCandidate() + ";";
		}
		previous = previous.substring(0, previous.length()-1);
		String sql = "select * from " + TABLE_NAME_CHAINS + " where " + COLUMN_NAME_PREVIOUS + "=?";
		String[] args = new String[] {
				previous
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);

		if(cursor.getCount() == 0) {
			cursor.close();
			sql = "select * from " + TABLE_NAME_CHAINS + " where " + COLUMN_NAME_PREVIOUS + " like ?";
			cursor = dbDictionary.rawQuery(sql, args);
		}

		Map<String, Word> list = new HashMap<>();
		while(cursor.moveToNext()) {
			String candidate = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CANDIDATE));
			if(list.containsKey(candidate)) {
				list.get(candidate).frequency++;
			} else {
				list.put(candidate, new Word(candidate, null));
			}
		}
		cursor.close();
		list = sort(list);
		Word[] result = new Word[list.values().size()];
		return list.values().toArray(result);
	}

	public int learn(WordChain chain) {
		String previous = "";
		for(int i = 0 ; i < chain.size()-1 ; i++) {
			previous += chain.get(i).getCandidate() + ";";
		}
		previous = previous.substring(0, previous.length() - 1);
		String candidate = chain.get(chain.size()-1).getCandidate();

		String sql;

		sql = "select * from " + TABLE_NAME_CHAINS + " where " + COLUMN_NAME_PREVIOUS + "=? and " + COLUMN_NAME_CANDIDATE + "=?";
		String[] args = new String[] {
				previous,
				candidate
		};
		if(dbDictionary.rawQuery(sql, args).getCount() > 0) {
			return 0;
		}

		sql = "insert into " + TABLE_NAME_CHAINS + " ("
				+ COLUMN_NAME_PREVIOUS + ", "
				+ COLUMN_NAME_CANDIDATE + ") values(\""
				+ previous + "\", \"" + candidate + "\")";
		try {
			dbDictionary.execSQL(sql);
		} catch(Exception e) {
			return -1;
		}
		return 1;
	}

	protected Map<String, Word> sort(Map<String, Word> map) {
		List<Map.Entry<String, Word>> list = new LinkedList<Map.Entry<String, Word>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Word>>() {
			@Override
			public int compare(Map.Entry<String, Word> o1, Map.Entry<String, Word> o2) {
				return o1.getValue().getFrequency() - o2.getValue().getFrequency();
			}
		});
		Map<String, Word> result = new LinkedHashMap<>();
		for(Map.Entry<String, Word> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}