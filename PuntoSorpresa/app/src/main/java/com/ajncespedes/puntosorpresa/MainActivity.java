package com.ajncespedes.puntosorpresa;

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
