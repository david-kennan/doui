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
	private View addCategoriesView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_categories_manager);
		addCategoriesView = (View) findViewById(R.id.iAddRowView);
		addCategoriesView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (douiTodoCategoryEditHelper != null) {
					douiTodoCategoryEditHelper.switchEditableRowToView();
				}
				douiTodoCategoryEditHelper = new DouiTodoCategoryEditHelper(v);
				douiTodoCategoryEditHelper.switchEditableRowToInsert();
				douiTodoCategoryEditHelper
						.setImbtSaveOnClickListener(new OnClickListener() {

							public void onClick(View v) {
								String newCategoryName = douiTodoCategoryEditHelper
										.getEtItemName().getText().toString();
								ContentValues values = new ContentValues();
								values.put(
										TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
										newCategoryName);
								saveItem(values);
							}
						});
			}
		});
		fillList();

	}

	/** Routines required to load list of categories. */
	private void fillList() {
		String[] from = new String[] {
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		int[] to = new int[] { R.id.tvItemName };

		String selectCondition = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME +"<>?";
		String[] selectConditionArgs ={"-None-"};
		ContentResolver cr = getContentResolver();
		cursorToDoCategories = cr
				.query(DouiContentProvider.TODO_CATEGORIES_URI, from, selectCondition,
						selectConditionArgs, null);
		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_category_editable_row, cursorToDoCategories,
				from, to, 0);
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
								TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID,
								itemId);
						values.put(
								TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
								newCategoryName);
						saveItem(values);
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
						cursor.close();
					}
				});

	}

	/**
	 * Save category item and switch editors to view mode.
	 * 
	 * @param values
	 *            values to be updated. If values contain
	 *            TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID then item
	 *            will be updated, otherwise inserted.
	 *            TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME checked
	 *            to be unique.
	 * */
	private void saveItem(ContentValues values) {
		String itemId = values
				.getAsString(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID);
		String newName = values
				.getAsString(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME);
		Uri selectUri = DouiContentProvider.TODO_CATEGORIES_URI;
		String selectCondition = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME
				+ "=?";
		String[] selectConditionArg = { newName };
		Cursor cursor = getContentResolver().query(selectUri, null,
				selectCondition, selectConditionArg, null);
		if (cursor.getCount() <= 0) {
			if (null == itemId || itemId.equals("")) {
				Uri insertUri = DouiContentProvider.TODO_CATEGORIES_URI;
				getContentResolver().insert(insertUri, values);
				douiTodoCategoryEditHelper.switchEditableRowToView();
				fillList();
			} else {
				Uri updateUri = Uri
						.parse(DouiContentProvider.TODO_CATEGORIES_URI
								.toString() + "/" + itemId);
				getContentResolver().update(updateUri, values, null, null);
				douiTodoCategoryEditHelper.switchEditableRowToView();
				fillList();
			}
		} else {
			Toast toast = Toast.makeText(getApplicationContext(), "Item "
					+ newName + " already exists.\nEnter another name.",
					Toast.LENGTH_LONG);
			toast.show();
		}
		cursor.close();
	}
}
