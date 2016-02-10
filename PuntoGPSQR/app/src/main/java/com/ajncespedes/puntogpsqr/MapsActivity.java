package com.ajncespedes.puntogpsqr;

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
 * Reconoce un punto GPS codificado como código QR y nos muestra en un mapa la marca de ese objetivo al cual podemos ir
 * obteniendo nuestra propia localización, recibiendo notificaciones de distancia en Android Wear
 */
import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * @class MainActivity
 * Clase principal de la aplicación
 */

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationChangeListener {

    private GoogleMap mMap;
    private CameraUpdate mCamera;
    private Button botonQR;
    private double longitud, latitud;
    private Location destino;
    private boolean llegada = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        destino = new Location("Destino");
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview)).getMap();
        //Establecemos el mapa de tipo satélite
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        //Declaramos el botón que inicia el lector de QR
        botonQR = (Button) findViewById(R.id.buttonQR);
        botonQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCoordenadas();
            }
        });

    }

    /**
     * Método para obtener las coordenadas mediante el lector de QR
     */
    private void getCoordenadas() {
        //Lanzamos la app del lector
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            Toast.makeText(this, "¡Debes tener instalada una app de lectura de QR!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retorna la lectura del lector QR y la traduce a longitud/latitud
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                getCoordenadas();
            }
            if (resultCode == RESULT_CANCELED) {
                // handle cancel
            }
        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String resultado = data.getStringExtra("SCAN_RESULT");
                Toast.makeText(this, resultado,
                        Toast.LENGTH_SHORT).show();
                String[] palabras = resultado.split("_");
                latitud = Double.parseDouble(palabras[1]);
                longitud = Double.parseDouble(palabras[3]);
                onMapReady();
            }
            if (resultCode == RESULT_CANCELED) {
                // handle cancel
            }
        }
    }

    /**
     * Si el mapa está listo, creamos el destino del código QR y nos movemos hacia él
     */

    public void onMapReady() {
        try{
            mMap.setMyLocationEnabled(true);
        }catch (SecurityException e){}
        mMap.setOnMyLocationChangeListener(this);

        //Añadimos el lugar obtenido
        LatLng lugar = new LatLng(latitud, longitud);

        //Convertimos el lugar a tipo Location
        destino.setLatitude(latitud);
        destino.setLongitude(longitud);

        //Añadimos la marca del lugar y acercamos/movemos la cámara hacia él
        mMap.addMarker(new MarkerOptions().position(lugar).title("Destino"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lugar));
        mCamera = CameraUpdateFactory.newLatLngZoom(lugar, 15);
        mMap.animateCamera(mCamera);
    }

    /**
     * Método que detecta cambios en nuestra posición actual y envía notificaciones que recibe android wear para obtener la distancia
     * hasta el objetivo y para avisarnos si hemos llegado.
     */
    @Override
    public void onMyLocationChange(Location loc){

        //Obtenemos la distancia desde nuestra posición hasta el objetivo
        double distancia = Math.rint(loc.distanceTo(destino)*10)/10;

        //Mandamos el aviso de la distancia cada 10 metros
        if(distancia>5 && distancia % 10 < 1){
            String aviso = "Distancia "+distancia+" m";
            Notification notificacion = new NotificationCompat.Builder(getApplication())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Aviso de Distancia")
                    .setContentText(aviso)
                    .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                    .setVibrate(new long[] {0, 500})
                    .setSubText("Distancia hasta el objetivo")
                    .build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
            int notificacionId = 1;
            notificationManager.notify(notificacionId, notificacion);
        }else if(distancia < 5){ //Si la distancia es menor que 5, consideramos que ya hemos llegado
            if(!llegada){
                String aviso = "¡Ya hemos llegado al destino!";
                Notification notificacion = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Aviso de llegada")
                        .setContentText(aviso)
                        .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                        .setVibrate(new long[] {0, 1000, 1000})
                        .setSubText("Distancia hasta el objetivo")
                        .build();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
                int notificacionId = 1;
                notificationManager.notify(notificacionId, notificacion);
                llegada = true;
            }
        }
    }

}
