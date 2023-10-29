package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCategorias;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Categoria;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Negocio;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.ProductoAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class EntradaActivity extends AppCompatActivity implements ProductoAdapter.OnProductSelectedListener {

    private ImageButton btnBackToLogin;
    private ImageButton btnNavigation;

    private RecyclerView recyclerViewProductos;
    private SearchView searchView;

    private ProductoAdapter adapter;
    private List<Producto> productosList;

    private AlertDialog popupDialog;
    private List<Producto> productosSeleccionados = new ArrayList<>();

    private int categoriaCount = 0;
    private String nombreNegocio;

    private View customTitle; // Declarar customTitle como una variable miembro
    private TextView toolbarTitle; // Declarar la variable para el título
    private LinearLayout categoryButtonContainer;
    private List<Long> categoriasSeleccionadas = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);

        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnNavigation = findViewById(R.id.btnNavigation);
        searchView = findViewById(R.id.searchView);
        recyclerViewProductos = findViewById(R.id.recyclerView);
        productosList = new ArrayList<>();

        // Abre el SearchView automáticamente
        searchView.setIconified(false);
        searchView.setQuery("", true); // El segundo parámetro (false) evita que se ejecute la búsqueda automáticamente

        // Encuentra el contenedor de botones de categoría en tu diseño
        categoryButtonContainer = findViewById(R.id.categoryButtonContainer);

        // Infla el diseño personalizado para el título centrado
        customTitle = getLayoutInflater().inflate(R.layout.custom_toolbar_title, null);

        // Encuentra el título dentro del diseño personalizado
        toolbarTitle = customTitle.findViewById(R.id.toolbar_title);

        // Configura el título del negocio (asegúrate de que tengas una función obtenerNombreNegocio definida)
        obtenerNombreNegocio();

        // Inicializa el adaptador para el RecyclerView de búsqueda
        adapter = new ProductoAdapter(this, productosList, this); // Pasa "this" como la referencia a EntradaActivity
        recyclerViewProductos.setAdapter(adapter);

        // Configura el RecyclerView con el adaptador
        recyclerViewProductos.setLayoutManager(new GridLayoutManager(this, 2));

        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        TextView toolbarTitle = customTitle.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(nombreNegocio);

        // Configura el título del negocio (asegúrate de que tengas una función obtenerNombreNegocio definida)
        obtenerNombreNegocio();

        obtenerProductosDelNegocio(userNegocioId);

        // Realiza una llamada a la API para obtener las categorías
        Log.d("EntradaActivity", "Obteniendo categorías...");
        obtenerCategorias(userNegocioId);

        Log.d("EntradaActivity", "Actividad de entrada creada correctamente.");
        Log.d("EntradaActivity", "Número de productos cargados: " + productosList.size());

        // Configuración del botón de navegación
        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarPopupMisDatos();
            }
        });

        // Configuración del botón de retroceso
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(EntradaActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        // Configuración del SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterProductos(newText);
                return true;
            }
        });

        // Ejemplo de cómo configurar una barra de herramientas personalizada
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            // Establece el diseño personalizado como vista de título en la barra de herramientas
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(customTitle);
        }

        // Obtén los datos del usuario desde SharedPreferences
        SharedPreferences sharedPreferences2 = getSharedPreferences("UserDataUser", MODE_PRIVATE);

        String userName = sharedPreferences.getString("userName", "");
        Log.d("UserData", "User Name: " + userName);

        // Encuentra el TextView
        TextView welcomeText = findViewById(R.id.userText);

