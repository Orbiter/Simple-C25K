package nl.ttys0.simplec25k;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import nl.ttys0.simplec25k.R;

public class MyPreferenceActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Toast.makeText(this, "wham", Toast.LENGTH_LONG).show();
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference customPref = (Preference) findPreference("test_volume");
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						playSound();
						return true;
					}

				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "Show current settings");
		return super.onCreateOptionsMenu(menu);
	}

	public void playSound() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		float mediaSoundVolume = (float) (Integer.parseInt(sharedPrefs
				.getString("volume_percentage", "40")) / 100f);
		boolean mediaSoundBool = sharedPrefs.getBoolean("enable_sound", true);
		MediaPlayer mp = MediaPlayer.create(MyPreferenceActivity.this,
				R.raw.beep);
		if (mp != null && mediaSoundBool) {
			mp.setVolume(mediaSoundVolume, mediaSoundVolume);
			mp.start();
		}
	}
}
