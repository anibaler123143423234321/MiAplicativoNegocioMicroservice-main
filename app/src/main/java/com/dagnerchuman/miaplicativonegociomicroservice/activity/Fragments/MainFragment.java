package com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.dagnerchuman.miaplicativonegociomicroservice.R;

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        // Inicializa y muestra la interfaz de usuario aquí
        return rootView;
    }
}
