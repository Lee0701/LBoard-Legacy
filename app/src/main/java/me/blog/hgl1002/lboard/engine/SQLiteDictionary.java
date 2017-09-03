package me.blog.hgl1002.lboard.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
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
		if(words.length >= 5) {
		}
		for(int i = 5 ; i >= 2 ; i--) {
			if(words.length >= i) {
				String tableName = "";
				switch (i) {
				case 5:
					tableName = TABLE_NAME_QUINQGRAMS;
					break;
				case 4:
					tableName = TABLE_NAME_QUADGRAMS;
					break;
				case 3:
					tableName = TABLE_NAME_TRIGRAMS;
					break;
				case 2:
					tableName = TABLE_NAME_BIGRAMS;
					break;
				}
				learnChain(tableName, Arrays.copyOfRange(words, words.length-6, words.length-1));
			}
		}
		return 0;
	}

	protected void learnChain(String tableName, Word[] words) {
		String sql = "select * from " + tableName;
		sql += " where " + getWhereClause(words.length);
		String[] values = new String[words.length];
		for(int i = 0 ; i < words.length ; i++) {
			values[i] = words[i].getCandidate();
		}
		Cursor cursor = dbDictionary.rawQuery(sql, values);
		if(cursor.getCount() > 0) {
			sql = "update " + tableName;
			sql += " set " + COLUMN_NAME_FREQUENCY + " = " + COLUMN_NAME_FREQUENCY + " + 1";
			sql += " where " + getWhereClause(words.length);
			dbDictionary.execSQL(sql, values);
		} else {
			sql = "insert into " + tableName;
			sql += " (" + COLUMN_NAME_X;
			sql += ", " + COLUMN_NAME_Y;
			if(words.length >= 3) sql += ", " + COLUMN_NAME_A;
			if(words.length >= 4) sql += ", " + COLUMN_NAME_B;
			if(words.length >= 5) sql += ", " + COLUMN_NAME_C;
			sql += ")";
			sql += " values(?";
			for(int i = 1 ; i < words.length ; i++) sql += ", ?";
			sql += ")";
			dbDictionary.execSQL(sql, values);
		}
	}

	private String getWhereClause(int length) {
		String sql = "";
		sql += COLUMN_NAME_X + " = ?";
		sql += " and " + COLUMN_NAME_Y + " = ?";
		if(length >= 3) sql += " and " + COLUMN_NAME_A + " = ?";
		if(length >= 4) sql += " and " + COLUMN_NAME_B + " = ?";
		if(length >= 5) sql += " and " + COLUMN_NAME_C + " = ?";
		return sql;
	}

}
