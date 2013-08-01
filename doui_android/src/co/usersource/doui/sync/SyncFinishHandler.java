package co.usersource.doui.sync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class SyncFinishHandler extends BroadcastReceiver {
	
	private Intent finishActivity;
	private Activity parentActivity;
	
	public SyncFinishHandler(Activity parent) {
		IntentFilter syncIntentFilter = new IntentFilter(SyncAdapter.ACTION_SYNC_FINISHED);
		parentActivity = parent;
		parentActivity.registerReceiver(this, syncIntentFilter);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		parentActivity.startActivity(finishActivity);
		parentActivity.unregisterReceiver(this);
	}
	
	public void  setFinishActivity(Intent activity) {
		finishActivity = activity;
	}

}
