package org.fferegrino.tacometro;

import java.text.ParseException;

import org.fferegrino.tac_o_metro.R;
import org.fferegrino.tac_o_metro.R.layout;
import org.fferegrino.tac_o_metro.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends Activity {

	Button bIniciar, bTest;
	EditText editTextBrilloFondo, editTextVariacion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		bIniciar = (Button) findViewById(R.id.bIniciar);
		bTest = (Button) findViewById(R.id.bTest);
		editTextBrilloFondo = (EditText) findViewById(R.id.editTextBrilloFondo);
		editTextVariacion = (EditText) findViewById(R.id.editTextVariacion);
		
		editTextVariacion.setText("10");
		editTextBrilloFondo.setText("75");

		bIniciar.setOnClickListener(click);
		bTest.setOnClickListener(click);
	}

	OnClickListener click = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			Intent i = new Intent(StartActivity.this, MainActivity.class);
			if (arg0.getId() == R.id.bIniciar) {
				try {
					double brilloFondo = Double.parseDouble(editTextBrilloFondo
							.getText().toString());
					double variacion = Double.parseDouble(editTextVariacion
							.getText().toString());
					i.putExtra("brilloFondo", brilloFondo);
					i.putExtra("variaicion", variacion);
					i.putExtra("test", false);
				} catch (NumberFormatException exception) {
					Toast.makeText(getApplicationContext(),
							"Error en los campos de entrada", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				i.putExtra("test", true);
			}
			startActivity(i);
		}
	};

}
