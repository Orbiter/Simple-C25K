/*
 *  Simplec25kMainActivity.java
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

import java.io.File;
import java.util.StringTokenizer;

import android.util.Log;
import nl.ttys0.simplec25k.R;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Simplec25kMainActivity extends ListActivity {
	TextView selection;

	// files
	static final String SCHEDULEFILE = "schedule";
	static final String SETTINGSFILE = "settings";

	// global arrays containing the schedule information
	static String[] programStringArr = new String[27]; // workout name
	static Boolean[] programDoneArr = new Boolean[27]; // workout completed?
	static String selectedProgram;

	private WorkoutFileEditor workoutFileEditor = new WorkoutFileEditor(this);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		setListAdapter(new IconicAdapter());

		ProgressDialog dialog = ProgressDialog.show(
				Simplec25kMainActivity.this, "", "Loading. Please wait...",
				true);

		// test for schedule file, if it doesn't exists we'll create it
		File file = getBaseContext().getFileStreamPath(SCHEDULEFILE);

		if (!file.exists()) {
            Log.d("Simple", "Schedule file not found");
            workoutFileEditor
					.WriteSettings(SCHEDULEFILE,
                            "w1d1;false,w1d2;false,w1d3;false,"
                                    + "w2d1;false,w2d2;false,w2d3;false,"
                                    + "w3d1;false,w3d2;false,w3d3;false,"
                                    + "w4d1;false,w4d2;false,w4d3;false,"
                                    + "w5d1;false,w5d2;false,w5d3;false,"
                                    + "w6d1;false,w6d2;false,w6d3;false,"
                                    + "w7d1;false,w7d2;false,w7d3;false,"
                                    + "w8d1;false,w8d2;false,w8d3;false,"
                                    + "w9d1;false,w9d2;false,w9d3;false,",
                            MODE_PRIVATE);
		}

		// retrieve the schedule information
		String s[] = workoutFileEditor.ReadSettings(SCHEDULEFILE).split(",");
		for (int i = 0; i < 27; i++) {
			StringTokenizer tk = new StringTokenizer(s[i], ";");
			programStringArr[i] = tk.nextToken();
			programDoneArr[i] = Boolean.valueOf(tk.nextToken());
		}

		dialog.cancel();

	}

	@Override
	protected void onResume() {
		// retrieve the schedule information
		WorkoutFileEditor workoutFileEditor = new WorkoutFileEditor(this);
		String s[] = workoutFileEditor.ReadSettings(SCHEDULEFILE).split(",");
		for (int i = 0; i < 27; i++) {
			StringTokenizer tk = new StringTokenizer(s[i], ";");
			programStringArr[i] = tk.nextToken();
			programDoneArr[i] = Boolean.valueOf(tk.nextToken());
		}
		super.onResume();
	}

	// When a choice is made
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Intent myIntent = new Intent(this, TimerActivity.class);
        myIntent.putExtra("selectedProgram", programStringArr[position]);
		startActivityForResult(myIntent, 0);

	}

	// class to fill list
	class IconicAdapter extends ArrayAdapter<String> {

		IconicAdapter() {
			super(Simplec25kMainActivity.this, R.layout.row, programStringArr);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, parent, false);
			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(programStringArr[position].replace("w", "Week ")
					.replace("d", ", Day"));

			ImageView icon = (ImageView) row.findViewById(R.id.icon);

			if (Boolean
					.valueOf(Simplec25kMainActivity.programDoneArr[position])) {
				icon.setImageResource(R.drawable.ok);
			} else {
				icon.setImageResource(R.drawable.clean);
			}

			return (row);
		}
	}

	// class for receiving broadcasts. In this case it's being used to receive
	// commands from the TimerActivity
	@SuppressWarnings("unused")
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

			String orgData = arg1.getStringExtra("DATA_TO_MAIN");

			// here we can receive commands from timerActivity

			if (orgData != null) {
				if (orgData.equals("UPDATED")) {
					// retrieve the schedule information
					String s[] = workoutFileEditor.ReadSettings(SCHEDULEFILE)
							.split(",");
					for (int i = 0; i < 27; i++) {
						StringTokenizer tk = new StringTokenizer(s[i], ";");
						programStringArr[i] = tk.nextToken();
						programDoneArr[i] = Boolean.valueOf(tk.nextToken());
					}
				}
			}
		}
	}
}