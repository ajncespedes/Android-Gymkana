package com.valen.brujulavoz;

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
 * Reconoce un punto cardinal y un error por voz y nos muestra una brújula y una flecha que se torna verde cuando estamos
 * en la posición correcta dada por el punto cardinal y el error
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Variables necesarias para reconocer la voz
    private final static int DEFAULT_NUMBER_RESULTS = 10;
    private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;

    private int numberRecoResults = DEFAULT_NUMBER_RESULTS;
    private String languageModel = DEFAULT_LANG_MODEL;

    private static final String LOGTAG = "ASRBEGIN";
    private TextView coordenadas;
    private String orientacionString = "";
    //Variable que indica norte, este, sur u oeste que queremos señalar
    private int N_E_S_O = 0;
    //Error en grados
    private int error = 0;
    private static int ASR_CODE = 123;

    private ImageView imgBrujula, imgFlecha;

    //Guarda el angulo (grado) actual del compass
    private float gradoActual = 0f;

    //Declaramos los sensores
    private SensorManager mSensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    float degree;
    //Variables para guardar los valores de los sensores
    float azimut;
    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordenadas=(TextView)findViewById(R.id.orientacion);

        // Se guardan en variables los elementos del layout
        imgBrujula = (ImageView) findViewById(R.id.imagenBrujula);
        imgFlecha = (ImageView) findViewById(R.id.imagenFlecha);

        // Se inicializa los sensores del dispositivo android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mGravity = null;
        mGeomagnetic = null;

        setSpeakButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /**
     * Método para obtener los parámetros que afectarán a la brújula.
     * @param lecturas Array de posibles traducciones que se han obtenido con la interpretación del audio.
     */
    public void leerParametrosParaBrujula(ArrayList<String> lecturas){
            boolean encontrado = false;
            int posicion = 0;
            while(!encontrado && posicion < lecturas.size()){
                String[] palabras = lecturas.get(posicion).toUpperCase().split(" ");
                if (palabras[0].equals("NORTE")) {
                    N_E_S_O = 0;
                    error = Integer.parseInt(palabras[1]);
                    orientacionString = "NORTE";
                    encontrado=true;

                } else if (palabras[0].equals("ESTE")) {
                    N_E_S_O = 1;
                    error = Integer.parseInt(palabras[1]);
                    orientacionString = "ESTE";
                    encontrado=true;
                } else if (palabras[0].equals("SUR")) {
                    N_E_S_O = 2;
                    error = Integer.parseInt(palabras[1]);
                    orientacionString = "SUR";
                    encontrado=true;
                } else if (palabras[0].equals("OESTE")) {
                    N_E_S_O = 3;
                    error = Integer.parseInt(palabras[1]);
                    orientacionString = "OESTE";
                    encontrado=true;
                } else{
                    posicion++;
                    if(posicion==lecturas.size()){
                        N_E_S_O=0;
                        error=0;
                    }
                }
            }


    }

    public void setRecognitionParams()  {
        numberRecoResults = 10;
        languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    }


    /**
     * Método principal que realiza las tareas de la aplicación.
     * @param requestCode Código con el que comenzó el Activity.
     * @param resultCode Código devuelto por el Activity.
     * @param data Datos devueltos por el Activity.
     */
    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASR_CODE)  {
            if (resultCode == RESULT_OK)  {
                if(data!=null) {
                    //Retrieves the N-best list and the confidences from the ASR result
                    ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    float[] nBestConfidences = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)  //Checks the API level because the confidence scores are supported only from API level 14
                        nBestConfidences = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                    //Creates a collection of strings, each one with a recognition result and its confidence
                    //following the structure "Phrase matched (conf: 0.5)"
                    ArrayList<String> nBestView = new ArrayList<String>();

                    for(int i=0; i<nBestList.size(); i++){
                        if(nBestConfidences!=null){
                            if(nBestConfidences[i]>=0)
                                nBestView.add(nBestList.get(i) + " (conf: " + String.format("%.2f", nBestConfidences[i]) + ")");
                            else
                                nBestView.add(nBestList.get(i) + " (no confidence value available)");
                        }
                        else
                            nBestView.add(nBestList.get(i) + " (no confidence value available)");
                    }
                    leerParametrosParaBrujula(nBestList);
                    if(error==0){
                        coordenadas.setText("No he entendido");
                    }else{
                        coordenadas.setText(""+ orientacionString + " " + error);
                    }
                }
            }
            else {
                //Reports error in recognition error in log
                Log.e(LOGTAG, "Recognition was not successful");
            }
        }
    }

    /**
     * Método para abrir la ventana que escucha la voz
     */
    private void listen()  {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numberRecoResults);
        startActivityForResult(intent, ASR_CODE);
    }

    /**
     * Método para crear el botón que abre la ventana de google
     */
    private void setSpeakButton() {
        //Gain reference to speak button
        Button speak = (Button) findViewById(R.id.boton_reconocimiento);

        //Set up click listener
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Speech recognition does not currently work on simulated devices,
                //it the user is attempting to run the app in a simulated device
                //they will get a Toast
                if("generic".equals(Build.BRAND.toLowerCase())){
                    Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(LOGTAG, "ASR attempt on virtual device");
                }
                else{
                    setRecognitionParams(); //Read speech recognition parameters from GUI
                    listen(); 				//Set up the recognizer with the parameters and start listening
                }
            }
        });
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

    /**
     * Método que se actualiza constantemente cada vez que un sensor cambia su estado, gira la imagen de la brújula y
     * comprueba si estamos en la posición correcta
     * @param event variable de eventos de los sensores del dispositivo
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Se comprueba que tipo de sensor está activo en cada momento
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
        }

        if ((mGravity != null) && (mGeomagnetic != null)) {
            float RotationMatrix[] = new float[16];
            boolean success = SensorManager.getRotationMatrix(RotationMatrix,                                                             null, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(RotationMatrix, orientation);
                azimut = orientation[0] * (180 / (float) Math.PI);
            }
        }
        degree = azimut;
        //Animación de la rotación
        RotateAnimation ra = new RotateAnimation(
                gradoActual,
                degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(1000);
        ra.setFillAfter(true);
        imgBrujula.startAnimation(ra);
        gradoActual = -degree;

        //Si apunta adecuadamente, ponemos la flecha verde
        if(N_E_S_O==0 && degree < error/2 && degree > -error/2){
            imgFlecha.setImageDrawable(getResources().getDrawable(R.drawable.flecha_verde));
        }else if(N_E_S_O==1 && degree < (90+error/2) && degree > (90-error/2)){
            imgFlecha.setImageDrawable(getResources().getDrawable(R.drawable.flecha_verde));
        }else if(N_E_S_O==2 && ( (degree<0 && degree<-180+error/2) || (degree>=0 && degree>180-error/2) ) ){
            imgFlecha.setImageDrawable(getResources().getDrawable(R.drawable.flecha_verde));
        }else if(N_E_S_O==3 && degree < (-90+error/2) && degree > (-90-error/2)){
            imgFlecha.setImageDrawable(getResources().getDrawable(R.drawable.flecha_verde));
        }else{
            imgFlecha.setImageDrawable(getResources().getDrawable(R.drawable.flecha_roja));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
