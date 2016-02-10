package com.ajncespedes.puntosorpresa;

/**
 *Copyright (C) 2016  Antonio José Navarro Céspedes y Miguel Ángel Valenzuela Hidalgo
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * @author Antonio José Navarro Céspedes
 * @author Miguel Ángel Valenzuela Hidalgo
 * @version 10.02.2016
 * Detecta cuando nos acercamos al dispositivo, es una ayuda para cuando realizamos flexiones de brazos ya que cuenta automáticamente
 * las flexiones realizadas
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * @class MainActivity
 * Clase principal de la aplicación
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Declaramos las variables del sensor y las necesarias para contar las flexiones
    private TextView textFlexiones;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private int nFlexiones = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Inicializamos variables
        textFlexiones = (TextView) findViewById(R.id.textoNumero);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    /**
     * Método que realiza el conteo de veces que se tapa el sensor de proximidad
     * @param event evento realizado
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Si el evento es el del sensor de proximidad
        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)
        {
            //Si el valor es el del máximo rango, incrementamos una flexión
            if(event.values[0] == mProximity.getMaximumRange()) {
                nFlexiones++;
                textFlexiones.setText(""+nFlexiones);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
