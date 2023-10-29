package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiService;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonSignIn;

    private static final int REQUEST_INTERNET_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa las vistas
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);

        // Verifica si tienes permiso de acceso a Internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
        }

        // Configura el evento click para el botón "Iniciar Sesión"
        buttonSignIn.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (validateInput(email, password)) {
                performSignIn(email, password);
            }
        });

        ImageView togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        TextInputEditText editTextPassword = findViewById(R.id.editTextPassword);

// Configura un listener para alternar la visibilidad de la contraseña
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Cambiar a texto claro
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    // Cambiar a contraseña oculta
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, completa todos los campos");
            return false;
        }
        return true;
    }

    private void performSignIn(String email, String password) {
        ApiService apiService = ConfigApi.getInstance(this);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        Call<User> call = apiService.signIn(user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    Long userId = user.getId();
                    String authToken = user.getToken();
                    ConfigApi.setAuthToken(authToken);  // Establecer el token manualmente

                    // Continuar con el manejo del inicio de sesión
                    handleSuccessfulLogin(user);
                } else {
                    handleLoginFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleNetworkError(t);
            }
        });
    }

    private void handleSuccessfulLogin(User user) {
        if (user != null && user.getId() != -1 && user.getNegocioId() != -1) {
            // Guarda los datos del usuario en SharedPreferences
            saveUserData(user);

            // Llama al método para establecer el token en ConfigApi
            ConfigApi.setAuthToken(user.getToken());

            // Configura Retrofit y obtén una instancia de ApiServiceNegocio
            ApiServiceNegocio apiServiceNegocio = ConfigApi.getInstanceNegocio(this);

            // Realiza las solicitudes utilizando apiServiceNegocio
            // ...

            Log.d("LoginActivity", "Token recibido: " + user.getToken());

            // Envía los datos del usuario a MainActivity
            sendUserDataToMainActivity(user);

            // Muestra un mensaje y navega a la siguiente actividad
            showToastAndNavigate("Inicio de sesión exitoso", EntradaActivity.class);


        } else {
            handleLoginFailure();
        }
    }


    private void handleLoginFailure() {
        showToast("Inicio de sesión fallido");
    }

    private void handleNetworkError(Throwable t) {
        showToast("Error en la solicitud: " + t.getMessage());
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void showToastAndNavigate(String message, Class<?> targetActivity) {
        showToast(message);
        Intent intent = new Intent(LoginActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }

    private void sendUserDataToMainActivity(User user) {
        // Crea un Intent para MainActivity
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.putExtra("userEmail", user.getEmail());
        mainIntent.putExtra("userName", user.getNombre());
        mainIntent.putExtra("userApellido", user.getApellido());
        mainIntent.putExtra("userTelefono", user.getTelefono());
        mainIntent.putExtra("userId", user.getId());
        mainIntent.putExtra("userNegocioId", user.getNegocioId());

        // Inicia la actividad MainActivity
        startActivity(mainIntent);
    }


    private void saveUserData(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Guarda todos los atributos del usuario
        editor.putString("userToken", user.getToken());
        editor.putString("userEmail", user.getEmail());
        editor.putString("userName", user.getNombre());
        editor.putString("userApellido", user.getApellido());
        editor.putString("userTelefono", user.getTelefono());
        editor.putLong("userId", user.getId());
        editor.putLong("userNegocioId", user.getNegocioId());
        // Agrega más atributos según las propiedades de la clase User

        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_INTERNET_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso de Internet fue concedido, pero ya habíamos solicitado el inicio de sesión previamente.
                // No es necesario realizar otra solicitud de inicio de sesión aquí.
            } else {
                showToast("Permiso de Internet denegado. Por favor, concede el permiso para continuar.");
            }
        }
    }


    public void onClickSignUp(View view) {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
