/**
 * 
 */
package co.usersource.doui.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.usersource.doui.database.adapters.ITableAdapter;
import co.usersource.doui.database.adapters.TableTodoContextsAdapter;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoItemsContextsAdapter;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

/**
 * @author rsh
 * 
 *         This class intended to provide access and creation routines for Doui
 *         program database.
 */
public class DouiSQLiteOpenHelper extends SQLiteOpenHelper {

	/** Database name. */
	public static final String DATABASE_NAME = "todo.db";
	/** Version for upgrade routines. */
	public static final int DATABASE_VERSION = 1;


	/** Helper member to access TodoList table. */
	private ITableAdapter tableTodoListAdapter;
	/** Helper member to access TodoItems table. */
	private TableTodoItemsAdapter tableTodoItemsAdapter;
	/** Helper member to access TodoContexts table. */
	private TableTodoContextsAdapter tableTodoContextsAdapter;
	/** Helper member to access TodoStatuses table. */
	private TableTodoStatusAdapter tableTodoStatusAdapter;
	
	/**
	 * @return the tableTodoStatusAdapter
	 */
	public TableTodoStatusAdapter getTableTodoStatusAdapter() {
		return tableTodoStatusAdapter;
	}

	/** Helper member to access Items to Contexts linking table. */
	private TableTodoItemsContextsAdapter tableTodoItemsContextsAdapter;

	/**
	 * @return the tableTodoContextsAdapter
	 */
	public TableTodoContextsAdapter getTableTodoContextsAdapter() {
		return tableTodoContextsAdapter;
	}

	/**
	 * @return the tableTodoItemsContextsAdapter
	 */
	public TableTodoItemsContextsAdapter getTableTodoItemsContextsAdapter() {
		return tableTodoItemsContextsAdapter;
	}

	/**
	 * @return the tableTodoItemsAdapter
	 */
	public TableTodoItemsAdapter getTableTodoItemsAdapter() {
		return tableTodoItemsAdapter;
	}

	/**
	 * @return the tableTodoListAdapter
	 */
	public ITableAdapter getTableTodoListAdapter() {
		return tableTodoListAdapter;
	}

	/**
	 * Constructor.
	 * */
	public DouiSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		tableTodoListAdapter = new TableTodoCategoriesAdapter(this);  
		tableTodoItemsAdapter = new TableTodoItemsAdapter(this);
		tableTodoContextsAdapter = new TableTodoContextsAdapter(this);
		tableTodoItemsContextsAdapter = new TableTodoItemsContextsAdapter(this);
		tableTodoStatusAdapter = new TableTodoStatusAdapter(this);
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
		tableTodoContextsAdapter.onCreate(database);
		tableTodoItemsAdapter.onCreate(database);
		tableTodoItemsContextsAdapter.onCreate(database);
		tableTodoStatusAdapter.onCreate(database);
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
		if(oldVersion == 0 && (newVersion == 1 || newVersion == 2))
		{
			onCreate(db);
		}
		if(oldVersion == 1 && newVersion == 2)
		{
			tableTodoStatusAdapter.onCreate(db);
			tableTodoItemsAdapter.upgrade(db, oldVersion, newVersion);
			/* TODO
			 * 1. Lists from db v1 are categories now.
			 * If todo has assigned list with the same name as existent category - set FK to that category.
			 * 2. isDone became FK to the status 
			 * */
		}
		
	}

}
