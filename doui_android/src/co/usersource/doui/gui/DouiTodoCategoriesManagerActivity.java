/**
 * 
 */
package co.usersource.doui.gui;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoCategoriesManagerActivity extends ListActivity {
	private Cursor cursorToDoCategories;
	private SimpleCursorAdapter adapter;
	private DouiTodoCategoryEditHelper douiTodoCategoryEditHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_categories_manager);
		fillList();

	}

	/** Routines required to load list of categories. */
	private void fillList() {
		String[] from = new String[] {
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		int[] to = new int[] { R.id.tvItemName };

		ContentResolver cr = getContentResolver();
		cursorToDoCategories = cr
				.query(DouiContentProvider.TODO_CATEGORIES_URI, from, null,
						null, null);
		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_category_editable_row, cursorToDoCategories,
				from, to);
		setListAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		if (!adapter.getCursor().isClosed()) {
			adapter.getCursor().close();
		}
		super.onDestroy();
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		final long itemId = id;
		if (douiTodoCategoryEditHelper != null) {
			douiTodoCategoryEditHelper.switchEditableRowToView();
		}
		douiTodoCategoryEditHelper = new DouiTodoCategoryEditHelper(v);
		douiTodoCategoryEditHelper.switchEditableRowToEdit();
		douiTodoCategoryEditHelper
				.setImbtSaveOnClickListener(new OnClickListener() {

					public void onClick(View v) {

						String newCategoryName = douiTodoCategoryEditHelper
								.getEtItemName().getText().toString();
						ContentValues values = new ContentValues();
						values.put(
								TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
								newCategoryName);
						Uri updateUri = Uri
								.parse(DouiContentProvider.TODO_CATEGORIES_URI
										.toString() + "/" + itemId);
						getContentResolver().update(updateUri, values, null,
								null);
						douiTodoCategoryEditHelper.switchEditableRowToView();
						fillList();
					}
				});
		douiTodoCategoryEditHelper
				.setImbtDeleteOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Uri uriTodoItemsInCategory = Uri
								.parse(DouiContentProvider.TODO_CATEGORIES_URI
										.toString()
										+ "/"
										+ itemId
										+ "/"
										+ DouiContentProvider.TODO_PATH);
						Cursor cursor = getContentResolver().query(
								uriTodoItemsInCategory, null, null, null, null);
						if (cursor.getCount() <= 0) {
							Uri updateUri = Uri
									.parse(DouiContentProvider.TODO_CATEGORIES_URI
											.toString() + "/" + itemId);
							getContentResolver().delete(updateUri, null, null);
							fillList();
						} else {
							Toast toast = Toast
									.makeText(
											getApplicationContext(),
											"Unable to delete: Category contain items.",
											Toast.LENGTH_LONG);
							toast.show();
						}
					}
				});

	}

}
