package me.blog.hgl1002.lboard.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDictionary implements WritableDictionary {

	protected SQLiteDatabase dbDictionary;

	protected static final String TABLE_NAME_UNIGRAMS = "unigrams";
	protected static final String TABLE_NAME_BIGRAMS = "bigrams";
	protected static final String TABLE_NAME_TRIGRAMS = "trigrams";
	protected static final String TABLE_NAME_QUADGRAMS = "quadgrams";
	protected static final String TABLE_NAME_QUINQGRAMS = "quinqgrams";

	protected static final String COLUMN_NAME_ID = "id";
	protected static final String COLUMN_NAME_STROKE = "stroke";
	protected static final String COLUMN_NAME_CANDIDATE = "candidate";
	protected static final String COLUMN_NAME_X = "x";
	protected static final String COLUMN_NAME_Y = "y";
	protected static final String COLUMN_NAME_A = "a";
	protected static final String COLUMN_NAME_B = "b";
	protected static final String COLUMN_NAME_C = "c";
	protected static final String COLUMN_NAME_POS = "pos";
	protected static final String COLUMN_NAME_FREQUENCY = "frequency";
	protected static final String COLUMN_NAME_ATTRIBUTE = "attribute";

	public SQLiteDictionary(String dbFilePath) {
		if(dbFilePath != null) {
			this.dbDictionary = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
			createTables();
		}
	}

	protected void createTables() {
		if(dbDictionary == null) return;
		String sql = "create table if not exists " + TABLE_NAME_UNIGRAMS + " "
				+ "(" + COLUMN_NAME_ID + " integer primary key autoincrement, "
				+ COLUMN_NAME_STROKE + " text, "
				+ COLUMN_NAME_CANDIDATE + " text, "
				+ COLUMN_NAME_FREQUENCY + " integer, "
				+ COLUMN_NAME_ATTRIBUTE + " integer)";
		dbDictionary.execSQL(sql);

		sql = "create table if not exists " + TABLE_NAME_BIGRAMS + " "
				+ "(" + COLUMN_NAME_X + " integer, "
				+ COLUMN_NAME_Y + " integer, "
				+ COLUMN_NAME_FREQUENCY + " integer)";
		dbDictionary.execSQL(sql);

		sql = "create table if not exists " + TABLE_NAME_TRIGRAMS + " "
				+ "(" + COLUMN_NAME_X + " integer, "
				+ COLUMN_NAME_Y + " integer, "
				+ COLUMN_NAME_A + " integer, "
				+ COLUMN_NAME_FREQUENCY + " integer)";
		dbDictionary.execSQL(sql);

		sql = "create table if not exists " + TABLE_NAME_QUADGRAMS + " "
				+ "(" + COLUMN_NAME_X + " integer, "
				+ COLUMN_NAME_Y + " integer, "
				+ COLUMN_NAME_A + " integer, "
				+ COLUMN_NAME_B + " integer, "
				+ COLUMN_NAME_FREQUENCY + " integer)";
		dbDictionary.execSQL(sql);

		sql = "create table if not exists " + TABLE_NAME_QUINQGRAMS + " "
				+ "(" + COLUMN_NAME_X + " integer, "
				+ COLUMN_NAME_Y + " integer, "
				+ COLUMN_NAME_A + " integer, "
				+ COLUMN_NAME_B + " integer, "
				+ COLUMN_NAME_C + " integer, "
				+ COLUMN_NAME_FREQUENCY + " integer)";
		dbDictionary.execSQL(sql);
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
		return null;
	}

	@Override
	public int learnWord(Word word) {
		return 0;
	}

	@Override
	public int learnWords(Word[] words) {
		return 0;
	}

}
