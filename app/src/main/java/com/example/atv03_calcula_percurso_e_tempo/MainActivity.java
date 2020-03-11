package com.example.atv03_calcula_percurso_e_tempo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView distanciaInputTextView;
    private TextInputEditText procurarInputText;
    private Chronometer tempoPercorridoChronometer;

    private Button condecederPermisaoButton;
    private Button ativarGPSButton;
    private Button desativarGPSButton;
    private Button iniciarPercursoButton;
    private Button terminarPercursoButton;
    private FloatingActionButton procurarButton;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private boolean gps_ativado = false;
    private boolean inicio_percurso = false;

    private static final int REQUEST_GPS_CODE = 1001;

    private Location localidadeAnterior;

    private double distancia = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarVariaveis();


        condecederPermisaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS_CODE);
                gps_ativado = true;
            }
        });

        desativarGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(gps_ativado)
                       desativarGPS();
                    else
                        Toast.makeText(MainActivity.this, getString(R.string.gps_ja_desativado), Toast.LENGTH_SHORT).show();
            }
        });

        ativarGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ativarGPS();
            }
        });

        iniciarPercursoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps_ativado){
                    inicio_percurso = true;
                    distanciaInputTextView.setText(String.format(Locale.getDefault(),"%.1f KM",distancia/100000000));
                    tempoPercorridoChronometer.setBase(SystemClock.elapsedRealtime());
                    tempoPercorridoChronometer.start();
                }
                else
                    Toast.makeText(MainActivity.this, getString(R.string.precisa_ativar_GPS), Toast.LENGTH_SHORT).show();

            }
        });

        terminarPercursoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicio_percurso = false;
                distanciaInputTextView.setText(String.format(Locale.getDefault(),"%.1f KM",distancia/100000000));
                long elapsedMillis = SystemClock.elapsedRealtime() - tempoPercorridoChronometer.getBase();
                tempoPercorridoChronometer.stop();
                tempoPercorridoChronometer.setBase(SystemClock.elapsedRealtime());
                Toast.makeText(MainActivity.this, String.format(Locale.getDefault(),"Distancia Percorrida = %.1f KM\nTempo Percorrido = %s",
                        distancia/100000000,pegarTempo(elapsedMillis)), Toast.LENGTH_SHORT).show();
                distancia = 0.0;
                distanciaInputTextView.setText("0.0 KM");
            }
        });

        procurarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps_ativado){
                    String procurar = "geo:"+localidadeAnterior.getAltitude()+","+localidadeAnterior.getLongitude()+"?q="+procurarInputText.getText();
                    Uri uri = Uri.parse(String.format(Locale.getDefault(),procurar));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, "Ative o GPS", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String pegarTempo(long millis){
        return String.format(Locale.getDefault(),"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void iniciarVariaveis() {
        condecederPermisaoButton = findViewById(R.id.condecederPermisaoButton);
        ativarGPSButton = findViewById(R.id.ativarGPSButton);
        desativarGPSButton = findViewById(R.id.desativarGPSButton);
        iniciarPercursoButton = findViewById(R.id.iniciarPercursoButton);
        terminarPercursoButton = findViewById(R.id.terminarPercursoButton);
        procurarButton = findViewById(R.id.procurarButton);
        procurarInputText = findViewById(R.id.procurarInputText);

        localidadeAnterior = new Location("Distancia A");

        distanciaInputTextView = findViewById(R.id.distanciaInputTextView);
        tempoPercorridoChronometer = findViewById(R.id.tempoPercorridoChronometer);
        tempoPercorridoChronometer.setBase(SystemClock.elapsedRealtime());
        tempoPercorridoChronometer.stop();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
           public void onLocationChanged(Location location) {
                if(inicio_percurso){
                    if(localidadeAnterior!= null)
                        distancia += location.distanceTo(localidadeAnterior);
                    distanciaInputTextView.setText(String.format(Locale.getDefault(),"%.1f KM",distancia/100000000));
                    localidadeAnterior.setAltitude(location.getAltitude());
                    localidadeAnterior.setLongitude(location.getLongitude());
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MainActivity.this, provider, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void ativarGPS() {
        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            gps_ativado = true;
        }
        else{
            Toast.makeText(MainActivity.this, getString(R.string.conceda_permissao), Toast.LENGTH_SHORT).show();
        }
    }

    private void desativarGPS(){
        locationManager.removeUpdates(locationListener);
        gps_ativado = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_GPS_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                }
                else{
                    Toast.makeText(this, getString(R.string.sem_gps_nao_da), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
