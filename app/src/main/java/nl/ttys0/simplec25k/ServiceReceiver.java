//http://www.tutorialforandroid.com/2009/01/get-phone-state-when-someone-is-calling_22.html
package nl.ttys0.simplec25k;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class ServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		MyPhoneStateListener phoneListener = new MyPhoneStateListener();
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
}