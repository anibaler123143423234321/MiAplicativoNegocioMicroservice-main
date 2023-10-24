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

                    for (int i = 0; i < productosEnCarrito.size(); i++) {
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

                        Call<Compra> call = apiServiceCompras.saveCompra(compra);

                        call.enqueue(new Callback<Compra>() {
                            @Override
                            public void onResponse(Call<Compra> call, Response<Compra> response) {
                                if (response.isSuccessful()) {
                                    Compra compraGuardada = response.body();
                                    Toast.makeText(CarritoActivity.this, "Compra realizada con éxito", Toast.LENGTH_SHORT).show();

                                    // Actualiza el stock del producto restando la cantidad deseada
                                    producto.setStock(producto.getStock() - cantidadDeseada);
                                    Call<Producto> updateCall = apiServiceProductos.actualizarProducto(producto.getId(), producto);
                                    updateCall.enqueue(new Callback<Producto>() {
                                        @Override
                                        public void onResponse(Call<Producto> call, Response<Producto> response) {
                                            if (response.isSuccessful()) {
                                                // El stock del producto se ha actualizado con éxito
                                            } else {
                                                // Maneja el error en la actualización del producto
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Producto> call, Throwable t) {
                                            // Maneja el error de la llamada al actualizarProducto
                                        }
                                    });

                                    Intent confirmationIntent = new Intent(CarritoActivity.this, EntradaActivity.class);
                                    startActivity(confirmationIntent);
                                    finish();
                                } else {
                                    Toast.makeText(CarritoActivity.this, "Error al realizar la compra", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Compra> call, Throwable t) {
                                Toast.makeText(CarritoActivity.this, "Error al realizar la compra", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(CarritoActivity.this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
