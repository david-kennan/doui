/**
 * 
 */
package com.github.davidkennan.doui.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoListAdapter;

/**
 * @author rsh
 * 
 */
public class DouiContentProviderTestCase extends
		ProviderTestCase2<DouiContentProvider> {

	public DouiContentProviderTestCase() {
		super(DouiContentProvider.class, DouiContentProvider.class.getName());
	}

	public void testToDoLists() {
		ContentProvider provider = getProvider();
		Uri uri = DouiContentProvider.TODO_LISTS_URI;
		try {
			Cursor cursor = provider.query(uri, null, null, null, null);
			assertNotNull(cursor);
			assertEquals(TableTodoListAdapter.STR_ARRAY_LISTS.length,
					cursor.getCount());
			cursor.close();
			for (int i = 0; i < TableTodoListAdapter.STR_ARRAY_LISTS.length; i++) {
				uri = Uri.parse("content://" + DouiContentProvider.AUTHORITY
						+ "/" + DouiContentProvider.TODO_LISTS_PATH + "/" + i);
				String columnNames[] = { TableTodoListAdapter.TABLE_TODO_LISTS_NAME };
				cursor = provider.query(uri, columnNames, null, null, null);
				assertEquals(1, cursor.getCount());
				cursor.moveToFirst();
				String listName = cursor.getString(0);
				assertEquals(TableTodoListAdapter.STR_ARRAY_LISTS[i], listName);
			}
		} catch (Exception e) {
			assertFalse(e.getClass().getName() + ": " + e.getMessage(), true);
		}
	}

	public void testCreateToDoItem() {
		String todoItemTitle = "todoItemTitle";
		String todoItemBody = "todoItemBody\n Multiline.";

		ContentProvider provider = getProvider();
		Uri uriToDoList = DouiContentProvider.TODO_LISTS_URI;
		String columnNames[] = { TableTodoListAdapter.TABLE_TODO_LISTS_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_LISTS_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST };
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		assertEquals(todoItemTitle, cursor.getString(0));
		assertEquals(todoItemBody, cursor.getString(1));
		assertEquals(listId.intValue(), cursor.getInt(2));
		cursor.close();
	}

	public void testCreateToDoItemWithContext() {
		String todoItemTitle = "todoItemTitle";
		String contextName = "@context";
		String todoItemBody = "todoItemBody." + contextName;

		ContentProvider provider = getProvider();
		// Obtain some list
		Uri uriToDoList = DouiContentProvider.TODO_LISTS_URI;
		String columnNames[] = { TableTodoListAdapter.TABLE_TODO_LISTS_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		// Create item
		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_LISTS_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST };
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
		// Access item from context name
		Uri uriContex = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_CONTEXTS_PATH+ "/" + contextName +"/");
		cursor = provider.query(uriContex, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
		// Delete item
		provider.delete(newItemUri, null, null);
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() == 0);
		cursor.close();
	}

	public void testDeleteToDoItem() {
		String todoItemTitle = "todoItemTitle";
		String todoItemBody = "todoItemBody\n Multiline.";

		ContentProvider provider = getProvider();
		Uri uriToDoList = DouiContentProvider.TODO_LISTS_URI;
		String columnNames[] = { TableTodoListAdapter.TABLE_TODO_LISTS_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_LISTS_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST };
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
		provider.delete(newItemUri, null, null);
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() == 0);
		cursor.close();
	}

	public void testUpdateToDoItem() {
		String todoItemTitle = "todoItemTitle";
		String todoItemBody = "todoItemBody\n Multiline.";
		String todoItemTitle2 = "todoItemTitle2";
		String todoItemBody2 = "todoItemBody2\n Multiline.";

		ContentProvider provider = getProvider();
		Uri uriToDoList = DouiContentProvider.TODO_LISTS_URI;
		String columnNames[] = { TableTodoListAdapter.TABLE_TODO_LISTS_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_LISTS_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST };

		values.clear();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle2);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody2);

		provider.update(newItemUri, values, null, null);
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		assertEquals(todoItemTitle2, cursor.getString(0));
		assertEquals(todoItemBody2, cursor.getString(1));
		assertEquals(listId.intValue(), cursor.getInt(2));
		cursor.close();
	}

}
