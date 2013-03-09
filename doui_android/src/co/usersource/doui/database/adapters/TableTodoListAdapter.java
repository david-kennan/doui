/**
 * 
 */
package co.usersource.doui.database.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author rsh Adapter class for the Todo Lists table.
 */
public class TableTodoListAdapter implements ITableAdapter {

	/** Table with lists of the todo. */
	public static final String TABLE_TODO_LISTS = "todo_lists";
	/** Table with lists of the todo. Primary key. */
	public static final String TABLE_TODO_LISTS_ID = "_id";
	/** Table with lists of the todo. Name of the list. */
	public static final String TABLE_TODO_LISTS_NAME = "name";
	/** Table with lists of the todo. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_LISTS = "create table "
			+ TABLE_TODO_LISTS + "(" + TABLE_TODO_LISTS_ID
			+ " integer primary key autoincrement, " + TABLE_TODO_LISTS_NAME
			+ " TEXT" + ");";
	/** Pre-defined array of Lists. */
	public static final String STR_ARRAY_LISTS[] = { "Next", "Waiting",
			"Finance and admin", "Health", "Someday", "...ETC..." };

	private SQLiteOpenHelper sqliteOpenHelper;

	public TableTodoListAdapter(SQLiteOpenHelper sqliteOpenHelper) {
		this.sqliteOpenHelper = sqliteOpenHelper;
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.ITableAdapter#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(STR_CREATE_TABLE_TODO_LISTS);
		for (int i = 0; i < STR_ARRAY_LISTS.length; i++) {
			database.execSQL("insert or replace into " + TABLE_TODO_LISTS + "("
					+ TABLE_TODO_LISTS_ID + "," + TABLE_TODO_LISTS_NAME
					+ ") values (" + i  + ",'" + STR_ARRAY_LISTS[i] + "');");
		}
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.ITableAdapter#insert(android.content.ContentValues)
	 */
	public long insert(ContentValues values) {
		return this.sqliteOpenHelper.getWritableDatabase().insert(TABLE_TODO_LISTS,
				null, values);
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.ITableAdapter#delete(java.lang.String, java.lang.String[])
	 */
	public int delete(String arg1, String[] arg2) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.ITableAdapter#update(android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.ITableAdapter#query(java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	public Cursor query(String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		SQLiteDatabase database = this.sqliteOpenHelper.getReadableDatabase();
		result = database.query(TABLE_TODO_LISTS, projection, selection,
				selectionArgs, null, null, sortOrder);
		return result;
	}
}
