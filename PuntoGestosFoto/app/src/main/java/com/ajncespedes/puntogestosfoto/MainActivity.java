package com.ajncespedes.puntogestosfoto;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * @class MainActivity
 * Clase principal de la aplicación
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnTouchListener {

    //Inicializamos las variables necesarias
    private TextView textPatron;
    private ArrayList<ImageView> cuadricula;
    private ArrayList<Boolean> patronDesbloqueo, patron;
    int TAKE_PHOTO_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Inicializamos los arrays para la cuadrícula y el patrón de desbloqueo
        cuadricula = new ArrayList<>();
        patronDesbloqueo = new ArrayList<>();
        patron = new ArrayList<>();

        //Cargamos las imágenes de la cuadrícula
        cuadricula.add((ImageView) findViewById(R.id.img_1_1));
        cuadricula.add((ImageView) findViewById(R.id.img_1_2));
        cuadricula.add((ImageView) findViewById(R.id.img_1_3));
        cuadricula.add((ImageView) findViewById(R.id.img_2_1));
        cuadricula.add((ImageView) findViewById(R.id.img_2_2));
        cuadricula.add((ImageView) findViewById(R.id.img_2_3));
        cuadricula.add((ImageView) findViewById(R.id.img_3_1));
        cuadricula.add((ImageView) findViewById(R.id.img_3_2));
        cuadricula.add((ImageView) findViewById(R.id.img_3_3));

        //Establecemos un patrón de desbloqueo
        patronDesbloqueo.add(true); patronDesbloqueo.add(true); patronDesbloqueo.add(true);
        patronDesbloqueo.add(true); patronDesbloqueo.add(false); patronDesbloqueo.add(false);
        patronDesbloqueo.add(true); patronDesbloqueo.add(true); patronDesbloqueo.add(true);

        //Inicializamos el patrón real a falso
        for(int i = 0; i < 9; i++){
            patron.add(false);
        }

        //Añadimos los listeners a las imágenes de la cuadrícula
        for(int i = 0; i < 9; i++){
            cuadricula.get(i).setOnClickListener(this);
            cuadricula.get(i).setOnTouchListener(this);
        }

        textPatron = (TextView) findViewById(R.id.textoPatron);
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
     * Método para comprobar si una posición x,y corresponde a una posición de la cuadrícula.
     * @param v Imagen que corresponde a una posición de la cuadrícula.
     * @param x Posición x de la pantalla
     * @param y Posición y de la pantalla
     */
    public boolean estaEn(ImageView v, int x, int y){

        int [] location = new int[2];
        //Determinamos el tamaño de la celda (1,1)
        cuadricula.get(0).getLocationOnScreen(location);
        int celda_1_1_X = location[0];

        //Determinamos el tamaño de la celda (1,2)
        cuadricula.get(1).getLocationOnScreen(location);
        int celda_1_2_X = location[0];

        //Determinamos el ancho/alto de cualquier celda de la cuadrícula
        int tamanoCelda = celda_1_2_X - celda_1_1_X;

        v.getLocationOnScreen(location);

        //Si la posición se encuentra dentro de la imagen, devuelve true
        if((x > location[0] && x < location[0] + tamanoCelda)  && (y > location[1] && y < location[1] + tamanoCelda)){
            return true;
        }

        return false;
    }

    /**
     * Método que inicia la cámara
     */
    public void iniciarCamara(){

        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        File newdir = new File(dir);
        newdir.mkdirs();

        //Llamamos a la foto que tomemos PuntoGestoFoto.jpg
        String file = dir+"PuntoGestoFoto.jpg";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        }
        catch (IOException e)
        {
        }

        Uri outputFileUri = Uri.fromFile(newfile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
    }


    @Override
    public void onClick(View v) {

    }


    /**
     * Método que comprueba el gesto del patrón realizado
     * @param event evento realizado
     * @param v vista con la que interactuamos
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //Obtenemos la posición x e y en la pantalla donde estamos tocando
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        //Rellenamos el vector del patrón según la casilla en la que nos encontremos y la pintamos de amarillo
        for(int i = 0; i < 9; i++){
            if(estaEn(cuadricula.get(i),x,y)){
                patron.set(i,true);
                cuadricula.get(i).setColorFilter(Color.YELLOW);
            }
        }

        //Cuando levantamos el dedo tras el gesto, comprobamos si el patrón es el correcto
        if (event.getAction() == MotionEvent.ACTION_UP) {
            boolean distinto = false;
            for(int i = 0; i < 9 && !distinto; i++){
                if(patron.get(i) != patronDesbloqueo.get(i)){
                    distinto = true;
                }
            }
            //Si el patrón no es correcto, limpiamos variables y llamamos a la cámara
            if(!distinto){
                textPatron.setText(getString(R.string.patron_desbloqueado));
                //Reseteamos los valores para volver a intentar
                for(int i = 0; i < 9; i++){
                    cuadricula.get(i).setColorFilter(Color.WHITE);
                }
                for(int i = 0; i < 9; i++){
                    patron.set(i,false);
                }
                iniciarCamara();
            }else{
                textPatron.setText(getString(R.string.patron_incorrecto));
                for(int i = 0; i < 9; i++){
                    if(patron.get(i) == true){
                        cuadricula.get(i).setColorFilter(Color.RED);
                    }
                }
                //Pintamos durante 0.2 segundos el recorrido de rojo, indicando el error
                new CountDownTimer(200, 50) {
                    @Override
                    public void onTick(long arg0) {
                        // TODO Auto-generated method stub
                    }
                    @Override
                    public void onFinish() {
                        //Reseteamos los valores para volver a intentar
                        for(int i = 0; i < 9; i++){
                            cuadricula.get(i).setColorFilter(Color.WHITE);
                        }
                        for(int i = 0; i < 9; i++){
                            patron.set(i,false);
                        }
                    }
                }.start();

            }
        }
        return false;
    }
}
