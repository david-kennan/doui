/**
 * 
 */
package co.usersource.doui.database.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import co.usersource.doui.database.DouiSQLiteOpenHelper;

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
	/** Table where todo items stored. Foreign key to current item status. */
	public static final String TABLE_TODO_ITEMS_FK_STATUS = "fk_status";
	/** Table where todo items stored. Reference to the primary list item. */
	public static final String TABLE_TODO_ITEMS_FK_LIST = "fk_list";
	/** Table where todo items stored. Create statement. */
	public static final String STR_CREATE_TABLE_TODO_ITEMS = "create table "
			+ TABLE_TODO_ITEMS + "(" + TABLE_TODO_ITEMS_ID
			+ " integer primary key autoincrement, " + TABLE_TODO_ITEMS_TITLE
			+ " TEXT, " + TABLE_TODO_ITEMS_BODY + " TEXT, "
			+ TABLE_TODO_ITEMS_FK_STATUS + " INTEGER DEFAULT NULL, "
			+ TABLE_TODO_ITEMS_FK_LIST + " INTEGER, " + "FOREIGN KEY("
			+ TABLE_TODO_ITEMS_FK_STATUS + ") REFERENCES "
			+ TableTodoStatusAdapter.TABLE_TODO_STATUSES + "("
			+ TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID + "),"
			+ "FOREIGN KEY(" + TABLE_TODO_ITEMS_FK_LIST + ") REFERENCES "
			+ TableTodoListAdapter.TABLE_TODO_LISTS + "("
			+ TableTodoListAdapter.TABLE_TODO_LISTS_ID + ")" + ");";

	private DouiSQLiteOpenHelper sqliteOpenHelper;

	public TableTodoItemsAdapter(DouiSQLiteOpenHelper sqliteOpenHelper) {
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
			ContentValues todoItemData = new ContentValues(values);
			todoItemData.put(TABLE_TODO_ITEMS_ID, result);
			this.updateContexts(todoItemData);
		}
		return result;
	}

	// TODO Not implemented yet. Update for contexts required.
	public int delete(String arg1, String[] arg2) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.delete(TABLE_TODO_ITEMS, arg1, arg2);
		return result;
	}

	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase database = this.sqliteOpenHelper.getWritableDatabase();
		int result = database.update(TABLE_TODO_ITEMS, values, selection,
				selectionArgs);
		if (result > 0) {
			String columns[] = { TABLE_TODO_ITEMS_ID, TABLE_TODO_ITEMS_BODY,
					TABLE_TODO_ITEMS_FK_STATUS };
			Cursor cursor = database.query(TABLE_TODO_ITEMS, columns,
					selection, selectionArgs, null, null, null);
			while (cursor.moveToNext()) {
				ContentValues todoItemValues = new ContentValues();
				todoItemValues.put(TABLE_TODO_ITEMS_ID, cursor.getString(0));
				todoItemValues.put(TABLE_TODO_ITEMS_BODY, cursor.getString(1));
				todoItemValues.put(TABLE_TODO_ITEMS_FK_STATUS,
						cursor.getString(2));
				updateContexts(todoItemValues);
			}
			cursor.close();
		}
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

	/**
	 * Scan item body for contexts.
	 * 
	 * @param values
	 *            item data.
	 * @return List of parsed contexts.
	 * */
	private List<String> getItemContextsFromBody(ContentValues values) {
		List<String> result = new ArrayList<String>();
		String itemBody = values.getAsString(TABLE_TODO_ITEMS_BODY);
		Pattern contextPattern = Pattern.compile("@(\\w*)");
		Matcher contextMatcher = contextPattern.matcher(itemBody);
		while (contextMatcher.find()) {
			result.add(contextMatcher.group(0));
		}
		return result;
	}

	/** Method to update context tables according to item values. */
	private void updateContexts(ContentValues todoItemData) {

		this.clearItemContextLinks(todoItemData);

		List<String> itemContexts = this.getItemContextsFromBody(todoItemData);
		for (String contextName : itemContexts) {
			ContentValues contextData = sqliteOpenHelper
					.getTableTodoContextsAdapter()
					.getContextByName(contextName);
			if (contextData == null) {
				contextData = new ContentValues();
				contextData.put(
						TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_NAME,
						contextName);
				long id = sqliteOpenHelper.getTableTodoContextsAdapter()
						.insert(contextData);
				contextData.put(
						TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID, id);
			}

			if (contextData
					.getAsInteger(TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID) > -1) {
				String[] columns = {
						TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_ID,
						TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS,
						TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS };
				String selection = TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS
						+ "=? and "
						+ TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS
						+ "=?";
				String selectionArgs[] = {
						contextData
								.getAsString(TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID),
						todoItemData
								.getAsString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID) };
				Cursor cursor = sqliteOpenHelper
						.getTableTodoItemsContextsAdapter().query(columns,
								selection, selectionArgs, null);
				if (cursor.getCount() == 0) {
					ContentValues valuesTodoContex = new ContentValues();
					valuesTodoContex
							.put(TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS,
									todoItemData
											.getAsString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID));
					valuesTodoContex
							.put(TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_CONTEXTS,
									contextData
											.getAsString(TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID));
					sqliteOpenHelper.getTableTodoItemsContextsAdapter().insert(
							valuesTodoContex);
				}
				cursor.close();
			} else {
				Log.e(this.getClass().getName(),
						"Contex Id contains negative value");
			}
		}

		sqliteOpenHelper.getTableTodoContextsAdapter().removeEmptyContexts();
	}

	/** This method removes any links between current item and contexts. */
	private void clearItemContextLinks(ContentValues todoItemData) {
		String condition = TableTodoItemsContextsAdapter.TABLE_TODO_ITEMS_CONTEXTS_FK_TODO_ITEMS
				+ "=?";
		String conditionArgs[] = { todoItemData
				.getAsString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID) };
		sqliteOpenHelper.getTableTodoItemsContextsAdapter().delete(condition,
				conditionArgs);
	}

}
