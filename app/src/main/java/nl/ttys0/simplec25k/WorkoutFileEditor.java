package nl.ttys0.simplec25k;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.widget.Toast;

public class WorkoutFileEditor {
	private static Context context;

	public WorkoutFileEditor(Context context) {

		WorkoutFileEditor.context = context;
	}

	public String ReadSettings(String fileName) {
		FileInputStream fIn = null;
		InputStreamReader isr = null;

		char[] inputBuffer = new char[1000];
		String data = null;

		try {
			fIn = context.openFileInput(fileName);
			isr = new InputStreamReader(fIn);
			isr.read(inputBuffer);
			data = new String(inputBuffer);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Settings not read", Toast.LENGTH_SHORT)
					.show();
		} finally {
			try {
				isr.close();
				fIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public void WriteSettings(String fileName, String data, int mode) {

		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;

		try {
			fOut = context.openFileOutput(fileName, mode);
			osw = new OutputStreamWriter(fOut);
			osw.write(data);
			osw.flush();
		} catch (Exception e) {

			Toast.makeText(context, "Can't save changes. WTF?",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} finally {
			try {
				osw.close();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
