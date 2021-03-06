/* Secu3Droid - An open source, free manager for SECU-3 engine
 * control unit
 * Copyright (C) 2013 Maksim M. Levin. Russia, Voronezh
 * 
 * SECU-3  - An open source, free engine control unit
 * Copyright (C) 2007 Alexey A. Shabelnikov. Ukraine, Gorlovka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * contacts:
 *            http://secu-3.org
 *            email: mmlevin@mail.ru
*/

package org.secu3.android;

import java.util.Locale;

import org.secu3.android.api.io.Secu3Dat;
import org.secu3.android.api.io.Secu3Dat.ADCRawDat;
import org.secu3.android.api.io.Secu3Dat.FWInfoDat;
import org.secu3.android.api.io.Secu3Dat.SensorDat;
import org.secu3.android.api.io.Secu3Manager.SECU3_TASK;
import org.secu3.android.api.io.Secu3Service;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String CHECKBOX = "checkbox";
	private static final String STATUS = "status";
	private static final String DATA = "data";	

	private String sensorsFormat = "";
	private String sensorsRawFormat = "";
	private boolean isOnline;
	private boolean errors = false;
	
	ReceiveMessages receiver = null;
	TextView textViewData = null;
	TextView textViewStatus = null;
	TextView textFWInfo = null;
	CheckBox checkBox = null;	
	FWInfoDat fwInfoDat = null;
	
	public class ReceiveMessages extends BroadcastReceiver 
	{
		public IntentFilter intentFilter;
		
		public ReceiveMessages() {
			intentFilter = new IntentFilter();
			intentFilter.addAction(Secu3Service.EVENT_SECU3_SERVICE_RECEIVE_PACKET);			
			intentFilter.addAction(Secu3Service.EVENT_SECU3_SERVICE_STATUS_ONLINE);
		}
		
		@Override
		public void onReceive(Context context, Intent intent) 
		{    
			update (intent); 
		}
	}
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		setTheme(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_night_mode_key), false)?R.style.AppBaseTheme:R.style.AppBaseTheme_Light);	
		setContentView(R.layout.activity_main);		

		sensorsFormat = getString(R.string.sensors_format);
		sensorsRawFormat = getString(R.string.sensors_raw_format);
		textViewData = (TextView)findViewById(R.id.textViewData);
		textViewStatus = (TextView)findViewById(R.id.mainTextViewStatus);
		textFWInfo = (TextView)findViewById(R.id.mainTextFWInfo);
		checkBox = (CheckBox)findViewById(R.id.mainShowRawDataCheckBox);	
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (buttonView == checkBox) setRawMode(isChecked);				
			}
		});
		
		receiver = new ReceiveMessages();		
		
		if (savedInstanceState != null) {
			textViewData.setText(savedInstanceState.getString(DATA));
			textViewStatus.setText(savedInstanceState.getString(STATUS));
			checkBox.setChecked(savedInstanceState.getBoolean(CHECKBOX));
		}
		super.onCreate(savedInstanceState);		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {						
		outState.putString(DATA, textViewData.getText().toString());
		outState.putString(STATUS, textViewStatus.getText().toString());
		outState.putBoolean(STATUS, checkBox.isChecked());
		super.onSaveInstanceState(outState);		
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem m = menu.findItem(R.id.menu_errors);
		m.setIcon(errors?R.drawable.ic_menu_errors_highlighted:R.drawable.ic_menu_errors);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			startActivity(new Intent(this,SettingsActivity.class));
			return true;
		case R.id.menu_params:			
			startActivity(new Intent(this,ParamActivity.class));
			return true;
		case R.id.menu_errors:
			startActivity(new Intent(this,ErrorsActivity.class));
			return true;
		case R.id.menu_exit:
			Secu3Service.secu3Notification.notificationManager.cancelAll();
			startService(new Intent (Secu3Service.ACTION_SECU3_SERVICE_STOP,Uri.EMPTY,this,Secu3Service.class));
			return true;
		case R.id.menu_diagnostics:
			new AlertDialog.Builder(this)
				.setTitle(android.R.string.dialog_alert_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.menu_diagnostics_warning_title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {						
						if ((fwInfoDat == null) || ((fwInfoDat.options & FWInfoDat.COPT_DIAGNOSTICS) == 0)) {
							Toast.makeText(getApplicationContext(), R.string.diagnostics_not_supported_title, Toast.LENGTH_LONG).show();
						}
						startActivity(new Intent (getApplicationContext(),DiagnosticsActivity.class));
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}		
	}
	
	
	@Override
	protected void onResume() {				
		registerReceiver(receiver, receiver.intentFilter);
		startService(new Intent (Secu3Service.ACTION_SECU3_SERVICE_START,Uri.EMPTY,this,Secu3Service.class));
		super.onResume();		
	}
	
	@Override
	protected void onPause() {				
		unregisterReceiver(receiver);
		super.onPause();		
	}
	
	private void setRawMode(boolean raw) {
		SECU3_TASK task = raw?SECU3_TASK.SECU3_RAW_SENSORS:SECU3_TASK.SECU3_READ_SENSORS;
		startService(new Intent (Secu3Service.ACTION_SECU3_SERVICE_SET_TASK,Uri.EMPTY,this,Secu3Service.class).putExtra(Secu3Service.ACTION_SECU3_SERVICE_SET_TASK_PARAM, task.ordinal()));
	}
	
	void update(Intent intent) {
		if (Secu3Service.EVENT_SECU3_SERVICE_STATUS_ONLINE.equals(intent.getAction())) {			
			boolean isOnline = intent.getBooleanExtra(Secu3Service.EVENT_SECU3_SERVICE_STATUS,false); 
			String s = isOnline?getString(R.string.status_online):getString(R.string.status_offline);
			textViewStatus.setText(s);		
			if (isOnline && !this.isOnline) {
				this.isOnline = true;						
				startService(new Intent (Secu3Service.ACTION_SECU3_SERVICE_SET_TASK,Uri.EMPTY,this,Secu3Service.class).putExtra(Secu3Service.ACTION_SECU3_SERVICE_SET_TASK_PARAM, SECU3_TASK.SECU3_READ_FW_INFO.ordinal()));
				setRawMode(checkBox.isChecked());				
			}						
		} else if (Secu3Service.EVENT_SECU3_SERVICE_RECEIVE_PACKET.equals(intent.getAction()))
		{
			Secu3Dat packet = intent.getParcelableExtra(Secu3Service.EVENT_SECU3_SERVICE_RECEIVE_PARAM_PACKET);
			if (packet instanceof SensorDat) {
				SensorDat sd = (SensorDat)packet;
				boolean errors = sd.ce_errors != 0;
				if (errors != this.errors) {
					this.errors = errors;
					ActivityCompat.invalidateOptionsMenu(this);
				}
				if (!checkBox.isChecked() && (sd != null)) {
					textViewData.setText(String.format(Locale.US,sensorsFormat,
							sd.frequen, sd.pressure, sd.voltage, sd.temperat, sd.adv_angle,
							sd.knock_k, sd.knock_retard, sd.air_flow, sd.ephh_valve,
							sd.carb, sd.gas, sd.epm_valve, sd.cool_fan,sd.st_block,sd.add_i1,sd.add_i2,sd.tps,sd.choke_pos));
				}			
			} else if (packet instanceof ADCRawDat) {
				ADCRawDat ad = (ADCRawDat)packet;
				if (checkBox.isChecked() && (ad != null)) {
					textViewData.setText(String.format(Locale.US,sensorsRawFormat,
									ad.map_value, ad.ubat_value, ad.temp_value,
									ad.knock_value,ad.tps_value,ad.add_i1_value,ad.add_i2_value));
				}			
			} else if (packet instanceof FWInfoDat) {
				fwInfoDat = (FWInfoDat) packet;
				if (fwInfoDat != null) {
					textFWInfo.setText(fwInfoDat.info);
				}			
			}
		}		
	}
}
