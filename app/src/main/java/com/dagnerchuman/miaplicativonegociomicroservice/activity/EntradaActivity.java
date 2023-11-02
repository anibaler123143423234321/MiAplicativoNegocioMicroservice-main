package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.ProductoAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCategorias;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Categoria;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Negocio;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntradaActivity extends AppCompatActivity implements ProductoAdapter.OnProductSelectedListener {

    @Override
    public void onProductSelected(Producto producto) {
        if (producto.isSelected()) {
            productosSeleccionados.add(producto);
            Log.d("ProductoCarrito", "ID: " + producto.getId() + ", Nombre: " + producto.getNombre());
        } else {
            productosSeleccionados.remove(producto);
        }
    }

    private ImageButton btnBackToLogin, btnCarrito;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ProductoAdapter adapter;
    private List<Producto> productosList;
    private List<Producto> productList = new ArrayList();
    private ConstraintLayout constraintLayout;
    private AlertDialog popupDialog;
    private List<Producto> productosSeleccionados = new ArrayList<>();
    private int categoriaCount = 0;
    private String nombreNegocio;
    private View customTitle;
    private TextView toolbarTitle;
    private LinearLayout categoryButtonContainer;
    private List<Long> categoriasSeleccionadas = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);

        initViews();
        setupSearchView();
        setupRecyclerView();
        setupSwipeRefreshLayout();
        setupBottomNavigationView();
        setupCarritoButton();
        obtenerNombreNegocio();
        obtenerProductosDelNegocio();
        obtenerCategorias();

        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
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

    private void initViews() {
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnCarrito = findViewById(R.id.btnCarritoE);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        productosList = new ArrayList<>();
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        constraintLayout = findViewById(R.id.constraintLayoutE);
        categoryButtonContainer = findViewById(R.id.categoryButtonContainer);
        customTitle = getLayoutInflater().inflate(R.layout.custom_toolbar_title, null);
        toolbarTitle = customTitle.findViewById(R.id.toolbar_title);


    }

    private void setupSearchView() {
        searchView.setIconified(true);
        searchView.setQuery("", true);
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
    }

    private void setupRecyclerView() {
        adapter = new ProductoAdapter(this, productosList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            obtenerProductosDelNegocio();
            swipeRefreshLayout.setRefreshing(false);
        });
    }


    private void setupBottomNavigationView() {
        bottomNavigationView.setSelectedItemId(R.id.page_1);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.page_1) {
                return true;
            } else if (itemId == R.id.page_2) {
                verMisDatos();
                return true;
            } else if (itemId == R.id.page_3) {
                verCompras();
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupCarritoButton() {
        btnCarrito.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private float startX, startY;
            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isMoving = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        newX = Math.max(0, Math.min(newX, constraintLayout.getWidth() - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, constraintLayout.getHeight() - v.getHeight()));

                        v.setX(newX);
                        v.setY(newY);

                        if (Math.abs(event.getRawX() - startX) > 120 || Math.abs(event.getRawY() - startY) > 120) {
                            isMoving = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            navigateToCarritoActivity();
                        }
                        break;
                }
                return true;
            }
        });

        btnCarrito.setOnClickListener(view -> navigateToCarritoActivity());
    }

    private void navigateToCarritoActivity() {
        ArrayList<Producto> productosSeleccionados = new ArrayList<>();
        for (Producto producto : productList) {
            if (producto.isSelected()) {
                productosSeleccionados.add(producto);
            }
        }

        Intent carritoIntent = new Intent(EntradaActivity.this, CarritoActivityEntrada.class);
        carritoIntent.putExtra("productosEnCarritoE", productosSeleccionados);
        startActivity(carritoIntent);
    }

    private void verMisDatos() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void verCompras() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", -1);
        Intent mainIntentN = new Intent(this, ListadoDeComprasActivity.class);
        mainIntentN.putExtra("userId", userId);
        startActivity(mainIntentN);
    }

    private void obtenerNombreNegocio() {
        // Configura el servicio API y obtiene el ID del negocio del SharedPreferences
        ApiServiceNegocio apiServiceNegocio = ConfigApi.getInstanceNegocio(this);
        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        // Crea la llamada para obtener los datos del negocio
        Call<Negocio> call = apiServiceNegocio.getNegocioById(userNegocioId);

        // Obtiene el contexto de la actividad
        final Context context = this;

        // Realiza la llamada asíncrona a la API
        call.enqueue(new Callback<Negocio>() {
            @Override
            public void onResponse(Call<Negocio> call, Response<Negocio> response) {
                if (response.isSuccessful()) {
                    Negocio negocio = response.body();
                    if (negocio != null) {
                        nombreNegocio = negocio.getNombre();

                        // Configura el Toolbar con el nombre del negocio
                        Toolbar toolbar = findViewById(R.id.toolbar);
                        setSupportActionBar(toolbar);
                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayShowTitleEnabled(false); // Oculta el título predeterminado
                            actionBar.setDisplayShowCustomEnabled(true); // Habilita la vista personalizada del título
                            actionBar.setCustomView(customTitle); // Establece la vista personalizada como título
                            actionBar.setTitle(""); // Limpia cualquier título existente
                            toolbarTitle = customTitle.findViewById(R.id.toolbar_title);

                            if (nombreNegocio != null) {
                                toolbarTitle.setText("Bienvenido a " + nombreNegocio);
                            }
                        }
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


    private void obtenerProductosDelNegocio() {
        ApiServiceProductos apiService = ConfigApi.getInstanceProducto(this);
        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);
        Call<List<Producto>> call = apiService.getProductosPorNegocio(userNegocioId);

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    List<Producto> productos = response.body();
                    productList.addAll(productos);
                    adapter.notifyDataSetChanged();
                    int maxProductos = Math.min(productos.size(), 10);
                    for (int i = 0; i < maxProductos; i++) {
                        productosList.add(productos.get(i));
                    }
                    adapter = new ProductoAdapter(EntradaActivity.this, productosList);
                    recyclerView.setAdapter(adapter);
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

    private void obtenerCategorias() {
        ApiServiceCategorias apiServiceCategorias = ConfigApi.getInstanceCategorias(this);
        Call<List<Categoria>> call = apiServiceCategorias.getAllCategorias();

        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    List<Categoria> categorias = response.body();
                    for (Categoria categoria : categorias) {
                        if (categoria.getNegocioId().equals(userNegocioId)) {
                            Button categoryButton = new Button(EntradaActivity.this);
                            categoryButton.setText(categoria.getNombre());
                            categoryButton.setBackgroundResource(R.drawable.categoria_button_background);
                            categoryButton.setOnClickListener(view -> {
                                long categoriaId = categoria.getId();
                                SharedPreferences sharedPreferences = getSharedPreferences("CategoriaPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong("categoriaId", categoriaId);
                                editor.apply();
                                Intent categoriaIntent = new Intent(EntradaActivity.this, CategoriaProductosActivity.class);
                                startActivity(categoriaIntent);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cart) {
            Intent intent = new Intent(this, CarritoActivityEntrada.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
