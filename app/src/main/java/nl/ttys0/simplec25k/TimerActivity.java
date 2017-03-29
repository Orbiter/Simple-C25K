/*
 *  TimerActivity.java
 * 
 *  Copyright 2012 Roel Blaauwgeers <roel@ttys0.nl>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  info:
 *  Class to control the ProgramService from a GUI.
 */

package nl.ttys0.simplec25k;

import java.util.HashMap;
import nl.ttys0.simplec25k.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TimerActivity extends Activity {

	protected static final String MY_ACTION = "MY_ACTION";

	public static CountdownChronometer countdown;
	public static CountdownChronometer totalCountdown;
	public static Context context;

	private TextView mainTitle;
	private TextView current;
	private TextView description;
	private MyReceiver myReceiver;
	private HashMap<String, String> hashMap = new HashMap<String, String>();
	private SharedPreferences prefs;
	
	private Button startButton;
	private Button pauseButton;
	private Button skipButton;

	private Boolean paused = false;
	private Boolean started = false;

	// selected from main
	public static String selectedProgram;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.timer);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// setup descriptions, there is probably an easier way that I don't know
		// of:)
		Resources res = getResources();
		hashMap.put("w1d1", res.getString(R.string.w1));
		hashMap.put("w1d2", res.getString(R.string.w1));
		hashMap.put("w1d3", res.getString(R.string.w1));

		hashMap.put("w2d1", res.getString(R.string.w2));
		hashMap.put("w2d2", res.getString(R.string.w2));
		hashMap.put("w2d3", res.getString(R.string.w2));

		hashMap.put("w3d1", res.getString(R.string.w3));
		hashMap.put("w3d2", res.getString(R.string.w3));
		hashMap.put("w3d3", res.getString(R.string.w3));

		hashMap.put("w4d1", res.getString(R.string.w4));
		hashMap.put("w4d2", res.getString(R.string.w4));
		hashMap.put("w4d3", res.getString(R.string.w4));

		hashMap.put("w5d1", res.getString(R.string.w5d1));
		hashMap.put("w5d2", res.getString(R.string.w5d2));
		hashMap.put("w5d3", res.getString(R.string.w5d3));

		hashMap.put("w6d1", res.getString(R.string.w6d1));
		hashMap.put("w6d2", res.getString(R.string.w6d2));
		hashMap.put("w6d3", res.getString(R.string.w6d3));

		hashMap.put("w7d1", res.getString(R.string.w7));
		hashMap.put("w7d2", res.getString(R.string.w7));
		hashMap.put("w7d3", res.getString(R.string.w7));

		hashMap.put("w8d1", res.getString(R.string.w8));
		hashMap.put("w8d2", res.getString(R.string.w8));
		hashMap.put("w8d3", res.getString(R.string.w8));

		hashMap.put("w9d1", res.getString(R.string.w9));
		hashMap.put("w9d2", res.getString(R.string.w9));
		hashMap.put("w9d3", res.getString(R.string.w9));

		TimerActivity.context = this;

		// Register BroadcastReceiver
		// to receive event from our service
		myReceiver = new MyReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ProgramService.MY_ACTION);
		registerReceiver(myReceiver, intentFilter);

		// get the selected program. It shouldn't ever be null, but just in case, we start a new main
		// activity and kill the current timerActivity.
        selectedProgram = getIntent().getStringExtra("selectedProgram");
		if (selectedProgram == null) {
			// restart app
			Intent i = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(
							getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		} else {

			countdown = (CountdownChronometer) findViewById(R.id.chronometer1);
			// countdown.setBase(System.currentTimeMillis() + 30000);

			mainTitle = (TextView) findViewById(R.id.name);
			mainTitle.setText(selectedProgram.replace("w", "Week ").replace(
					"d", ", Day "));

			startButton = (Button) findViewById(R.id.startButton);
			pauseButton = (Button) findViewById(R.id.pauseButton);
			skipButton = (Button) findViewById(R.id.skipButton);

			// Watch for button clicks.
			// button = (Button) findViewById(R.id.startButton);
			startButton.setOnClickListener(mStartListener);

			// button = (Button) findViewById(R.id.pauseButton);
			pauseButton.setOnClickListener(mPauseListener);
			pauseButton.setClickable(false);

			// button = (Button) findViewById(R.id.skipButton);
			skipButton.setOnClickListener(mSkipListener);
			skipButton.setClickable(false);

			// set text fields
			description = (TextView) findViewById(R.id.textView1);
			description.setText(hashMap.get(selectedProgram));

			current = (TextView) findViewById(R.id.textView2);
			current.setText("Press Start to begin.");

			countdown = (CountdownChronometer) findViewById(R.id.chronometer1);
			totalCountdown = (CountdownChronometer) findViewById(R.id.chronometer2);

		}

	}

	private void sendIntent(String intent) {
		Intent i = new Intent();
		i.setAction(MY_ACTION);
		i.putExtra("DATA_TO_PS", intent);
		sendBroadcast(i);
	}

	void stop() {
		if (prefs.getBoolean("show_alerts", true)) {
			new AlertDialog.Builder(this)
				.setMessage("Are you sure you want to exit?")
				.setCancelable(false)
				.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							sendIntent("STOP");
							finish();
						}
					})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.show();
		} else {
			sendIntent("STOP");
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(myReceiver);
		super.onDestroy();
	}

	// ===========================================================
	// Buttons
	// ===========================================================
	// Start button
	View.OnClickListener mStartListener = new OnClickListener() {
		public void onClick(View v) {

			if (!started) {

				// change into stopbutton
				started = true;
				startButton.setText("Stop");

				// enable skipbutton
				skipButton.setClickable(true);

				// enable pausebutton
				pauseButton.setClickable(true);

				// Start our service
				Intent svcIntent = new Intent(TimerActivity.this,
						nl.ttys0.simplec25k.ProgramService.class);
				svcIntent.putExtra("INIT_DATA", selectedProgram);
				startService(svcIntent);

			} else
				stop();
		}
	};

	// pause button
	View.OnClickListener mPauseListener = new OnClickListener() {
		public void onClick(View v) {
			// mChronometer.stop();

			if (!paused) {

				// change pausebutton into a resume button
				paused = true;
				pauseButton.setText("Resume");

				// disable skipbutton
				skipButton.setClickable(false);

				sendIntent("PAUSE");

				countdown.stop();
				totalCountdown.stop();

			} else {

				// change into pausebutton
				paused = false;
				pauseButton.setText("Pause");

				skipButton.setClickable(true);

				sendIntent("RESUME");
			}

		}
	};

	// skip button
	View.OnClickListener mSkipListener = new OnClickListener() {
		public void onClick(View v) {
			if (prefs.getBoolean("show_alerts", true)) {
				new AlertDialog.Builder(TimerActivity.this)
					.setMessage("Are you sure you want to skip?")
					.setCancelable(false)
					.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								sendIntent("SKIP");
							}
						})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					})
					.show();
			} else {
				sendIntent("SKIP");
			}
		}
	};

	// back button (hard button)
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			stop();
		}
		return super.onKeyDown(keyCode, event);
	}

	// menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = new Intent(this, MyPreferenceActivity.class);
		startActivityForResult(myIntent, 0);
		return true;
	}

	// class for receiving broadcasts
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

			String orgData = arg1.getStringExtra("DATA_TO_TA");

			if (orgData != null) {
				if (orgData.equals("DONE")) {
					if (prefs.getBoolean("show_alerts", true)) {
						new AlertDialog.Builder(TimerActivity.this)
							.setMessage("Workout finished. Well done!")
							.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0, int arg1) {
										finish();
									}
								})
							.show();
					} else {
						finish();
					}
				} else if (orgData.equals("STARTED")) {
					if (prefs.getBoolean("show_alerts", true)) {
						new AlertDialog.Builder(TimerActivity.this)
							.setMessage("Workout started. Warmup for five minutes.\nGet started!")
							.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0, int arg1) {}
								})
							.show();
					}
				} else if (orgData.contains("SET_CURRENT")) {
					String[] s = orgData.split(";");

					current.setText(s[1]);

					countdown.setBase(System.currentTimeMillis()
							+ Integer.parseInt(s[2]));
					totalCountdown.setBase(System.currentTimeMillis()
							+ Integer.parseInt(s[3]));
					countdown.start();
					totalCountdown.start();

				}

				/*
				 * unused atm else if
				 * (orgData.contains("UPDATE_TOTAL_COUNTDOWN")){
				 * totalCountdown.setBase(System.currentTimeMillis() +
				 * arg1.getIntExtra("TIMESET", 0)*1000); totalCountdown.start();
				 * }
				 */

			}

		}

	}

}
