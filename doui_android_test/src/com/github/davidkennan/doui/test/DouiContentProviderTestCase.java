/**
 * 
 */
package com.github.davidkennan.doui.test;

import android.content.ContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.github.davidkennan.doui.DouiContentProvider;
import com.github.davidkennan.doui.database.TableTodoListAdapter;

/**
 * @author rsh
 * 
 */
public class DouiContentProviderTestCase extends
		ProviderTestCase2<DouiContentProvider> {
	public DouiContentProviderTestCase() {
		super(DouiContentProvider.class, DouiContentProvider.class.getName());
	}

	public void testQuery() {
		ContentProvider provider = getProvider();

		Uri uri = DouiContentProvider.TODO_LISTS_URI;

		Cursor cursor = provider.query(uri, null, null, null, null);

		assertNotNull(cursor);

		cursor = null;
		try {
			cursor = provider.query(Uri.parse("definitelywrong"), null, null,
					null, null);
			// we're wrong if we get until here!
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
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
			assertFalse(e.getClass().getName()+": "+e.getMessage(), true);
		}

	}
}
