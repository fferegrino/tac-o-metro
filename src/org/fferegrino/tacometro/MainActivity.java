package org.fferegrino.tacometro;

import java.net.Socket;
import java.text.ParseException;

import org.fferegrino.tacometro.general.CameraDraw;
import org.fferegrino.tacometro.general.Preview;

import org.fferegrino.tac_o_metro.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected PowerManager.WakeLock wakelock;

	Preview mPreview;
	Button bSetPromedio;
	EditText textPromedio;
	TextView textRPM;
	CameraDraw mDrawOnTop;

	boolean preserveValues;
	Contador c = new Contador();

	Button bSetFondo;

	ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		Bundle extras = getIntent().getExtras();
		boolean test = extras.getBoolean("test");

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"tacOmetro");
		wakelock.acquire();

		mDrawOnTop = (CameraDraw) findViewById(R.id.Draw);
		mPreview = (Preview) findViewById(R.id.Preview);
		textRPM = (TextView) findViewById(R.id.textRPM);

		mPreview.setCameraDrawCompanion(mDrawOnTop);
		
		if(!test){
			mDrawOnTop.setPromedio(extras.getDouble("brilloFondo"));
			mDrawOnTop.setVariacionMaxima(extras.getDouble("variacion"));
			c.execute(mDrawOnTop);
		}
		else{
			textRPM.setVisibility(View.INVISIBLE);
			mDrawOnTop.setPromedio(1000);
			mDrawOnTop.setVariacionMaxima(2000);
		}

	}

	protected void onDestroy() {
		super.onDestroy();
	}
	
	protected void onPause(){
		super.onPause();
		this.wakelock.release();
	}

	protected void onResume() {
		super.onResume();
		wakelock.acquire();
	}

	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		this.wakelock.release();
	}

	public class Contador extends AsyncTask<CameraDraw, Double, Void> {
		int segs;

		@Override
		protected Void doInBackground(CameraDraw... params) {
			segs = 0;
			final CameraDraw s = params[0];
			new Thread() {
				public void run() {
					while (!isCancelled()) {
						try {
							segs++;
							s.setSegundos(segs);
							int pasos = s.getPasos();
							Double rpm = (((double) pasos * 60) / (double) segs);
							publishProgress(rpm);
							sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			}.start();
			return null;
		}

		@Override
		protected void onProgressUpdate(Double... values) {
			textRPM.setText(String.format("%1$3.3f", values[0]));
		}

	}

}