/**
 * 
 */
package com.github.davidkennan.doui.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author rsh
 * 
 *         This class intended to provide access and creation routines for Doui
 *         program database.
 */
public class DouiSQLiteOpenHelper extends SQLiteOpenHelper {

	/** Database name. */
	public static final String DATABASE_NAME = "commments.db";
	/** Version for upgrade routines. */
	public static final int DATABASE_VERSION = 1;

	/** Table where todo items stored. */
	public static final String TABLE_TODO_ITEMS = "todo_items";
	/** Table where todo items stored. Primary key. */
	public static final String TABLE_TODO_ITEMS_ID = "_id";
	/** Table where todo items stored. Title. */
	public static final String TABLE_TODO_ITEMS_TITLE = "title";
	/** Table where todo items stored. Text of the todo. */
	public static final String TABLE_TODO_ITEMS_BODY = "body";
	/** Table where todo items stored. Reference to the primary list item. */
	public static final String TABLE_TODO_ITEMS_FK_LIST = "fk_list";
	/** Table where todo items stored. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_ITEMS = "create table "
			+ TABLE_TODO_ITEMS + "(" + TABLE_TODO_ITEMS_ID
			+ " integer primary key autoincrement, " + TABLE_TODO_ITEMS_TITLE
			+ " TEXT, " + TABLE_TODO_ITEMS_BODY + " TEXT, "
			+ TABLE_TODO_ITEMS_FK_LIST + " INTEGER, " + "FOREIGN KEY("
			+ TABLE_TODO_ITEMS_FK_LIST + ") REFERENCES " + TableTodoListAdapter.TABLE_TODO_LISTS
			+ "(" + TableTodoListAdapter.TABLE_TODO_LISTS_ID + ")" + ");";

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

	/** Table with links between todo_item and context. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS = "todo_items_contexts";
	/** Table with links between todo_item and context. Primary key. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS_ID = "_id";
	/** Table with links between todo_item and context. Reference to the item. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS = "fk_todo_items";
	/** Table with links between todo_item and context. Reference to context. */
	public static final String TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS = "fk_todo_contexts";
	/** Table with links between todo_item and context. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_ITEMS_CONTEXTS = "create table "
			+ TABLE_TODO_ITEMS_CONTEXTS
			+ "("
			+ TABLE_TODO_ITEMS_CONTEXTS_ID
			+ " integer primary key autoincrement, "
			+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS
			+ " INTEGER, "
			+ "FOREIGN KEY("
			+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS
			+ ") REFERENCES "
			+ TABLE_TODO_ITEMS
			+ "("
			+ TABLE_TODO_ITEMS_ID
			+ "),"
			+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS
			+ " INTEGER, "
			+ "FOREIGN KEY("
			+ TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS
			+ ") REFERENCES "
			+ TABLE_TODO_CONTEXTS
			+ "("
			+ TABLE_TODO_CONTEXTS_ID + ")," + ");";

	/** Helper member to access TodoList table. */
	private TableTodoListAdapter tableTodoListAdapter;

	/**
	 * @return the tableTodoListAdapter
	 */
	public TableTodoListAdapter getTableTodoListAdapter() {
		return tableTodoListAdapter;
	}

	/**
	 * Constructor.
	 * */
	public DouiSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		tableTodoListAdapter = new TableTodoListAdapter(this);  
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {
		tableTodoListAdapter.onCreate(database);
		database.execSQL(STR_CREATE_TABLE_TODO_CONTEXTS);
		database.execSQL(STR_CREATE_TABLE_TODO_ITEMS);
		database.execSQL(STR_CREATE_TABLE_TODO_ITEMS_CONTEXTS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// We have no version migration procedure now.
	}

}
