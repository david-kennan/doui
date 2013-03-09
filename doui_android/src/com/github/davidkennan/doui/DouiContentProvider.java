/**
 * 
 */
package com.github.davidkennan.doui;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.util.Log;

import com.github.davidkennan.doui.database.DouiSQLiteOpenHelper;
import com.github.davidkennan.doui.database.adapters.TableTodoContextsAdapter;
import com.github.davidkennan.doui.database.adapters.TableTodoItemsAdapter;
import com.github.davidkennan.doui.database.adapters.TableTodoListAdapter;

/**
 * @author rsh
 * 
 */
public class DouiContentProvider extends ContentProvider {

	/** Root part of the content provider URI. */
	public static final String AUTHORITY = "com.github.davidkennan.doui.contentprovider";

	/** Suffix used to construct URI to acces todo-list's array. */
	public static final String TODO_LISTS_PATH = "todo_list";
	/** Full URI to acces todo-list's array. */
	public static final Uri TODO_LISTS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TODO_LISTS_PATH);
	/** Id for uri that match set of lists of TODOs. */
	public static final int TODO_LISTS_URI_ID = 10;
	/** Id for uri that match concrete todo list id from list. */
	public static final int TODO_LIST_URI_ID = 20;

	/** Suffix used to construct URI to access contexts list of todos. */
	public static final String TODO_CONTEXTS_PATH = "contexts";
	/** Full URI to access contexts list of todos. */
	public static final Uri TODO_CONTEXTS_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TODO_CONTEXTS_PATH);
	/** Id for URI match context list. */
	public static final int TODO_CONTEXTS_URI_ID = 30;
	/** Id for URI that match concrete context. */
	public static final int TODO_CONTEXT_URI_ID = 40;

	/** Suffix used to construct URI to access concrete todo item. */
	public static final String TODO_PATH = "todo";
	/** Full URI to access concrete todo item. */
	public static final Uri TODO_URI = Uri.parse(TODO_LISTS_URI.toString()
			+ "/#/" + TODO_PATH);
	/** Id for todo items list URI. */
	public static final int TODO_URI_ID = 50;
	/** Id for concrete todo item list URI. */
	public static final int TODO_ITEM_URI_ID = 60;

	/** Member responsible to determinate what kind of the URI passed. */
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, TODO_LISTS_PATH, TODO_LISTS_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_LISTS_PATH + "/#", TODO_LIST_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_CONTEXTS_PATH, TODO_CONTEXTS_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_CONTEXTS_PATH + "/*",
				TODO_CONTEXT_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_LISTS_PATH + "/#/" + TODO_PATH,
				TODO_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_LISTS_PATH + "/#/" + TODO_PATH
				+ "/#", TODO_ITEM_URI_ID);
	}

	private DouiSQLiteOpenHelper douiSQLiteOpenHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int result = 0;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

		case TODO_LISTS_URI_ID:
			result = douiSQLiteOpenHelper.getTableTodoListAdapter().delete(
					selection, selectionArgs);
			break;
		case TODO_URI_ID:
			result = douiSQLiteOpenHelper.getTableTodoItemsAdapter().delete(
					selection, selectionArgs);
			break;
		case TODO_ITEM_URI_ID:
			String itemSelection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
					+ "=?";
			String itemSelectionArgs[] = { uri.getLastPathSegment() };
			result = douiSQLiteOpenHelper.getTableTodoItemsAdapter().delete(
					itemSelection, itemSelectionArgs);
			break;
		default:
			Log.e(this.getClass().getName(),
					"Unknown URI type passed to query(...): " + uriType);
			throw new IllegalArgumentException(
					"Unknown URI type passed to query(...): " + uriType);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri result = null;
		long newItemId = -1;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case TODO_LISTS_URI_ID:
			newItemId = douiSQLiteOpenHelper.getTableTodoListAdapter().insert(
					values);
			if (newItemId > -1) {
				result = Uri.parse(TODO_LISTS_URI.toString() + newItemId);
			}
			break;
		case TODO_LIST_URI_ID:
			Log.e(this.getClass().getName(),
					"Attempt to insert new list from list URI:  " + uri);
			break;
		case TODO_URI_ID:
			newItemId = douiSQLiteOpenHelper.getTableTodoItemsAdapter().insert(
					values);
			if (newItemId > -1) {
				result = Uri.parse(uri.toString() + "/" + newItemId);
			}
			break;
		case TODO_ITEM_URI_ID:
			Log.e(this.getClass().getName(),
					"Attempt to insert new todo item from todo item URI:  "
							+ uri);
			break;
		default:
			Log.e(this.getClass().getName(),
					"Unknown URI type passed to query(...): " + uriType);
			throw new IllegalArgumentException(
					"Unknown URI type passed to query(...): " + uriType);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		douiSQLiteOpenHelper = new DouiSQLiteOpenHelper(getContext());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case TODO_LISTS_URI_ID:
			Cursor todoLists = douiSQLiteOpenHelper.getTableTodoListAdapter().query(
					projection, selection, selectionArgs, sortOrder);
			String contextProjection[]={
					"-1 "+TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID,
					TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_NAME
			};
			Cursor contexts =douiSQLiteOpenHelper.getTableTodoContextsAdapter().query(contextProjection, null, null, null);
			Cursor cursors[] = {todoLists, contexts};
			result = new MergeCursor(cursors);
			break;
		case TODO_LIST_URI_ID: {
			String selectConditions = TableTodoListAdapter.TABLE_TODO_LISTS_ID
					+ "= ?";
			String selectConditionsArgs[] = { uri.getLastPathSegment() };
			result = douiSQLiteOpenHelper.getTableTodoListAdapter().query(
					projection, selectConditions, selectConditionsArgs,
					sortOrder);
		}
			break;

		case TODO_URI_ID: {
			List<String> uriSegments = uri.getPathSegments();
			String listId = uriSegments.get((uriSegments.size() - 1) - 1);
			String selectConditions = TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST
					+ "= ? and "
					+ TableTodoItemsAdapter.TABLE_TODO_ITEMS_IS_DONE + " <> 1";
			String selectConditionsArgs[] = { listId };
			result = douiSQLiteOpenHelper.getTableTodoItemsAdapter().query(
					projection, selectConditions, selectConditionsArgs,
					sortOrder);
		}
			break;

		case TODO_ITEM_URI_ID: {
			String selectConditions = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
					+ "= ?";
			String selectConditionsArgs[] = { uri.getLastPathSegment() };
			result = douiSQLiteOpenHelper.getTableTodoItemsAdapter().query(
					projection, selectConditions, selectConditionsArgs,
					sortOrder);
		}
			break;

		case TODO_CONTEXT_URI_ID: {
			String contextName = uri.getLastPathSegment();
			result = douiSQLiteOpenHelper.getTableTodoContextsAdapter().queryContextItems(contextName);
		}
			break;
		default:
			Log.e(this.getClass().getName(),
					"Unknown URI type passed to query(...): " + uriType);
			throw new IllegalArgumentException(
					"Unknown URI type passed to query(...): " + uriType);
		}
		if (null != result) {
			result.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return result;
	}

	/**
	 * Updates values in the database. Uri must be in list context. Selection
	 * should be used to set concrete values.
	 * */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int result = 0;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

		case TODO_LISTS_URI_ID:
			douiSQLiteOpenHelper.getTableTodoListAdapter().update(values,
					selection, selectionArgs);
			break;
		case TODO_ITEM_URI_ID: {
			String selectConditions = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
					+ "= ?";
			String selectConditionsArgs[] = { uri.getLastPathSegment() };
			douiSQLiteOpenHelper.getTableTodoItemsAdapter().update(values,
					selectConditions, selectConditionsArgs);
		}
			break;
		case TODO_URI_ID:
			douiSQLiteOpenHelper.getTableTodoItemsAdapter().update(values,
					selection, selectionArgs);
			break;
		default:
			Log.e(this.getClass().getName(),
					"Unknown URI type passed to query(...): " + uriType);
			throw new IllegalArgumentException(
					"Unknown URI type passed to query(...): " + uriType);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

}
