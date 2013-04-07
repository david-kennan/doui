/**
 * 
 */
package co.usersource.doui.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

/**
 * @author rsh
 * 
 */
public class DouiContentProviderTestCase extends
		ProviderTestCase2<DouiContentProvider> {

	public DouiContentProviderTestCase() {
		super(DouiContentProvider.class, DouiContentProvider.class.getName());
	}

	/**
	 * TC to check whether we have static set of the lists.
	 * */
	public void testToDoLists() {
		ContentProvider provider = getProvider();
		Uri uri = DouiContentProvider.TODO_CATEGORIES_URI;
		try {
			Cursor cursor = provider.query(uri, null, null, null, null);
			assertNotNull(cursor);
			assertEquals(TableTodoCategoriesAdapter.STR_ARRAY_CATEGORIES.length,
					cursor.getCount());
			cursor.close();
			for (int i = 0; i < TableTodoCategoriesAdapter.STR_ARRAY_CATEGORIES.length; i++) {
				uri = Uri.parse("content://" + DouiContentProvider.AUTHORITY
						+ "/" + DouiContentProvider.TODO_CATEGORIES_PATH + "/" + i);
				String columnNames[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME };
				cursor = provider.query(uri, columnNames, null, null, null);
				assertEquals(1, cursor.getCount());
				cursor.moveToFirst();
				String listName = cursor.getString(0);
				assertEquals(TableTodoCategoriesAdapter.STR_ARRAY_CATEGORIES[i], listName);
			}
		} catch (Exception e) {
			assertFalse(e.getClass().getName() + ": " + e.getMessage(), true);
		}
	}

	/**
	 * TC to check whether ToDo item could be created.
	 * */
	public void testCreateToDoItem() {
		String todoItemTitle = "todoItemTitle";
		String todoItemBody = "todoItemBody\n Multiline.";

		ContentProvider provider = getProvider();
		Uri uriToDoList = DouiContentProvider.TODO_CATEGORIES_URI;
		String columnNames[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_CATEGORIES_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY };
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		assertEquals(todoItemTitle, cursor.getString(0));
		assertEquals(todoItemBody, cursor.getString(1));
		assertEquals(listId.intValue(), cursor.getInt(2));
		cursor.close();
	}

	/**
	 * This TC check whether item with context tag can produce corresponding
	 * context record.
	 * */
	public void testCreateToDoItemWithContext() {
		String todoItemTitle = "todoItemTitle";
		String contextName = "@context";
		String todoItemBody = "todoItemBody." + contextName;

		ContentProvider provider = getProvider();
		// Obtain some list
		Uri uriToDoList = DouiContentProvider.TODO_CATEGORIES_URI;
		String columnNames[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
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
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_CATEGORIES_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY };
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
		// Access item from context name
		Uri uriContex = Uri.parse("content://" + DouiContentProvider.AUTHORITY
				+ "/" + DouiContentProvider.TODO_CONTEXTS_PATH + "/"
				+ contextName + "/");
		cursor = provider.query(uriContex, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
		// Delete item
		provider.delete(newItemUri, null, null);
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() == 0);
		cursor.close();
	}

	/**
	 * This TC intended to check whether we still can delete ToDo item from
	 * database.
	 * */
	public void testDeleteToDoItem() {
		String todoItemTitle = "todoItemTitle";
		String todoItemBody = "todoItemBody\n Multiline.";

		ContentProvider provider = getProvider();
		Uri uriToDoList = DouiContentProvider.TODO_CATEGORIES_URI;
		String columnNames[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_CATEGORIES_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY };
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
		provider.delete(newItemUri, null, null);
		cursor = provider.query(newItemUri, newItemFields, null, null, null);
		assertTrue(cursor.getCount() == 0);
		cursor.close();
	}

	/**
	 * This TC check whether we can update ToDo item properties.
	 * */
	public void testUpdateToDoItem() {
		String todoItemTitle = "todoItemTitle";
		String todoItemBody = "todoItemBody\n Multiline.";
		String todoItemTitle2 = "todoItemTitle2";
		String todoItemBody2 = "todoItemBody2\n Multiline.";

		ContentProvider provider = getProvider();
		Uri uriToDoList = DouiContentProvider.TODO_CATEGORIES_URI;
		String columnNames[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		Cursor cursor = provider.query(uriToDoList, columnNames, null, null,
				null);
		assertTrue(cursor.getCount() > 0);
		cursor.moveToFirst();
		Integer listId = cursor.getInt(0);
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, todoItemTitle);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, todoItemBody);
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, listId);
		Uri uriToDoItems = Uri.parse("content://"
				+ DouiContentProvider.AUTHORITY + "/"
				+ DouiContentProvider.TODO_CATEGORIES_PATH + "/" + listId + "/"
				+ DouiContentProvider.TODO_PATH);
		Uri newItemUri = provider.insert(uriToDoItems, values);
		assertNotNull("New item URI is null.", newItemUri);
		String newItemFields[] = {
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY };

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

	/**
	 * This TC intended to check that after DB created we have all required statuses.
	 * */
	public void testStatuses()
	{
		ContentProvider provider = getProvider();
		Uri uri = DouiContentProvider.TODO_STATUSES_URI;
		try {
			Cursor cursor = provider.query(uri, null, null, null, null);
			assertNotNull(cursor);
			assertEquals(TableTodoStatusAdapter.STR_ARRAY_STATUSES.length,
					cursor.getCount());
			cursor.close();
			for (int i = 0; i < TableTodoStatusAdapter.STR_ARRAY_STATUSES.length; i++) {
				uri = Uri.parse("content://" + DouiContentProvider.AUTHORITY
						+ "/" + DouiContentProvider.TODO_STATUSES_PATH + "/" + i);
				String columnNames[] = { TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME };
				cursor = provider.query(uri, columnNames, null, null, null);
				assertEquals(1, cursor.getCount());
				cursor.moveToFirst();
				String statusName = cursor.getString(0);
				assertEquals(TableTodoStatusAdapter.STR_ARRAY_STATUSES[i], statusName);
			}
		} catch (Exception e) {
			assertFalse(e.getClass().getName() + ": " + e.getMessage(), true);
		}
	}
}
