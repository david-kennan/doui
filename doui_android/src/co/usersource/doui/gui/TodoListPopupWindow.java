package co.usersource.doui.gui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import co.usersource.doui.R;

/**
 * Popup window used to display list selection menu. May have a caption. Also
 * has lvToDoList property, which allow to set onClickListener for custom
 * handlers.
 * */
public class TodoListPopupWindow extends PopupWindow {
	private Context context;
	private SimpleCursorAdapter adapter;
	private Uri listUri;
	private ListView lvTodoLists;
	LinearLayout llMain;

	/**
	 * @return the lvTodoLists
	 */
	public ListView getLvTodoLists() {
		return lvTodoLists;
	}

	public TodoListPopupWindow(Context popupContext, Uri listUri,
			String caption, String[] projection, String condition,
			String[] conditionArgs) {
		super(new LinearLayout(popupContext));
		llMain = (LinearLayout)getContentView();
		this.context = popupContext;
		this.listUri = listUri;
				
		llMain.setOrientation(LinearLayout.VERTICAL);
		if (null != caption) {
			TextView tvCaption = new TextView(context);
			tvCaption.setText(caption);
			llMain.addView(tvCaption);
		}
		lvTodoLists = new ListView(context);
		llMain.addView(lvTodoLists);

		String[] from = projection;
		int[] to = new int[] { R.id.popupItemLabel };

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(this.listUri, projection, condition, conditionArgs,
				null);

		adapter = new SimpleCursorAdapter(context,
				R.layout.todo_list_popup_row, cursor, from, to);
		lvTodoLists.setAdapter(adapter);

		this.setContentView(llMain);

		setFocusable(true);
		setOutsideTouchable(true);
		// Removes default black background
		setBackgroundDrawable(new BitmapDrawable());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.PopupWindow#dismiss()
	 */
	@Override
	public void dismiss() {
		if (!adapter.getCursor().isClosed()) {
			adapter.getCursor().close();
		}
		super.dismiss();
	}
}
