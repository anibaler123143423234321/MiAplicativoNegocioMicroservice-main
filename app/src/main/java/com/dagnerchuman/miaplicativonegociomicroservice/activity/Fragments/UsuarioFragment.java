package com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.EntradaActivity;

public class UsuarioFragment extends Fragment {

    private ImageButton btnMenu;
    private TextView textViewEmail, textViewNombre, textViewApellido, textViewTelefono, textViewUserId, textViewNegocioId, textdni;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Inicializa la Toolbar como ActionBar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Habilita el botón de retroceso en la Toolbar
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializa el ImageView del botón de retroceso
        ImageView imageViewBack = view.findViewById(R.id.imageViewBackUser);

        // Agrega un OnClickListener al ImageView para volver a EntradaActivity
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Configura los datos del usuario en el Intent
                Intent mainIntent = new Intent(requireActivity(), EntradaActivity.class);
                mainIntent.putExtra("userEmail", requireActivity().getIntent().getStringExtra("userEmail"));
                mainIntent.putExtra("userName", requireActivity().getIntent().getStringExtra("userName"));
                mainIntent.putExtra("dni", requireActivity().getIntent().getStringExtra("dni"));
                mainIntent.putExtra("userApellido", requireActivity().getIntent().getStringExtra("userApellido"));
                mainIntent.putExtra("userTelefono", requireActivity().getIntent().getStringExtra("userTelefono"));
                mainIntent.putExtra("userFechaCreacion", requireActivity().getIntent().getStringExtra("userFechaCreacion"));
                mainIntent.putExtra("userId", requireActivity().getIntent().getLongExtra("userId", -1));
                mainIntent.putExtra("userNegocioId", requireActivity().getIntent().getLongExtra("userNegocioId", -1));

                // Iniciar MainActivity con startActivityForResult
                requireActivity().startActivity(mainIntent);
                requireActivity().onBackPressed();

            }
        });

        // Inicializa los TextViews
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewNombre = view.findViewById(R.id.textViewNombre);
        textdni = view.findViewById(R.id.textdni);
        textViewApellido = view.findViewById(R.id.textViewApellido);
        textViewTelefono = view.findViewById(R.id.textViewTelefono);
        textViewUserId = view.findViewById(R.id.textViewUserId);
        textViewNegocioId = view.findViewById(R.id.textViewNegocioId);

        // Lee los datos del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserDataUser", requireActivity().MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "");
        String userName = sharedPreferences.getString("userName", "");
        String dni = sharedPreferences.getString("dni", "");
        String userApellido = sharedPreferences.getString("userApellido", "");
        String userTelefono = sharedPreferences.getString("userTelefono", "");
        Long userId = sharedPreferences.getLong("userId", -1);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        // Muestra la información en los TextViews
        textViewEmail.setText("Email del usuario: " + userEmail);
        textViewNombre.setText("Nombre del usuario: " + userName);
        textdni.setText("Dni del usuario: " + dni);
        textViewApellido.setText("Apellido del usuario: " + userApellido);
        textViewTelefono.setText("Teléfono del usuario: " + userTelefono);
        textViewUserId.setText("ID del usuario: " + userId);
        textViewNegocioId.setText("ID del negocio: " + userNegocioId);

        return view;
    }
}
