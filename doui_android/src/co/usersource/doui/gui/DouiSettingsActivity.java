/**
 * 
 */
package co.usersource.doui.gui;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import co.usersource.doui.R;

/**
 * @author rsh
 * 
 */
public class DouiSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private EditTextPreference mSyncServerUrlPref;
	private EditTextPreference mSyncRepeatTimePref;
	private ListPreference mSyncAccountPref;

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.todo_preferences);
		mSyncServerUrlPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(getString(R.string.prefSyncServerUrl_Key));
		mSyncRepeatTimePref = (EditTextPreference) getPreferenceScreen()
				.findPreference(getString(R.string.prefSyncRepeatTime_Key));
		mSyncAccountPref = (ListPreference) getPreferenceScreen()
				.findPreference(getString(R.string.prefSyncAccount_Key));
		loadDeviceAccounts();
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
		mSyncRepeatTimePref.setSummary(getPreferenceScreen()
				.getSharedPreferences().getString(
						getString(R.string.prefSyncRepeatTime_Key), ""));
		mSyncAccountPref.setSummary(getPreferenceScreen()
				.getSharedPreferences().getString(
				getString(R.string.prefSyncAccount_Key), ""));
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
		} else if (key.equals(getString(R.string.prefSyncRepeatTime_Key))) {
			mSyncRepeatTimePref.setSummary(sharedPreferences.getString(
					getString(R.string.prefSyncRepeatTime_Key), ""));
		}else if (key.equals(getString(R.string.prefIsSyncable_Key))) {
			loadDeviceAccounts();
		}else if (key.equals(getString(R.string.prefSyncAccount_Key))) {
			mSyncAccountPref.setSummary(sharedPreferences.getString(
					getString(R.string.prefSyncAccount_Key), ""));
		}
	}

	/**
	 * Routine to obtain device account list and fill the corresponding setting.
	 */
	private void loadDeviceAccounts() {
		Account[] accounts = AccountManager.get(getApplicationContext())
				.getAccounts();
		List<String> listAccountTitles = new ArrayList<String>();
		for (Account account : accounts) {
			String accountTitle = account.name + "(" + account.type + ")";
			listAccountTitles.add(accountTitle);
		}
		mSyncAccountPref.setEntries(listAccountTitles.toArray(new String[0]));
		mSyncAccountPref.setEntryValues(listAccountTitles.toArray(new String[0]));
	}
}
