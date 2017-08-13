package me.blog.hgl1002.lboard.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDictionary implements LBoardDictionary {

	protected SQLiteDatabase dbDictionary;

	protected static final String TABLE_NAME_DIC = "dic";
	protected static final String TABLE_NAME_CHAINS = "chain";

	protected static final String COLUMN_NAME_ID = "id";
	protected static final String COLUMN_NAME_STROKE = "stroke";
	protected static final String COLUMN_NAME_CANDIDATE = "candidate";
	protected static final String COLUMN_NAME_PREVIOUS = "previous";
	protected static final String COLUMN_NAME_FREQUENCY = "frequency";
	protected static final String COLUMN_NAME_ATTRIBUTE = "attribute";

	public static final int CHAIN_DELETION_THRESHOLD = 20;
	public static final int CHAIN_DELETION_UNIT = 2;

	public static final int WORD_DELETION_THRESHOLD = 100;
	public static final int WORD_DELETION_UNIT = 2;

	public static final int WORD_DELETION_PERIOD = 50;

	int nextDeletion = WORD_DELETION_PERIOD;

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
				+ COLUMN_NAME_CANDIDATE + " text, "
				+ COLUMN_NAME_FREQUENCY + " integer, "
				+ COLUMN_NAME_ATTRIBUTE + ")";
		if(dbDictionary != null) {
			dbDictionary.execSQL(sql);
		}
	}

	protected void createChainTable(String name) {
		String sql = "create table if not exists " + name + " "
				+ "(" + COLUMN_NAME_PREVIOUS + " text, "
				+ COLUMN_NAME_CANDIDATE + " text, "
				+ COLUMN_NAME_FREQUENCY + " integer, "
				+ COLUMN_NAME_ATTRIBUTE + ")";
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
		String sql = "select * from " + TABLE_NAME_DIC
				+ " where " + COLUMN_NAME_STROKE + " like ?"
				+ " order by " + COLUMN_NAME_FREQUENCY + " desc ";
		String[] args = new String[] {
				keyString + "%"
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);
		List<Word> words = new ArrayList<>();
		while(cursor.moveToNext()) {
			String candidate = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CANDIDATE));
			String stroke = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_STROKE));
			int frequency = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_FREQUENCY));
			int attribute = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ATTRIBUTE));
			Word word = new Word(candidate, stroke, frequency, attribute);
			words.add(word);
		}
		Word[] result = new Word[words.size()];
		return words.toArray(result);
	}

	@Override
	public Word[] searchNextWord(int operation, int order, String keyString, Word[] previousWords) {
		String previous = "";
		String last = "";
		for(Word word : previousWords) {
			last = word.getCandidate();
			previous += last + ";";
		}
		previous = previous.substring(0, previous.length()-1);
		String sql = "select * from " + TABLE_NAME_CHAINS
				+ " where " + COLUMN_NAME_PREVIOUS + "=?"
				+ " order by " + COLUMN_NAME_FREQUENCY + " desc";
		String[] args = new String[] {
				previous
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);

		List<Word> list = new ArrayList<>();
		List<String> candidates = new ArrayList<>();

		while(cursor.moveToNext()) {
			String candidate = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CANDIDATE));
			int frequency = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_FREQUENCY));
			int attribute = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ATTRIBUTE));
			if(candidate.equals("END")) {
			} else {
				list.add(new Word(candidate, null, frequency, attribute));
				candidates.add(candidate);
			}
		}
		cursor.close();

		sql = "select * from " + TABLE_NAME_CHAINS
				+ " where " + COLUMN_NAME_PREVIOUS + " like ?"
				+ " order by " + COLUMN_NAME_FREQUENCY + " desc";
		args = new String[] {
				"%;" + last
		};
		cursor = dbDictionary.rawQuery(sql, args);
		while(cursor.moveToNext()) {
			String candidate = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CANDIDATE));
			int frequency = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_FREQUENCY));
			int attribute = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ATTRIBUTE));
			if(candidate.equals("END")) {
			} else {
				if(!candidates.contains(candidate)) list.add(new Word(candidate, null, frequency, attribute));
			}
		}
		cursor.close();

		Word[] result = new Word[list.size()];
		return list.toArray(result);
	}

	public int learnWord(Word word) {

		if(--nextDeletion <= 0) {
			deleteUnusedWords();
			nextDeletion = WORD_DELETION_PERIOD;
		}

		if(word.getStroke() == null) return -1;

		String candidate = word.getCandidate();
		String stroke = word.getStroke();
		int frequency = word.getFrequency();
		if(frequency == 0) frequency = 1;
		int attribute = word.getAttribute();

		String sql = "select * from " + TABLE_NAME_DIC
				+ " where " + COLUMN_NAME_STROKE + "=?"
				+ " and " + COLUMN_NAME_CANDIDATE + "=?";
		String[] args = new String[] {
				stroke,
				candidate
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);
		if(cursor.getCount() > 0) {
			cursor.moveToNext();
			int freq = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_FREQUENCY));
			cursor.close();
			sql = "update " + TABLE_NAME_DIC
					+ " set " + COLUMN_NAME_FREQUENCY + "=?, "
					+ COLUMN_NAME_ATTRIBUTE + "=?"
					+ " where " + COLUMN_NAME_STROKE + "=?"
					+ " and " + COLUMN_NAME_CANDIDATE + "=?";
			args = new String[] {
					String.valueOf(frequency + freq),
					stroke,
					candidate
			};
			dbDictionary.execSQL(sql, args);
			return 2;
		}
		sql = "insert into " + TABLE_NAME_DIC + " ("
				+ COLUMN_NAME_STROKE + ", "
				+ COLUMN_NAME_CANDIDATE + ", "
				+ COLUMN_NAME_FREQUENCY + ", "
				+ COLUMN_NAME_ATTRIBUTE + ") values(?, ?, ?, ?)";
		args = new String[] {
				stroke,
				candidate,
				String.valueOf(frequency),
				String.valueOf(attribute)
		};
		dbDictionary.execSQL(sql, args);
		return 1;
	}

	public int learnChain(WordChain chain) {
		String previous = getPreviousString(chain);

		deleteUnusedChains(previous);

		String candidate = chain.get(chain.size()-1).getCandidate();
		int attribute = chain.get(chain.size()-1).getAttribute();
		String sql;

		sql = "select * from " + TABLE_NAME_CHAINS + " where " + COLUMN_NAME_PREVIOUS + "=? and " + COLUMN_NAME_CANDIDATE + "=?";
		String[] args = new String[] {
				previous,
				candidate
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);
		if(cursor.getCount() > 0) {
			cursor.moveToNext();
			int frequency = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_FREQUENCY));
			cursor.close();
			sql = "update " + TABLE_NAME_CHAINS
					+ " set " + COLUMN_NAME_FREQUENCY + "=?"
					+ " where " + COLUMN_NAME_PREVIOUS + "=?"
					+ " and " + COLUMN_NAME_CANDIDATE + "=?";
			args = new String[] {
					String.valueOf(++frequency),
					previous,
					candidate
			};
			dbDictionary.execSQL(sql, args);
			return 2;
		}

		sql = "insert into " + TABLE_NAME_CHAINS + " ("
				+ COLUMN_NAME_PREVIOUS + ", "
				+ COLUMN_NAME_CANDIDATE + ", "
				+ COLUMN_NAME_FREQUENCY + ", "
				+ COLUMN_NAME_ATTRIBUTE + ") values(?, ?, ?, ?)";
		args = new String[] {
				previous,
				candidate,
				"1",
				String.valueOf(attribute)
		};
		dbDictionary.execSQL(sql, args);
		return 1;
	}

	public int deleteUnusedChains(WordChain chain) {
		return this.deleteUnusedChains(getPreviousString(chain));
	}

	public int deleteUnusedChains(String previous) {
		String sql = "select * from " + TABLE_NAME_CHAINS
				+ " where " + COLUMN_NAME_PREVIOUS + "=?"
				+ " and " + COLUMN_NAME_FREQUENCY + " > ?";
		String[] args = new String[] {
				previous,
				String.valueOf(CHAIN_DELETION_THRESHOLD)
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);
		if(cursor.getCount() > 0) {
			sql = "update " + TABLE_NAME_CHAINS
					+ " set " + COLUMN_NAME_FREQUENCY +  " = " + COLUMN_NAME_FREQUENCY + " / ?"
					+ " where " + COLUMN_NAME_PREVIOUS + " = ?";
			args = new String[] {
					String.valueOf(CHAIN_DELETION_UNIT),
					previous
			};
			dbDictionary.execSQL(sql, args);
			sql = "delete from " + TABLE_NAME_CHAINS
					+ " where " + COLUMN_NAME_FREQUENCY + " <= 0";
			dbDictionary.execSQL(sql);
			return 1;
		}
		return 0;
	}

	public int deleteUnusedWords() {
		String sql = "select * from " + TABLE_NAME_DIC
				+ " where " + COLUMN_NAME_FREQUENCY + " > ?";
		String[] args = new String[] {
				String.valueOf(WORD_DELETION_THRESHOLD)
		};
		Cursor cursor = dbDictionary.rawQuery(sql, args);
		if(cursor.getCount() > 0) {
			sql = "update " + TABLE_NAME_DIC
					+ " set " + COLUMN_NAME_FREQUENCY +  " = " + COLUMN_NAME_FREQUENCY + " / ?";
			args = new String[] {
					String.valueOf(WORD_DELETION_UNIT),
			};
			dbDictionary.execSQL(sql, args);
			sql = "delete from " + TABLE_NAME_CHAINS
					+ " where " + COLUMN_NAME_FREQUENCY + " <= 0";
			dbDictionary.execSQL(sql);
			return 1;
		}
		return 0;
	}

	public String getPreviousString(WordChain chain) {
		String previous = "";
		for(int i = 0 ; i < chain.size()-1 ; i++) {
			previous += chain.get(i).getCandidate() + ";";
		}
		previous = previous.substring(0, previous.length() - 1);
		return previous;
	}

}
