package com.carlesramos.songfx;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Copyright 2019 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * NotificacionesTest
 *
 * @author Germán Gascón
 * @version 0.1, 2019-03-04
 * @since 0.1
 **/

public class DialogoMapView extends DialogFragment implements DialogInterface.OnClickListener, OnMapReadyCallback {
    //Claves para el Bundle
    public static final String LATITUD = "latitud";
    public static final String LONGITUD = "longitud";
    public static final String TEXTO_BOTON = "button";

    private TextView tvLatitud;
    private TextView tvLongitud;
    private MapView mapView;
    private GoogleMap gmap;
    private double latitud;
    private double longitud;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialogo_mapview, null);
        builder.setView(layout);
        tvLatitud = layout.findViewById(R.id.tvLatitud);
        tvLongitud = layout.findViewById(R.id.tvLongitud);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = layout.findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.onResume();
        mapView.getMapAsync(this);

        latitud = 0;
        longitud = 0;
        String boton = "Aceptar";
        if(arguments != null) {
            latitud = arguments.getDouble(LATITUD, 0);
            longitud = arguments.getDouble(LONGITUD, 0);
            boton = arguments.getString(TEXTO_BOTON, "Aceptar");
        }
        builder.setTitle("Ubicación: " + String.format("%.2f, %.2f", latitud, longitud));
        tvLatitud.setText(String.format("%.2f",latitud));
        tvLongitud.setText(String.format("%.2f",longitud));
        builder.setPositiveButton(boton, this);
        return builder.create();
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Log.i("Diálogos", "Aceptar");
        dialogInterface.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(6);
        LatLng ny = new LatLng(latitud, longitud);
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(ny,7));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(ny);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        markerOptions.getPosition();
        gmap.addMarker(markerOptions);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
}
