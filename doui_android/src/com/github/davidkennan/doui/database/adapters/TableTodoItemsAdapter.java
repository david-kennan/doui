/**
 * 
 */
package com.github.davidkennan.doui.database.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author rsh
 * 
 */
public class TableTodoItemsAdapter implements ITableAdapter {

	/** Table where todo items stored. */
	public static final String TABLE_TODO_ITEMS = "todo_items";
	/** Table where todo items stored. Primary key. */
	public static final String TABLE_TODO_ITEMS_ID = "_id";
	/** Table where todo items stored. Title. */
	public static final String TABLE_TODO_ITEMS_TITLE = "title";
	/** Table where todo items stored. Text of the todo. */
	public static final String TABLE_TODO_ITEMS_BODY = "body";
	/** Table where todo items stored. Determinate whether item is done. */
	public static final String TABLE_TODO_ITEMS_IS_DONE = "is_done";
	/** Table where todo items stored. Reference to the primary list item. */
	public static final String TABLE_TODO_ITEMS_FK_LIST = "fk_list";
	/** Table where todo items stored. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_ITEMS = "create table "
			+ TABLE_TODO_ITEMS + "(" + TABLE_TODO_ITEMS_ID
			+ " integer primary key autoincrement, " + TABLE_TODO_ITEMS_TITLE
			+ " TEXT, " + TABLE_TODO_ITEMS_BODY + " TEXT, "
			+ TABLE_TODO_ITEMS_IS_DONE + " INTEGER DEFAULT 0, "
			+ TABLE_TODO_ITEMS_FK_LIST + " INTEGER, " + "FOREIGN KEY("
			+ TABLE_TODO_ITEMS_FK_LIST + ") REFERENCES "
			+ TableTodoListAdapter.TABLE_TODO_LISTS + "("
			+ TableTodoListAdapter.TABLE_TODO_LISTS_ID + ")" + ");";

	private SQLiteOpenHelper sqliteOpenHelper;

	public TableTodoItemsAdapter(SQLiteOpenHelper sqliteOpenHelper) {
		this.sqliteOpenHelper = sqliteOpenHelper;
	}

	public void onCreate(SQLiteDatabase database) {
		database.execSQL(STR_CREATE_TABLE_TODO_ITEMS);
	}

	public long insert(ContentValues values) {
		long result = -1;
		Integer todoListId = values.getAsInteger(TABLE_TODO_ITEMS_FK_LIST);
		if (null == todoListId) {
			Log.e(this.getClass().getName(),
					"todoListId is null, unable to create new TodoItem");
		} else {
			result = this.sqliteOpenHelper.getWritableDatabase().insert(
					TABLE_TODO_ITEMS, null, values);
			// TODO make update for contexts here
		}
		return result;
	}

	public int delete(String arg1, String[] arg2) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.delete(TABLE_TODO_ITEMS, arg1, arg2);
		return result;
	}

	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.update(TABLE_TODO_ITEMS, values, selection, selectionArgs);
		return result;
	}

	public Cursor query(String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		SQLiteDatabase database = this.sqliteOpenHelper.getReadableDatabase();
		result = database.query(TABLE_TODO_ITEMS, projection, selection,
				selectionArgs, null, null, sortOrder);
		return result;
	}

}
