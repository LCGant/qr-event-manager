package com.example.eventoadminapp.layouts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.eventoadminapp.R;

public class MainActivity extends AppCompatActivity {

    private Button buttonEntrada;
    private Button buttonSaida;
    private Button buttonAdicionarEvento;
    private Button buttonAdministrarEvento;

    /**
     * Chamado quando a atividade é criada pela primeira vez. Inicializa os botões da interface e seus listeners de clique.
     *
     * @param savedInstanceState Se a atividade está sendo reinicializada após ter sido encerrada anteriormente,
     *                           este Bundle contém os dados mais recentes fornecidos. Caso contrário, é nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialização das Views
        buttonEntrada            = findViewById(R.id.buttonEntrada);
        buttonSaida              = findViewById(R.id.buttonSaida);
        buttonAdicionarEvento    = findViewById(R.id.buttonAdicionarEvento);
        buttonAdministrarEvento  = findViewById(R.id.buttonAdministrarEvento);

        // Configura listeners
        buttonEntrada.setOnClickListener(v -> abrirSelecionarEvento("ENTRADA"));
        buttonSaida.setOnClickListener(v -> abrirSelecionarEvento("SAIDA"));
        buttonAdicionarEvento.setOnClickListener(v -> abrirAdicionarEvento());
        buttonAdministrarEvento.setOnClickListener(v -> abrirAdministrarEvento());
    }

    /**
     * Abre a tela de seleção de evento (entrada ou saída).
     *
     * @param tipo Tipo de ponto a ser registrado: "ENTRADA" ou "SAIDA".
     */
    private void abrirSelecionarEvento(String tipo) {
        Intent intent = new Intent(MainActivity.this, SelecionarEventoActivity.class);
        intent.putExtra("tipo", tipo);
        startActivity(intent);
    }

    /**
     * Abre a tela para adicionar um novo evento.
     */
    private void abrirAdicionarEvento() {
        Intent intent = new Intent(MainActivity.this, AdicionarEventoActivity.class);
        startActivity(intent);
    }

    /**
     * Abre a tela para administrar eventos existentes.
     */
    private void abrirAdministrarEvento() {
        Intent intent = new Intent(MainActivity.this, AdministrarEventoActivity.class);
        startActivity(intent);
    }
}
