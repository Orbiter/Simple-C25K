//http://www.tutorialforandroid.com/2009/01/get-phone-state-when-someone-is-calling_22.html

package nl.ttys0.simplec25k;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhoneStateListener extends PhoneStateListener {
	protected static final String MY_ACTION = "MY_ACTION";

	public void onCallStateChanged(int state, String incomingNumber) {
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			// Log.d("DEBUG", "IDLE");
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			// Log.d("DEBUG", "OFFHOOK");
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			// Log.d("DEBUG", "RINGING");

			// setup intent for sending command
			Intent myIntent = new Intent();
			myIntent.setAction(MY_ACTION);
			myIntent.putExtra("DATA_TO_PS", "PAUSE");

			// send command
			Context context = TimerActivity.context;
			if (context != null) {
				context.sendBroadcast(myIntent);
			}

			// stop the countdown in the gui
			CountdownChronometer cc = TimerActivity.countdown;
			if (cc != null) {
				cc.stop();
			}
			break;
		}
	}

}