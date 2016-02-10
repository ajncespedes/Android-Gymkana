package com.ajncespedes.puntomovimientosonido;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.media.SoundPool;
import android.widget.TextView;
import java.util.List;


/**
 * @class MainActivity
 * Clase principal de la aplicación
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Declaramos las variables necesarias para el sonido y el
    private SoundPool sp ;
    private int f_latigo, f_shake;
    private float actX = 0, actY = 0;
    private TextView textObjeto;
    private boolean primero = false; //Variable que comprueba los 2 pasos del movimiento del latigazo
    private long tiempo1, tiempo2, tiempoAgitar = 0; //Variables que controlan el flujo de tiempo de los dos sonidos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Inicializamos las variables
        textObjeto = (TextView) findViewById(R.id.textObjeto);
        sp = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //Cargamos los audios
        f_latigo = sp.load(this,R.raw.latigo,1);
        f_shake = sp.load(this,R.raw.shake,1);
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
        super.onResume();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sm.registerListener(this, sensors.get(0),
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onStop() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);
        sp.release();
        super.onStop();
    }


    /**
     * Método que comprueba cuál de los dos movimientos se están realizando
     * @param event evento del sensor recogido
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this){
            actX = event.values[0];
            actY = event.values[1];

            //Si el acelerómetro llega a niveles de 30 o -30, es porque estamos agitando el dispositivo
            if((actY < -30 || actY > 30) && !primero){
                sp.play(f_shake,1, 1, 0, 0,1);
                tiempoAgitar = System.currentTimeMillis();
                textObjeto.setText(getString(R.string.agitar));
            }

            //Si el dispositivo se gira hasta -8 y no se solapa con el sonido de agitar el dispositivo activamos la variable
            if(!primero && actX<-8 && (System.currentTimeMillis() - tiempoAgitar) > 1000){
                primero = true;
                tiempo1 = System.currentTimeMillis();
            }
            //Cuando hemos hecho el primer paso, si el dispositivo gira hasta 1 en poco tiempo, es un latigazo
            if(primero && actX>1){
                tiempo2 = System.currentTimeMillis() - tiempo1;
                if(tiempo2<500){ //Para ser realista el tiempo entre los 2 pasos debe ser pequeño, 0.5 segundos
                    //El volumen será más fuerte cuanto más rápido hagamos el movimiento
                    sp.play(f_latigo, 100.0f / (tiempo2), 100.0f / (tiempo2), 0, 0, 1);
                    textObjeto.setText(getString(R.string.latigo));
                }
                primero = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
