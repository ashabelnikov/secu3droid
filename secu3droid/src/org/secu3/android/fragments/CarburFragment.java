package org.secu3.android.fragments;

import org.secu3.android.R;
import org.secu3.android.api.io.Secu3Dat;
import org.secu3.android.api.io.Secu3Dat.CarburPar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

public class CarburFragment extends Fragment implements ISecu3Fragment {
	EditText carburEPHHLowThreshholdGasoline;
	EditText carburEPHHHighThreshholdGasoline;
	EditText carburEPHHLowThreshholdGas;
	EditText carburEPHHHighThreshholdGas;
	EditText carburOverrunDelay;
	CheckBox carburSensorInverse;
	EditText carburEPMValveOnPressure;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) return null;
		
		return inflater.inflate(R.layout.carbur_params, null);
	}

	@Override
	public void setData(Secu3Dat packet) {
		if (packet != null && isAdded()) {
			carburEPHHLowThreshholdGasoline = (EditText)getView().findViewById(R.id.carburOverrunLowThreshholdGasolineEditText);
			carburEPHHHighThreshholdGasoline = (EditText)getView().findViewById(R.id.carburOverrunHighThreshholdGasolineEditText);
			carburEPHHLowThreshholdGas = (EditText)getView().findViewById(R.id.carburOverrunLowThreshholdGasEditText);
			carburEPHHHighThreshholdGas = (EditText)getView().findViewById(R.id.carburOverrunHighThreshholdGasEditText);
			carburOverrunDelay = (EditText)getView().findViewById(R.id.carburOverrunValveDelayEditText);
			carburEPMValveOnPressure = (EditText)getView().findViewById(R.id.carburEPMValveOnPressureEditText);
			carburSensorInverse = (CheckBox)getView().findViewById(R.id.carburSensorInverseCheckBox);
			
			carburEPHHLowThreshholdGasoline.setText(String.valueOf(((CarburPar)packet).ephh_lot));
			carburEPHHHighThreshholdGasoline.setText(String.valueOf(((CarburPar)packet).ephh_hit));
			carburEPHHLowThreshholdGas.setText(String.valueOf(((CarburPar)packet).ephh_lot_g));
			carburEPHHHighThreshholdGas.setText(String.valueOf(((CarburPar)packet).ephh_hit_g));
			carburOverrunDelay.setText(String.valueOf(((CarburPar)packet).shutoff_delay));
			carburEPMValveOnPressure.setText(String.valueOf(((CarburPar)packet).epm_ont));
			carburSensorInverse.setChecked(((CarburPar)packet).epm_ont != 0);
		}
	}
}
