package org.sensors2.pd.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import org.sensors2.common.sensors.DataDispatcher;
import org.sensors2.common.sensors.Parameters;
import org.sensors2.common.sensors.SensorActivity;
import org.sensors2.common.sensors.SensorCommunication;
import org.sensors2.common.wifi.WifiActivity;
import org.sensors2.pd.R;
import org.sensors2.pd.filesystem.FileLoader;
import org.sensors2.pd.filesystem.FileSelector;
import org.sensors2.pd.sensors.PdDispatcher;
import org.sensors2.pd.sensors.Settings;
import org.sensors2.pd.wifi.WifiCommunication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 12.11.14.
 */
public class Sensors2PdActivity extends Activity implements SensorEventListener, SensorActivity, WifiActivity, OnTouchListener, FileSelector.FileSelectorListener {

	private Settings settings;
	private PdDispatcher dispatcher;
	private SensorManager sensorManager;
	private SensorCommunication sensorFactory;
	private WifiManager wifiManager;
	private WifiCommunication wifiFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensors2pd);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		View view = findViewById(R.id.scrollView1);
		view.setOnTouchListener(this);
		super.onCreate(savedInstanceState);
		this.settings = this.loadSettings();
		this.dispatcher = new PdDispatcher(this);
		// Sensors
		this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.sensorFactory = new SensorCommunication(this);
		// Wifi
		this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		this.wifiFactory = new WifiCommunication(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.sensorFactory.onPause();
		this.dispatcher.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.loadSettings();
		this.dispatcher.onResume();
		this.sensorFactory.onResume();
	}

	private Settings loadSettings() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Settings settings = new Settings(preferences);
		return settings;
	}

	@Override
	public List<Parameters> GetSensors(SensorManager manager) {
		List<Parameters> parameters = new ArrayList<Parameters>();
		for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
			parameters.add(new org.sensors2.pd.sensors.Parameters(sensor));
		}
		return parameters;
	}

	@Override
	public DataDispatcher getDispatcher() {
		return this.dispatcher;
	}

	@Override
	public SensorManager getSensorManager() {
		return this.sensorManager;
	}

	@Override
	public Settings getSettings() {
		return this.settings;
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		this.sensorFactory.dispatch(sensorEvent);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sensors2_pd, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_browse:
				FileSelector loader = new FileSelector(this, this);
				loader.chooseFile();
				return true;
			case R.id.action_guide: {
				Intent intent = new Intent(this, GuideActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_settings: {
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_about: {
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		return false;
	}

	@Override
	public void onChosenFile(String filePath) {
		FileLoader loader = new FileLoader(filePath, this);
		File loadedFile = loader.getFile();
		if (this.dispatcher.setPdFile(loadedFile)) {
			findViewById(R.id.runningPdFileIntro).setVisibility(View.VISIBLE);
			TextView view = (TextView) findViewById(R.id.runningPdFile);
			view.setText(loadedFile.getName());
			view.setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.runningPdFileIntro).setVisibility(View.INVISIBLE);
			findViewById(R.id.runningPdFile).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public WifiManager getWifiManager() {
		return wifiManager;
	}
}
