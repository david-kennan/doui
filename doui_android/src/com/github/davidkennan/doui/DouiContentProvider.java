/**
 * 
 */
package com.github.davidkennan.doui;

import com.github.davidkennan.doui.database.DouiSQLiteOpenHelper;
import com.github.davidkennan.doui.database.TableTodoListAdapter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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
	public static final Uri TODO_URI = Uri.parse("content://" + AUTHORITY + "/"
			+ TODO_PATH);
	/** Id for todo URI. */
	public static final int TODO_URI_ID = 50;

	/** Member responsible to determinate what kind of the URI passed. */
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, TODO_LISTS_PATH, TODO_LISTS_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_LISTS_PATH + "/#", TODO_LIST_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_CONTEXTS_PATH, TODO_CONTEXTS_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_CONTEXTS_PATH + "/#",
				TODO_CONTEXT_URI_ID);
		sURIMatcher.addURI(AUTHORITY, TODO_PATH + "/#", TODO_URI_ID);
	}

	private DouiSQLiteOpenHelper douiSQLiteOpenHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return null;
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
			result = douiSQLiteOpenHelper.getTableTodoListAdapter().query(
					projection, selection, selectionArgs, sortOrder);
			break;
		case TODO_LIST_URI_ID:
			String selectConditions = TableTodoListAdapter.TABLE_TODO_LISTS_ID
					+ "= ?";
			String selectConditionsArgs[] = { uri.getLastPathSegment()};
			result = douiSQLiteOpenHelper.getTableTodoListAdapter().query(
					projection, selectConditions, selectConditionsArgs, sortOrder);
			break;

		default:
			Log.e(this.getClass().getName(),
					"Unknown URI type passed to query(...): " + uriType);
			throw new IllegalArgumentException(
					"Unknown URI type passed to query(...): " + uriType);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
