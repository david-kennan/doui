/**
 * 
 */
package co.usersource.doui.gui;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import co.usersource.doui.R;

/**
 * @author rsh
 *
 */
public class DouiTodoCategoryEditHelper {

	private View editableRow;
	private EditText etItemName;
	/**
	 * @return the etItemName
	 */
	public EditText getEtItemName() {
		return etItemName;
	}

	private TextView tvItemName;
	private ImageButton imbtSave;

	private OnClickListener imbtSaveOnClickListener;
	private OnClickListener imbtDeleteOnClickListener;
	private ImageButton imbtDelete;
	
	public DouiTodoCategoryEditHelper(View editableRow){
		this.editableRow = editableRow;
		tvItemName = (TextView)editableRow.findViewById(R.id.tvItemName);
		etItemName = (EditText)editableRow.findViewById(R.id.etItemName);
		imbtSave = (ImageButton)editableRow.findViewById(R.id.imbtSave);		
//		imbtCancel = (ImageButton)editableRow.findViewById(R.id.imbtCancel);
		imbtDelete = (ImageButton)editableRow.findViewById(R.id.imbtDelete);
	}
	
	public void switchEditableRowToEdit()
	{
		tvItemName.setVisibility(View.GONE);
		etItemName.setVisibility(View.VISIBLE);
		etItemName.setText(tvItemName.getText());
		imbtSave.setVisibility(View.VISIBLE);

		//imbtCancel.setVisibility(View.VISIBLE);
		imbtDelete.setVisibility(View.VISIBLE);
	}
	
	public void switchEditableRowToView()
	{
		tvItemName.setVisibility(View.VISIBLE);
		etItemName.setVisibility(View.GONE);
		imbtSave.setVisibility(View.GONE);
		//imbtCancel.setVisibility(View.GONE);
		imbtDelete.setVisibility(View.GONE);
	}

	public OnClickListener getImbtSaveOnClickListener() {
		return imbtSaveOnClickListener;
	}

	public void setImbtSaveOnClickListener(OnClickListener imbtSaveOnClickListener) {
		this.imbtSaveOnClickListener = imbtSaveOnClickListener;
		imbtSave.setOnClickListener(imbtSaveOnClickListener);
	}

	public OnClickListener getImbtDeleteOnClickListener() {
		return imbtDeleteOnClickListener;
	}

	public void setImbtDeleteOnClickListener(OnClickListener imbtDeleteOnClickListener) {
		this.imbtDeleteOnClickListener = imbtDeleteOnClickListener;
		imbtDelete.setOnClickListener(imbtDeleteOnClickListener);
	}
}
