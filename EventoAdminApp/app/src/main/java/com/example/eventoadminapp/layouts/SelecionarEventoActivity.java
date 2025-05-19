package com.example.eventoadminapp.layouts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.example.eventoadminapp.R;
import com.example.eventoadminapp.adapters.EventoAdapter;
import com.example.eventoadminapp.dao.EventoDAO;
import com.example.eventoadminapp.model.Evento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity que exibe uma lista de eventos para selecionar e registrar
 * entradas ou saídas via QR code, conforme o tipo recebido ("ENTRADA" ou "SAIDA").
 */
public class SelecionarEventoActivity extends AppCompatActivity {

    private static final String TAG = "SelecionarEventoActivity";

    private RecyclerView recyclerViewEventos;
    private EventoAdapter eventoAdapter;
    private EventoDAO eventoDAO;
    private List<Evento> listaEventos;
    private String tipo; // "ENTRADA" ou "SAIDA"
    private FloatingActionButton fabAdicionarEvento;

    /**
     * Chamado ao criar a Activity. Inicializa views, DAO e carrega lista de eventos.
     *
     * @param savedInstanceState Bundle contendo o estado anterior da activity, ou null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_evento);

        recyclerViewEventos = findViewById(R.id.recyclerViewEventos);
        fabAdicionarEvento = findViewById(R.id.fabAdicionarEvento);
        eventoDAO = new EventoDAO(this);

        // Recebe o tipo de registro ("ENTRADA" ou "SAIDA") via Intent
        tipo = getIntent().getStringExtra("tipo");
        if (tipo == null || (!tipo.equals("ENTRADA") && !tipo.equals("SAIDA"))) {
            Toast.makeText(this, "Tipo de registro inválido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Carrega todos os eventos do banco
        listaEventos = eventoDAO.buscarTodosEventos();
        if (listaEventos.isEmpty()) {
            Toast.makeText(this,
                    "Nenhum evento encontrado. Por favor, adicione um evento primeiro.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Configura RecyclerView e adapter
        recyclerViewEventos.setLayoutManager(new LinearLayoutManager(this));
        eventoAdapter = new EventoAdapter(
            listaEventos,
            evento -> verificarTipoDoEvento(evento)
        );
        recyclerViewEventos.setAdapter(eventoAdapter);

        // FAB para adicionar novo evento
        fabAdicionarEvento.setOnClickListener(v -> {
            Intent intent = new Intent(
                SelecionarEventoActivity.this,
                AdicionarEventoActivity.class
            );
            startActivity(intent);
        });
    }

    /**
     * Atualiza a lista de eventos ao voltar para esta Activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        listaEventos = eventoDAO.buscarTodosEventos();
        eventoAdapter = new EventoAdapter(
            listaEventos,
            evento -> verificarTipoDoEvento(evento)
        );
        recyclerViewEventos.setAdapter(eventoAdapter);
    }

    /**
     * Verifica se o tipo de operação é permitido para o evento selecionado.
     * Se tudo OK, chama verificação de data.
     *
     * @param evento Evento clicado na lista.
     */
    private void verificarTipoDoEvento(Evento evento) {
        if ("SAIDA".equals(tipo) && evento.isEventoSimples()) {
            new AlertDialog.Builder(this)
                .setTitle("Operação Inválida")
                .setMessage("Este evento é simples e não permite registrar saídas.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        verificarDataDoEvento(evento);
    }

    /**
     * Verifica se a data do evento está no passado, presente ou futuro.
     * Exibe diálogo em caso de erro, ou inicia scanner se for hoje.
     *
     * @param evento Evento a ser verificado.
     */
    private void verificarDataDoEvento(Evento evento) {
        String dataEventoStr = evento.getData();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date dataEvento = sdf.parse(dataEventoStr);
            if (dataEvento == null) {
                mostrarErroDialog("Formato de data do evento inválido.");
                return;
            }

            Date dataAtual = removeTime(new Date());
            Date dataEventoSemHora = removeTime(dataEvento);

            Log.d(TAG, "Data do Evento: " + dataEventoSemHora);
            Log.d(TAG, "Data Atual: " + dataAtual);

            if (dataEventoSemHora.before(dataAtual)) {
                mostrarErroDialog(
                    "Este evento já ocorreu. Por favor, acesse 'Administrar Evento' para gerenciar."
                );
            } else if (dataEventoSemHora.after(dataAtual)) {
                mostrarErroDialog(
                    "A data deste evento ainda não chegou. " +
                    "Por favor, altere a data do evento em 'Administrar Evento'."
                );
            } else {
                iniciarScanner(evento);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Erro ao analisar a data do evento: " + e.getMessage());
            mostrarErroDialog("Formato de data do evento inválido.");
        }
    }

    /**
     * Remove hora, minuto, segundo e milissegundo de um Date, mantendo apenas ano/mês/dia.
     *
     * @param date Data completa.
     * @return Nova instância de Date com hora zerada.
     */
    private Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Mostra um diálogo simples de aviso com um título "Aviso".
     *
     * @param mensagem Texto a ser exibido no corpo do diálogo.
     */
    private void mostrarErroDialog(String mensagem) {
        new AlertDialog.Builder(this)
            .setTitle("Aviso")
            .setMessage(mensagem)
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Inicia a ScannerActivity, passando tipo e ID do evento por Intent,
     * e finaliza esta Activity.
     *
     * @param evento Evento selecionado para escanear.
     */
    private void iniciarScanner(Evento evento) {
        Intent intent = new Intent(
            SelecionarEventoActivity.this,
            ScannerActivity.class
        );
        intent.putExtra("tipo", tipo);
        intent.putExtra("eventoId", evento.getId());
        startActivity(intent);
        finish();
    }
}
