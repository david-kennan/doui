/**
 * 
 */
package co.usersource.doui.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import co.usersource.doui.R;

/**
 * @author rsh
 * 
 */
public class DouiSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private EditTextPreference mSyncServerUrlPref;

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.todo_preferences);
		mSyncServerUrlPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(getString(R.string.prefSyncServerUrl_Key));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, DouiMainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		// Setup the initial values
		mSyncServerUrlPref.setSummary(getPreferenceScreen()
				.getSharedPreferences().getString(
						getString(R.string.prefSyncServerUrl_Key), ""));
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.prefSyncServerUrl_Key))) {
			mSyncServerUrlPref.setSummary(sharedPreferences.getString(
					getString(R.string.prefSyncServerUrl_Key), ""));
		}
	}

}
