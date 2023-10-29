package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CarritoAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCompras;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Compra;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CarritoActivity extends AppCompatActivity {
    private List<Producto> productosEnCarrito;
    private List<Integer> cantidadesDeseadas;
    private Button btnFinalizarCompra;
    private CarritoAdapter carritoAdapter;
    private RecyclerView recyclerView;
    private Spinner spinnerTipoEnvio;
    private Spinner spinnerTipoDePago;
    private ImageButton btnBackToLogin;
    private ApiServiceCompras apiServiceCompras;
    private ApiServiceProductos apiServiceProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        productosEnCarrito = (List<Producto>) getIntent().getSerializableExtra("productosEnCarrito");
        apiServiceCompras = ConfigApi.getInstanceCompra(this);
        apiServiceProductos = ConfigApi.getInstanceProducto(this);

        recyclerView = findViewById(R.id.recyclerViewCarrito);
        carritoAdapter = new CarritoAdapter(this, productosEnCarrito);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carritoAdapter);

        spinnerTipoEnvio = findViewById(R.id.spinnerTipoEnvio);
        ArrayAdapter<CharSequence> tipoEnvioAdapter = ArrayAdapter.createFromResource(this, R.array.tipo_envio_array, android.R.layout.simple_spinner_item);
        tipoEnvioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoEnvio.setAdapter(tipoEnvioAdapter);

        spinnerTipoDePago = findViewById(R.id.spinnerTipoDePago);
        ArrayAdapter<CharSequence> tipoPagoAdapter = ArrayAdapter.createFromResource(this, R.array.tipo_pago_array, android.R.layout.simple_spinner_item);
        tipoPagoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDePago.setAdapter(tipoPagoAdapter);

        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(CarritoActivity.this, EntradaActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        cantidadesDeseadas = new ArrayList<>();
        for (int i = 0; i < productosEnCarrito.size(); i++) {
            cantidadesDeseadas.add(1);
        }

        // Recupera el userId desde SharedPreferences (ajusta el nombre de la preferencia y el valor por defecto según tus necesidades)
        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", 0L);  // 0L es el valor por defecto si no se encuentra la preferencia

        btnFinalizarCompra = findViewById(R.id.btnConfirmarCompra);
        btnFinalizarCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productosEnCarrito != null && !productosEnCarrito.isEmpty()) {
                    String tipoEnvio = spinnerTipoEnvio.getSelectedItem().toString();
                    String tipoDePago = spinnerTipoDePago.getSelectedItem().toString();
                    int totalCompras = productosEnCarrito.size();
                    final int[] comprasExitosas = {0}; // Variable final para contar compras exitosas

                    for (int i = 0; i < totalCompras; i++) {
                        Producto producto = productosEnCarrito.get(i);
                        int cantidadDeseada = cantidadesDeseadas.get(i);

                        Compra compra = new Compra();
                        compra.setUserId(userId);
                        compra.setProductoId(producto.getId());
                        compra.setTitulo(producto.getNombre());
                        compra.setPrecioCompra(producto.getPrecio());
                        compra.setTipoEnvio(tipoEnvio);
                        compra.setTipoDePago(tipoDePago);
                        compra.setCantidad(cantidadDeseada);

                        Call<Producto> stockCall = apiServiceProductos.getProductoById(producto.getId());

                        stockCall.enqueue(new Callback<Producto>() {
                            @Override
                            public void onResponse(Call<Producto> call, Response<Producto> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Producto productoActualizado = response.body();
                                    int stockDisponible = productoActualizado.getStock();

                                    // Verificar si la cantidad deseada es menor o igual al stock disponible
                                    if (cantidadDeseada <= stockDisponible) {
                                        Call<Compra> compraCall = apiServiceCompras.saveCompra(compra);

                                        compraCall.enqueue(new Callback<Compra>() {
                                            @Override
                                            public void onResponse(Call<Compra> call, Response<Compra> response) {
                                                if (response.isSuccessful()) {
                                                    // Compra exitosa
                                                    comprasExitosas[0]++;

                                                    // Actualizar el stock del producto
                                                    productoActualizado.setStock(stockDisponible - cantidadDeseada);
                                                    Call<Producto> updateCall = apiServiceProductos.actualizarProducto(producto.getId(), productoActualizado);

                                                    updateCall.enqueue(new Callback<Producto>() {
                                                        @Override
                                                        public void onResponse(Call<Producto> call, Response<Producto> response) {
                                                            if (response.isSuccessful()) {
                                                                // El stock del producto se ha actualizado con éxito
                                                            } else {
                                                                // Manejar errores en la actualización del producto
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<Producto> call, Throwable t) {
                                                            // Manejar errores en la llamada al actualizarProducto
                                                        }
                                                    });

                                                    // Si todas las compras son exitosas, redirige a la actividad de confirmación
                                                    if (comprasExitosas[0] == totalCompras) {
                                                        Toast.makeText(CarritoActivity.this, "Todas las compras se realizaron con éxito", Toast.LENGTH_SHORT).show();
                                                        Intent confirmationIntent = new Intent(CarritoActivity.this, EntradaActivity.class);
                                                        startActivity(confirmationIntent);
                                                        finish();
                                                    }
                                                } else {
                                                    // Manejar errores en la compra
                                                    Toast.makeText(CarritoActivity.this, "Error al realizar la compra", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Compra> call, Throwable t) {
                                                // Manejar errores en la llamada de compra
                                                Toast.makeText(CarritoActivity.this, "Error al realizar la compra", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        // Manejar errores cuando la cantidad deseada supera el stock
                                        Toast.makeText(CarritoActivity.this, "La cantidad deseada para '" + producto.getNombre() + "' supera el stock actual (" + stockDisponible + ")", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Manejar errores al obtener el stock del producto
                                    Toast.makeText(CarritoActivity.this, "Error al obtener el stock del producto", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Producto> call, Throwable t) {
                                // Manejar errores en la llamada para obtener el stock del producto
                                Toast.makeText(CarritoActivity.this, "Error al obtener el stock del producto", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    // Manejar errores cuando el carrito está vacío
                    Toast.makeText(CarritoActivity.this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
