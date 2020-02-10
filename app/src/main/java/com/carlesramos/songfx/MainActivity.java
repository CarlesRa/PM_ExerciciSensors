package com.carlesramos.songfx;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final int REQUEST_ACCESS_COURSE_LOCATION = 1;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 2;
    public enum Tipo {SENSOR, LOCATION}
    private Tipo tipo;
    private ArrayAdapter<String> adaptador;
    private ArrayList<String> items;
    private FusedLocationProviderClient fusedLocationClient;
    private Location ubicacionActual;

    private SensorManager mSensorManager;
    private Sensor giroSensor;
    private Sensor llumSensor;
    private float pitch;
    private MediaPlayer player;
    private MediaPlayer playerSampler;
    private PlaybackParams params;
    private WindowManager.LayoutParams windowParams;
    private Spinner spinner;
    private ImageButton ibMap;
    private ImageButton button;
    private Button btDrum;
    private Button btSnare;
    private Button btCharles;
    private SoundPool soundPool;
    private int drum;
    private int snare;
    private int hit;
    private boolean isPaused;
    private boolean isLocationAccepted;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLocationAccepted = true;
        getWindow().getDecorView().setBackgroundColor(Color.MAGENTA);

        //oculte la barra de notificacions
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        getSounds();

        spinner = findViewById(R.id.spinner);
        ibMap = findViewById(R.id.ibMap);
        button = findViewById(R.id.ivPlayPause);
        btDrum = findViewById(R.id.btKick);
        btSnare = findViewById(R.id.btSnare);
        btCharles = findViewById(R.id.btCharles);
        windowParams = getWindow().getAttributes();
        String[] valoresSpinner = {"Sampler", "Piano","Electronica"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spiner_item,valoresSpinner);


        ibMap.setOnClickListener(this);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        button.setOnClickListener(this);
        btDrum.setOnClickListener(this);
        btSnare.setOnClickListener(this);
        btCharles.setOnClickListener(this);

        pitch = 1f;
        isPaused = true;

        //SensorManager
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        //Sensor giroscopio
        giroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, giroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        //Sensor de llum
        llumSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, llumSensor, SensorManager.SENSOR_DELAY_NORMAL);


        player = MediaPlayer.create(this, R.raw.tono400);
        player.setVolume(0.5f,0.5f);
        player.setLooping(true);

        items = new ArrayList<>();
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            params = new PlaybackParams();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COURSE_LOCATION:
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obtenerUltimaUbicacion();

                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()){
            case Sensor.TYPE_GYROSCOPE : {
                if(event.values[0] > 0.15f) {
                    pitch+=0.1;
                    if (pitch > 2f){
                        pitch = 2f;
                    }
                    params.setPitch(pitch);
                    player.setPlaybackParams(params);
                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                }else if(event.values[0] < -0.1f) {
                    pitch-=0.1;
                    if (pitch < 0.1f){
                        pitch = 0.1f;
                    }
                    params.setPitch(pitch);
                    player.setPlaybackParams(params);
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                }
                break;
            }
            case Sensor.TYPE_LIGHT :
                if (event.values[0] > 12f){
                    windowParams.screenBrightness = 0.1f;
                    getWindow().setAttributes(windowParams);
                    return;
                }
                else if (event.values[0] < 11f){
                    windowParams.screenBrightness = 1f;
                    getWindow().setAttributes(windowParams);
                    return;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivPlayPause :
                if (isPaused){
                    button.setImageResource(R.drawable.pause);
                    player.start();
                    isPaused = false;
                }
                else{
                    button.setImageResource(R.drawable.play);
                    player.pause();
                    isPaused = true;
                }
                break;
            case R.id.btKick:
                soundPool.play(drum,1f,1f,1,0, 0);
                break;
            case R.id.btSnare :
                soundPool.play(snare,1f,1f,1,0, 0);
                break;
            case R.id.btCharles :
                soundPool.play(hit,1f,1f,1,0, 0);
                break;
            case R.id.ibMap :

                boolean permiso = true;
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                if (ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    permiso = false;
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                            REQUEST_ACCESS_COURSE_LOCATION);
                }
                if(ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    permiso = false;
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                            REQUEST_ACCESS_FINE_LOCATION);
                }
                if(permiso) {
                    obtenerUltimaUbicacion();
                } else {
                    Log.d(getClass().getSimpleName(), "Sin permisos para obtener la ubicación");
                }
                break;
        }
    }

    public void obtenerUltimaUbicacion() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            tipo = Tipo.LOCATION;
                            ubicacionActual = location;
                            // En raras ocaciones la ubicación puede ser nula
                            if (location != null) {
                                Bundle bundle = new Bundle();
                                bundle.putDouble(DialogoMapView.LONGITUD, ubicacionActual.getLongitude());
                                bundle.putDouble(DialogoMapView.LATITUD, ubicacionActual.getLatitude());
                                bundle.putString(DialogoMapView.TEXTO_BOTON, "Aceptar");
                                DialogoMapView dialogoMapView = new DialogoMapView();
                                dialogoMapView.setArguments(bundle);
                                dialogoMapView.show(getSupportFragmentManager(), "error_dialog_mapview");
                            }
                        }
                    });
        } catch (SecurityException se) {
            Log.d(getClass().getSimpleName(), "Sin permisos para obtener la ubicación");
        }
    }

    private void getSounds(){
        AudioAttributes attributes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            soundPool = new
                    SoundPool.Builder().setAudioAttributes(attributes).build();
            soundPool.load(MainActivity.this, R.raw.tono400, 1);
            drum = soundPool.load(MainActivity.this, R.raw.drum,1);
            snare = soundPool.load(MainActivity.this, R.raw.snare,1);
            hit = soundPool.load(MainActivity.this, R.raw.hithat,1);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0 : {
                if (playerSampler != null){
                    playerSampler.pause();
                }
                break;
            }
            case 1 :
                if (playerSampler != null){
                    playerSampler.stop();
                }
                playerSampler = MediaPlayer.create(MainActivity.this, R.raw.piano);
                playerSampler.setVolume(0.4f, 0.4f);
                playerSampler.start();
                break;
            case 2 :
                if (playerSampler != null){
                    playerSampler.stop();
                }
                playerSampler = MediaPlayer.create(MainActivity.this, R.raw.electronica);
                playerSampler.setVolume(0.4f, 0.4f);
                playerSampler.start();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