// Configura el mensaje de bienvenida
        if (!userName.isEmpty()) {
            String welcomeMessage = "¡Hola, " + userName + "!";
            welcomeText.setText(welcomeMessage);
            welcomeText.setVisibility(View.VISIBLE); // Muestra el TextView
        }


    }

    private void obtenerProductosDelNegocio(Long userNegocioId) {
        ApiServiceProductos apiService = ConfigApi.getInstanceProducto(this);

        Call<List<Producto>> call = apiService.getProductosPorNegocio(userNegocioId);

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    List<Producto> productos = response.body();

                    // Borra la lista de productos existente
                    productosList.clear();

                    // Agrega solo los primeros 10 productos
                    int maxProductos = Math.min(productos.size(), 10);
                    for (int i = 0; i < maxProductos; i++) {
                        productosList.add(productos.get(i));
                    }

                    // Notifica al adaptador sobre los cambios
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API Response ProductosDelNegocio", "Respuesta no exitosa: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);
            }
        });
    }


    private void mostrarPopupMisDatos() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setPositiveButton("Cerrar Ventana", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (popupDialog != null && popupDialog.isShowing()) {
                    popupDialog.dismiss();
                }
            }
        });

        alertDialogBuilder.setNeutralButton("Ver Mis Datos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                verMisDatos();
            }
        });

        alertDialogBuilder.setNegativeButton("Ver Compras", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                verCompras();
            }
        });

        popupDialog = alertDialogBuilder.create();
        popupDialog.show();
    }

    // Método para manejar el clic del botón "Ver Mis Datos"
    private void verMisDatos() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    // Método para manejar el clic del botón "Ver Negocios"
    private void verNegocios() {
        Intent mainIntentN = new Intent(this, NegociosActivity.class);
        startActivity(mainIntentN);
    }

    // Método para manejar el clic del botón "Ver Compras"
    private void verCompras() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", -1);

        Intent mainIntentN = new Intent(this, ListadoDeComprasActivity.class);
        mainIntentN.putExtra("userId", userId);
        startActivity(mainIntentN);
    }

    private void obtenerNombreNegocio() {
        ApiServiceNegocio apiServiceNegocio = ConfigApi.getInstanceNegocio(this);

        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        Call<Negocio> call = apiServiceNegocio.getNegocioById(userNegocioId);

        call.enqueue(new Callback<Negocio>() {
            @Override
            public void onResponse(Call<Negocio> call, Response<Negocio> response) {
                if (response.isSuccessful()) {
                    Negocio negocio = response.body();
                    if (negocio != null) {
                        nombreNegocio = negocio.getNombre(); // Asigna el nombre a la variable global

                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayShowTitleEnabled(false);
                        }
                        if (toolbarTitle != null) {
                            toolbarTitle.setText("Bienvenido a " + nombreNegocio);
                        }
                        // Agrega un log para verificar el nombre del negocio
                        Log.d("EntradaActivity", "Nombre del negocio: " + nombreNegocio);
                    }
                }
            }

            @Override
            public void onFailure(Call<Negocio> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);
            }
        });
    }


    // Implementación del método de la interfaz para recibir productos seleccionados
    @Override
    public void onProductSelected(Producto producto) {
        productosSeleccionados.add(producto);
    }

    // Método para obtener categorías
    private void obtenerCategorias(Long userNegocioId) {
        ApiServiceCategorias apiServiceCategorias = ConfigApi.getInstanceCategorias(this);

        Call<List<Categoria>> call = apiServiceCategorias.getAllCategorias();

        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    List<Categoria> categorias = response.body();

                    for (Categoria categoria : categorias) {
                        if (categoria.getNegocioId().equals(userNegocioId)) {
                            Button categoryButton = new Button(EntradaActivity.this);
                            categoryButton.setText(categoria.getNombre());

                            // Establece el fondo programáticamente
                            categoryButton.setBackgroundResource(R.drawable.categoria_button_background);

                            categoryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d("Categoría seleccionada", "ID: " + categoria.getId() + ", Nombre: " + categoria.getNombre());
                                    Intent categoriaIntent = new Intent(EntradaActivity.this, CategoriaProductosActivity.class);
                                    categoriaIntent.putExtra("categoriaSeleccionada", categoria.getId());
                                    startActivity(categoriaIntent);
                                }
                            });

                            categoryButtonContainer.addView(categoryButton);
                        }
                    }

                } else {
                    Log.e("API Response CATEGORIAS", "Respuesta no exitosa: " + response.code());
                    Log.e("API Response", "Estado del servidor: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);
            }
        });
    }

}