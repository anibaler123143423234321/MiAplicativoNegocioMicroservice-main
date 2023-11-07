package com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsuarioFragment extends Fragment {

    private TextView textViewEmail, textViewNombreApellido;
    private CircleImageView imageViewUserProfile;
    private CircleImageView buttonEditarFoto; // Cambiado a CircleImageView
    private static final int REQUEST_IMAGE_PICK = 1;
    private Uri selectedImageUri; // Almacena la URI de la imagen seleccionada

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Accede a la AppCompatActivity desde la Activity principal
        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Configura la Toolbar como la ActionBar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        // Inicializa el ImageView del botón de retroceso
        ImageView imageViewBack = view.findViewById(R.id.imageViewBackUser);

        // Inicializa el botón para editar la foto como CircleImageView
        //buttonEditarFoto = view.findViewById(R.id.buttonEditarFoto);

        // Agrega un OnClickListener al ImageView para volver atrás
        imageViewBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Inicializa los TextViews, el ImageView y el botón
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewNombreApellido = view.findViewById(R.id.textViewNombreApellido);
        imageViewUserProfile = view.findViewById(R.id.imageViewUserProfile);

        // Lee los datos del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserDataUser", requireActivity().MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "");
        String userName = sharedPreferences.getString("userName", "");
        String userApellido = sharedPreferences.getString("userApellido", "");
        String pictureUrl = sharedPreferences.getString("picture", "");

        // Muestra la información en los TextViews
        textViewEmail.setText(userEmail);
        textViewNombreApellido.setText(userName + " " + userApellido);

        // Cargar la imagen en el CircleImageView utilizando Picasso
        if (!pictureUrl.isEmpty()) {
            Picasso.get().load(pictureUrl).into(imageViewUserProfile);
        } else {
            // Si la URL de la imagen está vacía, puedes mostrar una imagen de marcador o un mensaje de error.
            imageViewUserProfile.setImageResource(R.drawable.image_not_found); // Reemplaza con tu imagen de marcador
        }

        // Configura el botón para cambiar la foto
//        buttonEditarFoto.setOnClickListener(v -> {
            // Abre la galería para permitir al usuario seleccionar una nueva foto de perfil
  //          Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    //        startActivityForResult(intent, REQUEST_IMAGE_PICK);
      //  });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            // Obtiene la URI de la imagen seleccionada desde la galería
            Uri imageUri = data.getData();

            // Actualiza la imagen de perfil con la nueva imagen seleccionada
            imageViewUserProfile.setImageURI(imageUri);

            // Guarda la nueva imagen de perfil en SharedPreferences u otro lugar si es necesario
        }
    }
}
