/**
 * 
 */
package co.usersource.doui.gui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoItemViewActivity extends Activity {

	private Uri itemUri;
	private String itemId;
	private String itemListId;
	private String itemBody;
	private String itemTitle;
	private TextView tvTodoItemTitle;
	private TextView tvTodoItemBody;
	private String itemListName;
	private TextView tvTodoListName;
	private ImageButton imbtEdit;
	private ImageButton imbtSetList;
	private LinearLayout llMain;
	private ImageButton imbtSetDone;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_item_view);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			itemUri = extras.getParcelable(DouiContentProvider.TODO_CATEGORIES_PATH);
		}
		this.refreshTodoItemData();
		imbtEdit = (ImageButton) findViewById(R.id.imbtEdit);
		final DouiTodoItemViewActivity self = this;
		imbtEdit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(self, DouiTodoItemEditActivity.class);
				i.putExtra(DouiContentProvider.TODO_CATEGORIES_PATH, itemUri);
				startActivity(i);
			}
		});
	}

	private void refreshTodoItemData() {
		String[] projection = { TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST };
		Cursor cursor = getContentResolver().query(itemUri, projection, null,
				null, null);
		cursor.moveToFirst();
		itemId = cursor.getString(0);
		itemTitle = cursor.getString(1);
		itemBody = cursor.getString(2);
		itemListId = cursor.getString(3);
		cursor.close();
		// TODO not best solution. Think about JOIN.
		Uri uriList = Uri.parse("content://" + DouiContentProvider.AUTHORITY
				+ "/" + DouiContentProvider.TODO_CATEGORIES_PATH + "/" + itemListId);
		String listProperties[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME };
		cursor = getContentResolver().query(uriList, listProperties, null,
				null, null);
		cursor.moveToFirst();
		llMain = (LinearLayout)findViewById(R.id.llMain);
		itemListName = cursor.getString(0);
		tvTodoItemTitle = (TextView) findViewById(R.id.tvTodoTitle);
		tvTodoItemTitle.setText(itemTitle);
		tvTodoItemBody = (TextView) findViewById(R.id.tvTodoBody);
		tvTodoItemBody.setText(itemBody);
		tvTodoListName = (TextView) findViewById(R.id.tvListName);
		tvTodoListName.setText(itemListName);
		imbtSetList = (ImageButton) findViewById(R.id.imbtSetList);
		imbtSetList.setOnClickListener(new OnClickListener() {
			TodoListsPopupWindow popup;

			public void onClick(View v) {
				
				if (popup==null || !popup.isShowing()) {
					if(popup != null)
					{
						popup.dismiss();
					}
					popup = new TodoListsPopupWindow(
							getApplicationContext(), itemUri);
					popup.showAtLocation(llMain, Gravity.RIGHT|Gravity.TOP, 0, 0);
					int location[] = { 0, 0 };
					imbtSetList.getLocationOnScreen(location);
					popup.update(0, location[1] - 200, 300, 200);
				} else {
					popup.dismiss();
				}
			}
		});
		
		// TODO this code is deprecated, must be replaced with some kind of the code to set item status.  
		/*imbtSetDone = (ImageButton) findViewById(R.id.imbtDone);
		imbtSetDone.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ContentValues values = new ContentValues();
				values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_IS_DONE, 1);

				String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
						+ "=?";
				String selectionArgs[] = { itemUri.getLastPathSegment() };
				getApplicationContext().getContentResolver().update(itemUri, values, selection,
						selectionArgs);
				Toast toast = Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT);
				toast.show();
				finish();
			}
			
		});*/
	}

	@Override
	protected void onRestart() {
		this.refreshTodoItemData();
		super.onRestart();
	}

}
