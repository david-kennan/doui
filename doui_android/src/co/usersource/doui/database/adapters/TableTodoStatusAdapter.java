/**
 * 
 */
package co.usersource.doui.database.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author rsh
 * Implementation for the adapter to manage ToDoItem statuses.
 */
public class TableTodoStatusAdapter implements ITableAdapter {

	/** Table with status items. */
	public static final String TABLE_TODO_STATUSES = "todo_statuses";
	/** Table with status items. Primary key. */
	public static final String TABLE_TODO_STATUSES_ID = "_id";
	/** Table with status items. Name. */
	public static final String TABLE_TODO_STATUSES_NAME = "name";
	/** Table with status items. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_STATUSES = "create table "
			+ TABLE_TODO_STATUSES+ "(" + TABLE_TODO_STATUSES_ID
			+ " integer primary key autoincrement, " + TABLE_TODO_STATUSES_NAME
			+ " TEXT" + ");";

	/** Pre-defined array of Statuses. */
	public static final String STR_ARRAY_STATUSES[] = { "Next",
		"Calendar", "Waiting", "Done", "Someday"};
	
	private SQLiteOpenHelper sqliteOpenHelper;

	public TableTodoStatusAdapter(SQLiteOpenHelper sqliteOpenHelper) {
		this.sqliteOpenHelper = sqliteOpenHelper;
	}
	
	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(STR_CREATE_TABLE_TODO_STATUSES);
		for (int i = 0; i < STR_ARRAY_STATUSES.length; i++) {
			database.execSQL("insert or replace into " + TABLE_TODO_STATUSES + "("
					+ TABLE_TODO_STATUSES_ID + "," + TABLE_TODO_STATUSES_NAME
					+ ") values (" + i  + ",'" + STR_ARRAY_STATUSES[i] + "');");
		}

	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#insert(android.content.ContentValues)
	 */
	public long insert(ContentValues values) {
		return this.sqliteOpenHelper.getWritableDatabase().insert(
				TABLE_TODO_STATUSES, null, values);
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#delete(java.lang.String, java.lang.String[])
	 */
	public int delete(String arg1, String[] arg2) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.delete(TABLE_TODO_STATUSES, arg1, arg2);
		return result;
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#update(android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.update(TABLE_TODO_STATUSES, values, selection,
				selectionArgs);
		return result;
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#query(java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	public Cursor query(String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		SQLiteDatabase database = this.sqliteOpenHelper.getReadableDatabase();
		result = database.query(TABLE_TODO_STATUSES, projection, selection,
				selectionArgs, null, null, sortOrder);
		return result;
	}

}
