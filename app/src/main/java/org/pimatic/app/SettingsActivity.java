package org.pimatic.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	public static String PREFERENCE_FILENAME = "pimatic";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		final Spinner protocol = (Spinner) findViewById(R.id.protocol);
		final EditText host = (EditText) findViewById(R.id.host);
		final EditText port = (EditText) findViewById(R.id.port);
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
		final CheckBox delTrusted = (CheckBox) findViewById(R.id.deleteCert);

		
		
		final SharedPreferences settings = getSharedPreferences(
				PREFERENCE_FILENAME, 0);

		Button button = (Button) findViewById(R.id.save);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("protocol", protocol.getSelectedItem().toString());
				editor.putString("host", host.getText().toString());
				editor.putInt("port", Integer.parseInt(port.getText().toString()));
				editor.putString("username", username.getText().toString());
				editor.putString("password", password.getText().toString());
				if (delTrusted.isChecked()) {
					editor.remove("trustedCert");
				}
				editor.commit();
				restart();
//				Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
//				startActivity(intent);
			}
		});

		protocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.v("protocol", protocol.getSelectedItem().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		int protocolPos = (settings.getString("protocol", getResources().getString(R.string.default_protocol)).equals("http") ? 0 : 1);
		protocol.setSelection(protocolPos);
		host.setText(settings.getString("host", getResources().getString(R.string.default_host)));
		port.setText("" + settings.getInt("port", getResources().getInteger(R.integer.default_port)));
		username.setText(settings.getString("username", getResources().getString(R.string.default_username)));
		password.setText(settings.getString("password", getResources().getString(R.string.default_password)));
		if(settings.getString("trustedCert", "").isEmpty()) {
			delTrusted.setEnabled(false);
		}
	}

	public void restart() {
		Context context = getApplicationContext();
		Toast.makeText(context, "Restarting app", Toast.LENGTH_LONG).show();
		Intent mStartActivity = new Intent(context, MainActivity.class);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		finish();
	}

}
