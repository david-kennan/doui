/**
 * 
 */
package com.github.davidkennan.doui.database.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author rsh
 *
 */
public class TableTodoContextsAdapter implements ITableAdapter {

	/** Table with contexts (@NAME items). */
	public static final String TABLE_TODO_CONTEXTS = "todo_contexts";
	/** Table with contexts (@NAME items). Primary key. */
	public static final String TABLE_TODO_CONTEXTS_ID = "_id";
	/** Table with contexts (@NAME items). Name. */
	public static final String TABLE_TODO_CONTEXTS_NAME = "name";
	/** Table with contexts (@NAME items). Create statement. */
	public static final String STR_CREATE_TABLE_TODO_CONTEXTS = "create table "
			+ TABLE_TODO_CONTEXTS + "(" + TABLE_TODO_CONTEXTS_ID
			+ " integer primary key autoincrement, " + TABLE_TODO_CONTEXTS_NAME
			+ " TEXT" + ");";

	private SQLiteOpenHelper sqliteOpenHelper;

	public TableTodoContextsAdapter(SQLiteOpenHelper sqliteOpenHelper) {
		this.sqliteOpenHelper = sqliteOpenHelper;
	}
	/* (non-Javadoc)
	 * @see com.github.davidkennan.doui.database.adapters.ITableAdapter#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(STR_CREATE_TABLE_TODO_CONTEXTS);
	}

	/* (non-Javadoc)
	 * @see com.github.davidkennan.doui.database.adapters.ITableAdapter#insert(android.content.ContentValues)
	 */
	public void insert(ContentValues values) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.github.davidkennan.doui.database.adapters.ITableAdapter#delete(java.lang.String, java.lang.String[])
	 */
	public int delete(String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.github.davidkennan.doui.database.adapters.ITableAdapter#update(android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.github.davidkennan.doui.database.adapters.ITableAdapter#query(java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	public Cursor query(String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

}
