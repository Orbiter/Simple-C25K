/*
 *  ProgramService.java
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
 */

package nl.ttys0.simplec25k;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

public class ProgramService extends Service {
	private static final String SCHEDULEFILE = "schedule";
	protected static final String MY_ACTION = "MY_ACTION";

	private String selectedProgram;
	private boolean running, paused, skipInterval;

	private Thread workoutThread;
	private WakeLock mWakeLock;
	private PendingIntent pi;
	private MyReceiver myReceiver;
	private AlarmManager alarmManager;
	
	private boolean mediaSoundBool;
	private boolean vibrateBool;
	private float mediaSoundVolume;

	private String lastTickerText;

	private int totalTimeLeft;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		// Register BroadcastReceiver
		// to receive event from our service
		myReceiver = new MyReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MyAlarmService.MY_ACTION);
		registerReceiver(myReceiver, intentFilter);

		// Toast.makeText(this, "program Service Started", Toast.LENGTH_LONG)
		// .show();
		// Log.d(TAG, "onCreate");

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String wakelockType = prefs.getString("wakelock", "dim");
        Log.d("SimpleC25K", "Wakelock: " + wakelockType);

        if(wakelockType.equals("partial")){
		    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        }
        else if(wakelockType.equals("full")){
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
        }
        else{
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");
        }
		mWakeLock.acquire();

	}

	@Override
	public void onDestroy() {
		// Toast.makeText(this, "program Service destroyed", Toast.LENGTH_LONG)
		// .show();
		// Log.d(TAG, "onDestroy");

		mWakeLock.release();

		workoutThread.interrupt();// probably not even necessary
		unregisterReceiver(myReceiver);
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startid) {
		// retrieve workout info
		selectedProgram = intent.getStringExtra("INIT_DATA");

		workoutThread = new Thread() {
			@Override
			public void run() {

				startWorkout();
			}
		};
		workoutThread.start();

	}

	// ============================================================
	// ROUTINES
	// ============================================================
	// awful bloody mess, in the future the information should be read from a
	// file
	private void startWorkout() {

		totalTimeLeft = 0;

		Intent myIntent = new Intent();
		myIntent.setAction(MY_ACTION);
		myIntent.putExtra("DATA_TO_TA", "STARTED");
		sendBroadcast(myIntent);

		running = true;
		paused = false;
		skipInterval = false;

		// w1d1, w1d2, w1d3 //total workout time: 25m
		if (selectedProgram.contains("w1")) {
			totalTimeLeft = 25 * 60 * 1000;
			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

			// 60+90=2.5minutes, so do this 8 times for a total of 20
			for (int i = 0; i < 8; i++) {
				// jogging 60s
				if (running)
					countdown(60, 500, "Jogging", "Jog for ");
				// walking 90s
				if (running)
					countdown(90, 500, "Walking", "Walk for ");
			}
		}

		// w2
		else if (selectedProgram.contains("w2")) {
			totalTimeLeft = 25 * 60 * 1000;
			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

			// 90+120=3.5minutes, so do this 5 times
			for (int i = 0; i < 5; i++) {
				// jogging 90s
				if (running)
					countdown(90, 500, "Jogging", "Jog for ");
				// walking 120s
				if (running)
					countdown(120, 500, "Walking", "Walk for ");
			}
			// more running 'cause 5*3.5=17.5
			if (running)
				countdown(90, 500, "Jogging", "Jog for ");
			// walk 60
			if (running)
				countdown(60, 500, "Walking", "Walk for ");
		}

		// w3
		else if (selectedProgram.contains("w3")) {
			totalTimeLeft = 1380 * 1000;
			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

			// 2 times
			for (int i = 0; i < 2; i++) {
				// jogging 90s
				if (running)
					countdown(90, 500, "Jogging", "Jog for ");
				// walking 90s
				if (running)
					countdown(90, 500, "Walking", "Walk for ");
				// jogging 3m
				if (running)
					countdown(180, 500, "Jogging", "Jog for ");
				// walking 3m
				if (running)
					countdown(180, 500, "Walking", "Walk for ");
			}
		}

		// w4
		else if (selectedProgram.contains("w4")) {
			totalTimeLeft = 1590 * 1000;

			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

			// jogging 3m
			if (running)
				countdown(180, 500, "Jogging", "Jog for ");
			// walking 90s
			if (running)
				countdown(90, 500, "Walking", "Walk for ");
			// jogging 5m
			if (running)
				countdown(300, 500, "Jogging", "Jog for ");
			// walking 2.5m
			if (running)
				countdown(150, 500, "Walking", "Walk for ");
			// jogging 3m
			if (running)
				countdown(180, 500, "Jogging", "Jog for ");
			// walking 90s
			if (running)
				countdown(90, 500, "Walking", "Walk for ");
			// jogging 5m
			if (running)
				countdown(300, 500, "Jogging", "Jog for ");

		}

		// w5
		else if (selectedProgram.contains("w5")) {

			if (selectedProgram.equals("w5d1")) {
				totalTimeLeft = 1560 * 1000;
				// warmup 5m
				if (running)
					countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

				// jogging 5m
				if (running)
					countdown(300, 500, "Jogging", "Jog for ");
				// walking 3m
				if (running)
					countdown(180, 500, "Walking", "Walk for ");
				// jogging 5m
				if (running)
					countdown(300, 500, "Jogging", "Jog for ");
				// walking 3m
				if (running)
					countdown(180, 500, "Walking", "Walk for ");
				// jogging 5m
				if (running)
					countdown(300, 500, "Jogging", "Jog for ");

			}

			if (selectedProgram.equals("w5d2")) {
				totalTimeLeft = 1560 * 1000;
				// warmup 5m
				if (running)
					countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

				// jogging 8m
				if (running)
					countdown(480, 500, "Jogging", "Jog for ");
				// walking 5m
				if (running)
					countdown(300, 500, "Walking", "Walk for ");
				// jogging 8m
				if (running)
					countdown(480, 500, "Jogging", "Jog for ");
			}

			if (selectedProgram.equals("w5d3")) {
				totalTimeLeft = (1500 * 1000);
				// warmup 5m
				if (running)
					countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

				// jogging 20m
				if (running)
					countdown(1200, 500, "Jogging", "Jog for ");

			}
		}

		// w6
		else if (selectedProgram.contains("w6")) {

			if (selectedProgram.equals("w6d1")) {
				totalTimeLeft = 1740 * 1000;

				// warmup 5m
				if (running)
					countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

				// jogging 5m
				if (running)
					countdown(300, 500, "Jogging", "Jog for ");
				// walking 3m
				if (running)
					countdown(180, 500, "Walking", "Walk for ");
				// jogging 8m
				if (running)
					countdown(480, 500, "Jogging", "Jog for ");
				// walking 3m
				if (running)
					countdown(180, 500, "Walking", "Walk for ");
				// jogging 5m
				if (running)
					countdown(300, 500, "Jogging", "Jog for ");

			} else if (selectedProgram.equals("w6d2")) {
				totalTimeLeft = 1680 * 1000;
				// warmup 5m
				if (running)
					countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

				// jogging 10m
				if (running)
					countdown(600, 500, "Jogging", "Jog for ");
				// walking 3m
				if (running)
					countdown(180, 500, "Walking", "Walk for ");
				// jogging 10m
				if (running)
					countdown(600, 500, "Jogging", "Jog for ");

			} else if (selectedProgram.equals("w6d3")) {
				totalTimeLeft = 1620 * 1000;
				// warmup 5m
				if (running)
					countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");

				// jogging 22m
				if (running)
					countdown(1320, 500, "Jogging", "Jog for ");

			}

		}

		else if (selectedProgram.contains("w7")) {
			totalTimeLeft = 1800 * 1000;
			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");
			// jogging 25m
			if (running)
				countdown(1500, 500, "Jogging", "Jog for ");

		}

		else if (selectedProgram.contains("w8")) {
			totalTimeLeft = 1980 * 1000;
			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");
			// jogging 28m
			if (running)
				countdown(1680, 500, "Jogging", "Jog for ");

		}

		// w9d3
		else if (selectedProgram.contains("w9")) {
			totalTimeLeft = 2100 * 1000;
			// warmup 5m
			if (running)
				countdown(5 * 60, 500, "Warmup", "Warmup: Brisk walk for ");
			// jogging 30m
			if (running)
				countdown(1800, 500, "Jogging", "Jog for ");
		}

		workoutFinished();
	}

	// ============================================================
	// END OF ROUTINES
	// ============================================================

	private void workoutFinished() {
		WorkoutFileEditor wfe = new WorkoutFileEditor(this);

		// if running is still true, we can safely assume the workout is
		// completed. If running == false, the workout is interrupted by the
		// user and should not be marked 'done'
		if (running) {

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//get preferences
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			mediaSoundVolume = (float)(Integer.parseInt(prefs.getString("volume_percentage","40"))/100f);
			mediaSoundBool= prefs.getBoolean("enable_sound", true);
			MediaPlayer mp = MediaPlayer
					.create(ProgramService.this, R.raw.beep);
			if(mp!=null && mediaSoundBool){
				mp.setVolume(mediaSoundVolume, mediaSoundVolume);
				mp.start();
			}

			sendNotificationReal("Workout completed", "Workout completed.", true);

			// let the user know we're done
			// setup vibrator
			vibrateBool = prefs.getBoolean("enable_vibrations", true);
			if(vibrateBool){
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				long[] pat = { 0, 700, 400 };
				v.vibrate(pat, -1);
			}

			// update file
			String s = wfe.ReadSettings(SCHEDULEFILE);
			s = s.replace(selectedProgram + ";false", selectedProgram + ";true");

			wfe.WriteSettings(SCHEDULEFILE, s, MODE_PRIVATE);

			// alert main to update its array
			Intent myIntent = new Intent();
			myIntent.setAction(MY_ACTION);
			myIntent.putExtra("DATA_TO_MAIN", "UPDATED");
			sendBroadcast(myIntent);

			// inform the timerActivity that the workout is done
			myIntent.setAction(MY_ACTION);
			myIntent.putExtra("DATA_TO_TA", "DONE");
			sendBroadcast(myIntent);

		}
		// stop this service
		this.stopSelf();

	}

	// counts down and sends message
	// 'time' in seconds, 'interval' in ms
	// make sure interval is never set on 0, this will
	// overload the statusbar
	// returns false if the user pressed stop
	private boolean countdown(int time, int interval, String workout,
			String message) {
		long syst = System.currentTimeMillis();
		long systFinished = syst + time * 1000;
		long msUntilFinished = +systFinished - syst;
		int secondsUntilFinished = (int) ((systFinished - syst) / 1000);

		int seconds = secondsUntilFinished % 60;
		int minutes = secondsUntilFinished / 60;

		// set alarm
		startTimer(time * 1000, 0, "");

		// Send data for the GUI
		Intent myIntent = new Intent();
		myIntent.setAction(MY_ACTION);
		myIntent.putExtra("DATA_TO_TA", "SET_CURRENT;" + workout + ";"
				+ (time * 1000) + ";" + totalTimeLeft);
		sendBroadcast(myIntent);

		// dialog.cancel();

		// updateTotalCountdown(totalTimeLeft);

		// send initial notification
		sendNotification(workout, message + String.format("%02d", minutes)
				+ ":" + String.format("%02d", seconds));

		while (systFinished - syst > 0) {
			if ((!paused) && (running)) {
				try {

					// get current systemtime
					syst = System.currentTimeMillis();

					// how many ms are left
					msUntilFinished = systFinished - syst;

					// how many seconds are left
					secondsUntilFinished = (int) (msUntilFinished / 1000);

					// parse to readable format (mm:ss)
					seconds = secondsUntilFinished % 60;
					minutes = secondsUntilFinished / 60;

					// if (secondsUntilFinished%5==0){//take it easy on the
					// notificationbar

					sendNotification(workout,
							message + String.format("%02d", minutes) + ":"
									+ String.format("%02d", seconds));
					// }

					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (paused) {

				// cancel scheduled alarm
				stopTimer(pi);

				sendNotification("Paused", message + "-Paused-");

				// myIntent.putExtra("DATA_TO_TA", "SET_CURRENT;" + workout +
				// ";" + (msUntilFinished));

				// wait for resume
				while (paused) {
					try {
						// Get out of here if the user requests a STOP
						if (!running) {
							break;
						}

						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// reset gui elements
				// Send data for the GUI
				myIntent.setAction(MY_ACTION);
				myIntent.putExtra("DATA_TO_TA", "SET_CURRENT;" + workout + ";"
						+ (msUntilFinished) + ";"
						+ (totalTimeLeft - (time * 1000 - msUntilFinished)));
				sendBroadcast(myIntent);

				// set alarm again with the last known resulting time
				startTimer((int) msUntilFinished, 0, "");

				// update systFinished
				systFinished = System.currentTimeMillis() + msUntilFinished;

			}

			if ((skipInterval) && (running) && (!paused)) {
				// Disable skipping
				skipInterval = false;

				// cancel scheduled alarm
				stopTimer(pi);

				totalTimeLeft -= time * 1000;
				// return to startworkout for next interval
				return false;

			}

			if (!running) {
				// cancel scheduled alarm
				stopTimer(pi);

				totalTimeLeft -= time * 1000;

				// make sure the thread is stopped before killing this service
				// systFinished = 0;
				return false;

			}

		}
		totalTimeLeft -= time * 1000;
		return true;
	}

	// start a timer with time in mili seconds
	public void startTimer(int time, int id, String message) {

		Intent mi = new Intent(ProgramService.this, MyAlarmService.class);
		mi.putExtra("MESSAGE", message);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.MILLISECOND, time);

		pi = PendingIntent.getService(ProgramService.this, id, mi, 0);

		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				pi);

		// Toast.makeText(ProgramService.this, "Start Alarm", Toast.LENGTH_LONG)
		// .show();

	}

	public void stopTimer(PendingIntent timerpi) {
		pi.cancel();

	}

	// ===========================================================
	// Helper functions
	// ===========================================================
	// Get a place in the notification bar and send a string
	// For sending 'real' notifications (those which can be 'cleaned')
	// from the bar, I'd suggest using 'sendNotificationReal()'
	public void sendNotification(String tickerText, String contentText) {
		//int icon = nl.ttys0.simplec25k.R.drawable.nbarlogo;//runner
		int icon = nl.ttys0.simplec25k.R.drawable.ic_launcher;

		CharSequence contentTitle = "Simple C25K";

		// define the actions to perform when user touch the notification
		Intent launchApp = new Intent(this, TimerActivity.class);
		// launchApp.putExtra("com.xxxxxxx.xxxxxxxxx.bean.Item",
		// "anyObjectYouWant");

		launchApp.setAction("VIEW_DETAILS_PROPERTY");
		PendingIntent launchNotification = PendingIntent.getActivity(
				getApplicationContext(), 0, launchApp, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setSmallIcon(icon);
		builder.setTicker(tickerText);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setContentIntent(launchNotification);
		final Notification notification = builder.build();

		boolean vibrate = false;
		if (! tickerText.equals(lastTickerText)) {
			vibrate = true;
			lastTickerText = tickerText;
		}
		sendNotificationReal(tickerText, contentText, vibrate);

		startForeground(1337, notification);
	}

	// This method can be used to send a 'real' notification. This notification
	// can be removed from the bar by the user by clicking them
	// Additionally these notifications are shown on connected Wear-devices.
	// There's a hardcoded id in here, so the can only be only one notification
	// at the time.
	public void sendNotificationReal(String tickerText, String contentText, boolean vibrate) {

		// Get a reference to the NotificationManager:
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		// Instantiate the Notification:
		int icon = nl.ttys0.simplec25k.R.drawable.ic_launcher;
		long when = System.currentTimeMillis();


		// Define the notification's message and PendingIntent:
		Context context = getApplicationContext();
		CharSequence contentTitle = "Simple C25K";
		Intent notificationIntent = new Intent(this, TimerActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(icon);
		builder.setTicker(tickerText);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setContentIntent(contentIntent);
		if (vibrate) {
			builder.setVibrate(new long[]{0, 1000});
			Log.e("VibrationMotor", "sendNotificationReal: VIBRATING");
		}
		final Notification notification = builder.build();

		// Pass the Notification to the NotificationManager:
		final int HELLO_ID = 1;
		mNotificationManager.notify(HELLO_ID, notification);

		if (vibrate) {
			try {
				// pause so that vibration is not interrupted by next notification
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	// class for receiving broadcasts. In this case it's being used to receive
	// commands from the TimerActivity
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

			String orgData = arg1.getStringExtra("DATA_TO_PS");

			if (orgData != null) {
				if (orgData.equals("PAUSE"))
					paused = true;
				if (orgData.equals("RESUME"))
					paused = false;
				if (orgData.equals("STOP"))
					running = false;
				if (orgData.equals("SKIP"))
					skipInterval = true;
			}

		}

	}

}
