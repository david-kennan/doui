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
 *
 */
public class TableTodoItemsContextsAdapter implements ITableAdapter {
	/** Table with links between todo_item and context. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS = "todo_items_contexts";
	/** Table with links between todo_item and context. Primary key. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS_ID = "_id";
	/** Table with links between todo_item and context. Reference to the item. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS = "fk_todo_items";
	/** Table with links between todo_item and context. Reference to context. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS = "fk_todo_contexts";
	/** Table with links between todo_item and context. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_ITEMS_CONTEXTS = 
			"create table "	+ TABLE_TODO_ITEMS_CONTEXTS	+ "("
			+ TABLE_TODO_ITEMS_CONTEXTS_ID	+ " integer primary key autoincrement, "
			+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS + " INTEGER, "
			+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS
			+ " INTEGER, "
			+ "FOREIGN KEY("+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS
			+ ") REFERENCES "+ TableTodoItemsAdapter.TABLE_TODO_ITEMS	+ "(" + TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID	+ "),"
			+ "FOREIGN KEY(" + TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS
			+ ") REFERENCES " + TableTodoContextsAdapter.TABLE_TODO_CONTEXTS
			+ "("+ TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID + ")" + ");";

	private SQLiteOpenHelper sqliteOpenHelper;

	public TableTodoItemsContextsAdapter(SQLiteOpenHelper sqliteOpenHelper) {
		this.sqliteOpenHelper = sqliteOpenHelper;
	}
	
	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(STR_CREATE_TABLE_TODO_ITEMS_CONTEXTS);
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#insert(android.content.ContentValues)
	 */
	public long insert(ContentValues values) {
		return this.sqliteOpenHelper.getWritableDatabase().insert(TABLE_TODO_ITEMS_CONTEXTS,
				null, values);
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#delete(java.lang.String, java.lang.String[])
	 */
	public int delete(String arg1, String[] arg2) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.delete(TABLE_TODO_ITEMS_CONTEXTS, arg1, arg2);
		return result;
	}

	/* (non-Javadoc)
	 * @see co.usersource.doui.database.adapters.ITableAdapter#update(android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.update(TABLE_TODO_ITEMS_CONTEXTS, values, selection,
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
		result = database.query(TABLE_TODO_ITEMS_CONTEXTS, projection, selection,
				selectionArgs, null, null, sortOrder);
		return result;
		}

}
