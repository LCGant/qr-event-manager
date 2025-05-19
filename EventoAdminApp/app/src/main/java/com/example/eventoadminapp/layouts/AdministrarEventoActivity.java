package com.example.eventoadminapp.layouts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ProgressBar;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Button;

import com.example.eventoadminapp.R;
import com.example.eventoadminapp.adapters.PontoAdapter;
import com.example.eventoadminapp.dao.EventoDAO;
import com.example.eventoadminapp.dao.ParticipanteDAO;
import com.example.eventoadminapp.dao.PontoDAO;
import com.example.eventoadminapp.model.Evento;
import com.example.eventoadminapp.model.Ponto;
import com.example.eventoadminapp.util.TxtExporter;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdministrarEventoActivity extends AppCompatActivity {

    private static final String TAG = "AdministrarEventoActivity";

    private TextView textViewInfoEvento;
    private RecyclerView recyclerViewPontos;
    private Button buttonGerarRelatorio;
    private Button buttonEditarEvento;
    private Button buttonExcluirEvento;
    private ProgressBar progressBar;
    private Spinner spinnerEventos;

    private EventoDAO eventoDAO;
    private PontoDAO pontoDAO;
    private ParticipanteDAO participanteDAO;

    private Evento eventoSelecionado;

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 200;

    // Executor para operações em background
    private Executor executor = Executors.newSingleThreadExecutor();

    private PontoAdapter pontoAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrar_evento);

        // Inicialização das Views utilizando findViewById
        textViewInfoEvento = findViewById(R.id.textViewInfoEvento);
        recyclerViewPontos = findViewById(R.id.recyclerViewPontos);
        buttonGerarRelatorio = findViewById(R.id.buttonGerarRelatorio);
        buttonEditarEvento = findViewById(R.id.buttonEditarEvento);
        buttonExcluirEvento = findViewById(R.id.buttonExcluirEvento);
        progressBar = findViewById(R.id.progressBar);
        spinnerEventos = findViewById(R.id.spinnerEventos);

        // Inicialização das DAOs
        eventoDAO = new EventoDAO(this);
        pontoDAO = new PontoDAO(this);
        participanteDAO = new ParticipanteDAO(this);

        // Configurar RecyclerView
        configurarRecyclerView();

        // Configurar o listener para o botão Gerar Relatório
        buttonGerarRelatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarEGerarRelatorio();
            }
        });

        // Configurar o listener para o botão Editar Evento
        buttonEditarEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exibirDialogoEditarEvento();
            }
        });

        // Configurar o listener para o botão Excluir Evento
        buttonExcluirEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarExcluirEvento();
            }
        });

        // Carregar todos os eventos e iniciar o processo de seleção
        carregarTodosOsEventos();
    }

    /**
     * Método para configurar o RecyclerView.
     * 
     * Este método define o LayoutManager, habilita o tamanho fixo e configura o adapter
     * com uma lista vazia de pontos e o ParticipanteDAO.
     * 
     * @see RecyclerView
     * @see LinearLayoutManager
     * @see PontoAdapter
     * @see ParticipanteDAO
     */
    private void configurarRecyclerView() {
        recyclerViewPontos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPontos.setHasFixedSize(true);
        pontoAdapter = new PontoAdapter(new ArrayList<>(), participanteDAO);
        recyclerViewPontos.setAdapter(pontoAdapter);
    }

    /** Método para carregar todos os eventos do banco de dados.
     * 
     * Este método busca todos os eventos registrados no banco de dados e configura o Spinner
     * com os nomes dos eventos. Se não houver eventos, exibe uma mensagem e finaliza a atividade.
     * 
     * @see EventoDAO#buscarTodosEventos()
     */
    private void carregarTodosOsEventos() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Evento> eventos = eventoDAO.buscarTodosEventos();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (eventos == null || eventos.isEmpty()) {
                            Toast.makeText(AdministrarEventoActivity.this, "Nenhum evento encontrado.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            // Configurar o Spinner com os eventos
                            configurarSpinnerEventos(eventos);
                        }
                    }
                });
            }
        });
    }

    /**
     * Método para configurar o Spinner com os eventos.
     * 
     * Este método recebe uma lista de eventos, extrai os nomes e datas, e configura o Spinner
     * para exibir essas informações. Também define um listener para lidar com a seleção de eventos.
     * 
     * @param eventos A lista de eventos a serem exibidos no Spinner.
     */
    private void configurarSpinnerEventos(final List<Evento> eventos) {
        // Criar uma lista de nomes de eventos para exibição no Spinner
        List<String> nomesEventos = new ArrayList<>();
        for (Evento evento : eventos) {
            nomesEventos.add(evento.getNome() + " - " + evento.getData());
        }

        // Criar um ArrayAdapter usando o layout padrão do Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomesEventos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventos.setAdapter(adapter);

        // Definir o listener para a seleção do Spinner
        spinnerEventos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                eventoSelecionado = eventos.get(position);
                verificarDataDoEvento();
                exibirInformacoesEvento();
                carregarPontos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Selecionar automaticamente o primeiro evento
        if (!eventos.isEmpty()) {
            spinnerEventos.setSelection(0);
        }
    }

    /**
     * Método para verificar a data do evento selecionado.
     * 
     * Este método valida a data do evento selecionado e exibe uma mensagem informando se a data
     * já passou, está no futuro ou é hoje. Utiliza SimpleDateFormat para garantir o formato correto.
     * 
     * @see SimpleDateFormat
     */
    private void verificarDataDoEvento() {
        String dataEventoStr = eventoSelecionado.getData();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false); // Para garantir que datas inválidas sejam rejeitadas

        try {
            Date dataEvento = sdf.parse(dataEventoStr);
            if (dataEvento == null) {
                throw new ParseException("Data inválida", 0);
            }

            // Obter a data atual sem componentes de tempo
            Calendar calEvento = Calendar.getInstance();
            calEvento.setTime(dataEvento);
            calEvento.set(Calendar.HOUR_OF_DAY, 0);
            calEvento.set(Calendar.MINUTE, 0);
            calEvento.set(Calendar.SECOND, 0);
            calEvento.set(Calendar.MILLISECOND, 0);

            Calendar calAtual = Calendar.getInstance();
            calAtual.setTime(new Date());
            calAtual.set(Calendar.HOUR_OF_DAY, 0);
            calAtual.set(Calendar.MINUTE, 0);
            calAtual.set(Calendar.SECOND, 0);
            calAtual.set(Calendar.MILLISECOND, 0);

            if (calEvento.before(calAtual)) {
                Snackbar.make(findViewById(R.id.administrarEventoLayout), "A data deste evento já passou.", Snackbar.LENGTH_LONG).show();
            } else if (calEvento.after(calAtual)) {
                Snackbar.make(findViewById(R.id.administrarEventoLayout), "A data deste evento está no futuro.", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(findViewById(R.id.administrarEventoLayout), "Este evento está acontecendo hoje!", Snackbar.LENGTH_LONG).show();
            }
        } catch (ParseException e) {
            Snackbar.make(findViewById(R.id.administrarEventoLayout), "Formato de data do evento inválido.", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Método para exibir as informações do evento selecionado.
     * 
     * Este método busca os detalhes do evento selecionado e exibe as informações relevantes
     * como nome, data, descrição e total de participantes. Utiliza um Executor para realizar
     * a operação em background e atualizar a UI na thread principal.
     * 
     * @see EventoDAO#buscarEventoPorId(int)
     * @see ParticipanteDAO#buscarTodosParticipantesPorEvento(int)
     */
    private void exibirInformacoesEvento() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final String info;
                if (eventoSelecionado.isEventoSimples()) {
                    int totalParticipantes = participanteDAO.buscarTodosParticipantesPorEvento(eventoSelecionado.getId()).size();
                    info = "Evento: " + eventoSelecionado.getNome() + "\n" +
                            "Data: " + eventoSelecionado.getData() + "\n" +
                            "Descrição: " + eventoSelecionado.getDescricao() + "\n\n" +
                            "Total de Participantes: " + totalParticipantes;
                } else {
                    // Verificar o tipo de contagem
                    boolean contagemUnica = eventoSelecionado.isContagemUnica();
                    String tipoContagemStr = contagemUnica ? "Participantes Únicos" : "Número de Entradas";

                    // Obter os dados relevantes
                    int totalParticipantesUnicos = pontoDAO.contarParticipantesUnicos(eventoSelecionado.getId());
                    int totalEntradas = pontoDAO.contarTotalEntradas(eventoSelecionado.getId());
                    int totalSaidas = pontoDAO.contarTotalSaidasPorEvento(eventoSelecionado.getId());

                    if (contagemUnica) {
                        // Para contagem única, não precisamos contar entradas e saídas
                        info = "Evento: " + eventoSelecionado.getNome() + "\n" +
                                "Data: " + eventoSelecionado.getData() + "\n" +
                                "Descrição: " + eventoSelecionado.getDescricao() + "\n\n" +
                                "Tipo de Contagem: " + tipoContagemStr + "\n" +
                                "Total de Participantes Únicos: " + totalParticipantesUnicos;
                    } else {
                        // Para contagem por entradas, exibimos os totais
                        info = "Evento: " + eventoSelecionado.getNome() + "\n" +
                                "Data: " + eventoSelecionado.getData() + "\n" +
                                "Descrição: " + eventoSelecionado.getDescricao() + "\n\n" +
                                "Tipo de Contagem: " + tipoContagemStr + "\n" +
                                "Total de Entradas: " + totalEntradas + "\n" +
                                "Total de Saídas: " + totalSaidas;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        textViewInfoEvento.setText(info);
                    }
                });
            }
        });
    }

    /**
     * Método para carregar os pontos do evento selecionado.
     * 
     * Este método busca os pontos associados ao evento selecionado no banco de dados
     * e atualiza o RecyclerView com os dados obtidos.
     * 
     * @see PontoDAO#buscarPontosPorEvento(int)
     * @param eventoSelecionado O evento para o qual os pontos serão carregados.
     */
    private void carregarPontos() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Ponto> pontos = pontoDAO.buscarPontosPorEvento(eventoSelecionado.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (pontos != null && !pontos.isEmpty()) {
                            pontoAdapter.setPontos(pontos);
                        } else {
                            pontoAdapter.setPontos(new ArrayList<>());
                            Snackbar.make(findViewById(R.id.administrarEventoLayout), "Nenhum ponto encontrado para este evento.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * Método para confirmar a geração do relatório e solicitar permissão, se necessário.
     * 
     * Este método exibe um Snackbar perguntando ao usuário se ele deseja realmente gerar o relatório.
     * Se o usuário confirmar, chama o método verificarPermissaoESalvarRelatorio().
     * 
     * @see #verificarPermissaoESalvarRelatorio()
     * @see #gerarRelatorioTxt()
     * @param buttonGerarRelatorio O botão que aciona a geração do relatório.
     * @throws SecurityException Se a permissão WRITE_EXTERNAL_STORAGE não for concedida.
     * @see Manifest.permission#WRITE_EXTERNAL_STORAGE
     */
    private void confirmarEGerarRelatorio() {
        Snackbar.make(buttonGerarRelatorio, "Deseja realmente gerar o relatório?", Snackbar.LENGTH_LONG)
                .setAction("Sim", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        verificarPermissaoESalvarRelatorio();
                    }
                }).show();
    }

    /**
     * Método para verificar a permissão de escrita no armazenamento e gerar o relatório TXT.
     * 
     * Este método verifica se a permissão WRITE_EXTERNAL_STORAGE foi concedida.
     * Se a permissão for concedida, chama o método gerarRelatorioTxt().
     * Caso contrário, solicita a permissão ao usuário.
     * 
     * @throws SecurityException Se a permissão WRITE_EXTERNAL_STORAGE não for concedida.
     * @see #gerarRelatorioTxt()
     * @see #WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST
     */
    private void verificarPermissaoESalvarRelatorio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            gerarRelatorioTxt();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST);
            } else {
                gerarRelatorioTxt();
            }
        }
    }

    /**
     * Método para gerar o relatório TXT considerando o tipo do evento.
     * 
     * @param eventoSelecionado O evento selecionado para o qual o relatório será gerado.
     * Este método verifica se o evento é simples ou completo e gera o relatório
     */
    private void gerarRelatorioTxt() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Ponto> pontos = pontoDAO.buscarPontosPorEvento(eventoSelecionado.getId());
                TxtExporter exporter = new TxtExporter(AdministrarEventoActivity.this);
                boolean sucesso = exporter.exportarPontosParaTxt(eventoSelecionado, pontos);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (sucesso) {
                            String fileName = "Relatorio_" + eventoSelecionado.getNome().replaceAll("\\s+", "_") + ".txt";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                Snackbar.make(buttonGerarRelatorio, "Relatório TXT gerado com sucesso!\nLocal: Downloads/" + fileName, Snackbar.LENGTH_LONG).show();
                            } else {
                                String caminho = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + fileName;
                                Snackbar.make(buttonGerarRelatorio, "Relatório TXT gerado com sucesso!\nLocal: " + caminho, Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(buttonGerarRelatorio, "Erro ao gerar relatório TXT.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    /**
     * exibirDialogoEditarEvento exibe um diálogo para editar os detalhes do evento selecionado.
     * 
     * Este método permite ao usuário modificar o nome, data, descrição, tipo de evento,
     * @param eventoSelecionado O evento que será editado.
     * contagem de participantes e limite de participantes.
     */
    private void exibirDialogoEditarEvento() {
        if (eventoSelecionado == null) {
            Toast.makeText(this, "Nenhum evento selecionado para editar.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Evento");

        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_editar_evento, null);
        final EditText inputNome = viewInflated.findViewById(R.id.editTextNomeEvento);
        final EditText inputData = viewInflated.findViewById(R.id.editTextDataEvento);
        final EditText inputDescricao = viewInflated.findViewById(R.id.editTextDescricaoEvento);
        final RadioGroup radioGroupTipoEvento = viewInflated.findViewById(R.id.radioGroupTipoEvento);
        final RadioGroup radioGroupContagemParticipantes = viewInflated.findViewById(R.id.radioGroupContagemParticipantes); // Novo RadioGroup
        final CheckBox checkBoxLimiteParticipantes = viewInflated.findViewById(R.id.checkBoxLimiteParticipantes);
        final EditText inputMaxParticipantes = viewInflated.findViewById(R.id.editTextMaxParticipantes);
        final LinearLayout layoutContagemParticipantes = viewInflated.findViewById(R.id.layoutContagemParticipantes); // Adicionado

        final boolean tipoOriginal = eventoSelecionado.isEventoSimples();
        final boolean contagemOriginal = eventoSelecionado.isContagemUnica();

        inputNome.setText(eventoSelecionado.getNome());
        inputData.setText(eventoSelecionado.getData());
        inputDescricao.setText(eventoSelecionado.getDescricao());

        // Selecionar o tipo de evento atual
        if (eventoSelecionado.isEventoSimples()) {
            radioGroupTipoEvento.check(R.id.radioButtonEventoSimples);
        } else {
            radioGroupTipoEvento.check(R.id.radioButtonEventoCompleto);
        }

        // Configurar CheckBox e EditText para o limite de participantes
        checkBoxLimiteParticipantes.setChecked(eventoSelecionado.getMaxParticipantes() > 0);
        inputMaxParticipantes.setVisibility(eventoSelecionado.getMaxParticipantes() > 0 ? View.VISIBLE : View.GONE);
        inputMaxParticipantes.setText(eventoSelecionado.getMaxParticipantes() > 0 ? String.valueOf(eventoSelecionado.getMaxParticipantes()) : "");

        // Configurar RadioGroup para contagem de participantes
        if (!eventoSelecionado.isEventoSimples()) {
            layoutContagemParticipantes.setVisibility(View.VISIBLE);
            radioGroupContagemParticipantes.setVisibility(View.VISIBLE);
            if (eventoSelecionado.isContagemUnica()) {
                radioGroupContagemParticipantes.check(R.id.radioButtonParticipantesUnicos);
            } else {
                radioGroupContagemParticipantes.check(R.id.radioButtonNumeroEntradas);
            }
        } else {
            layoutContagemParticipantes.setVisibility(View.GONE);
            radioGroupContagemParticipantes.setVisibility(View.GONE);
        }

        checkBoxLimiteParticipantes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                inputMaxParticipantes.setVisibility(View.VISIBLE);
            } else {
                inputMaxParticipantes.setVisibility(View.GONE);
                inputMaxParticipantes.setText("");
            }
        });

        // Mostrar ou esconder opções de contagem com base na seleção do tipo de evento
        radioGroupTipoEvento.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonEventoCompleto) {
                layoutContagemParticipantes.setVisibility(View.VISIBLE);
                radioGroupContagemParticipantes.setVisibility(View.VISIBLE);
            } else {
                layoutContagemParticipantes.setVisibility(View.GONE);
                radioGroupContagemParticipantes.setVisibility(View.GONE);
            }
        });

        // Configurar o EditText para abrir o DatePickerDialog ao clicar
        inputData.setFocusable(false);
        inputData.setOnClickListener(view -> abrirDatePicker(inputData));

        builder.setView(viewInflated);
        builder.setPositiveButton("Salvar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        final AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String novoNome = inputNome.getText().toString().trim();
            String novaData = inputData.getText().toString().trim();
            String novaDescricao = inputDescricao.getText().toString().trim();
            int selectedId = radioGroupTipoEvento.getCheckedRadioButtonId();
            boolean eventoSimples = (selectedId == R.id.radioButtonEventoSimples);

            int maxParticipantes = 0; // 0 indica ilimitado
            if (checkBoxLimiteParticipantes.isChecked()) {
                String maxParticipantesStr = inputMaxParticipantes.getText().toString().trim();
                if (TextUtils.isEmpty(maxParticipantesStr)) {
                    inputMaxParticipantes.setError("Insira o número máximo de participantes");
                    return;
                }
                try {
                    maxParticipantes = Integer.parseInt(maxParticipantesStr);
                } catch (NumberFormatException e) {
                    inputMaxParticipantes.setError("Número inválido");
                    return;
                }
                if (maxParticipantes <= 0) {
                    inputMaxParticipantes.setError("Insira um número maior que zero para o limite");
                    return;
                }
            }

            boolean contagemUnica = true; // Por padrão, contagem única
            if (!eventoSimples) {
                int selectedContagemId = radioGroupContagemParticipantes.getCheckedRadioButtonId();
                if (selectedContagemId == R.id.radioButtonParticipantesUnicos) {
                    contagemUnica = true; // Participantes Únicos
                } else if (selectedContagemId == R.id.radioButtonNumeroEntradas) {
                    contagemUnica = false; // Número de Entradas
                }
            }

            final int maxParticipantesFinal = maxParticipantes;
            final boolean contagemUnicaFinal = contagemUnica;

            if (novoNome.isEmpty() || novaData.isEmpty()) {
                Toast.makeText(AdministrarEventoActivity.this, "Nome e data são obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalParticipantesAtuais;
            if (eventoSelecionado.isEventoSimples()) {
                totalParticipantesAtuais = participanteDAO.buscarTodosParticipantesPorEvento(eventoSelecionado.getId()).size();
            } else {
                if (contagemUnicaFinal) {
                    totalParticipantesAtuais = pontoDAO.contarParticipantesUnicos(eventoSelecionado.getId());
                } else {
                    totalParticipantesAtuais = pontoDAO.contarTotalEntradas(eventoSelecionado.getId());
                }
            }

            // Verificar se o número máximo é menor que o número atual de participantes
            if (maxParticipantesFinal > 0 && totalParticipantesAtuais > maxParticipantesFinal) {
                // Caso a contagem seja alterada para única, mas o número de registros seja maior que o limite estabelecido
                if (!contagemOriginal && contagemUnicaFinal) {
                    AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
                    alertaBuilder.setTitle("Erro ao Trocar Tipo de Contagem");
                    alertaBuilder.setMessage("Não foi possível trocar a contagem de participantes, pois existem mais registros do que o limite estabelecido.");
                    alertaBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                    alertaBuilder.show();
                } else {
                    // Caso normal de limite excedido para participantes únicos
                    AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
                    alertaBuilder.setTitle("Limite Excedido");
                    alertaBuilder.setMessage("O número máximo de participantes é inferior ao número máximo permitido para a configuração atual.");
                    alertaBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                    alertaBuilder.show();
                }
                return; // Interrompe a execução caso o limite de participantes seja excedido
            }

            if (contagemOriginal && !contagemUnicaFinal) {
                atualizarEvento(novoNome, novaData, novaDescricao, eventoSimples, maxParticipantesFinal, contagemUnicaFinal, dialog);
            } else if (!contagemOriginal && contagemUnicaFinal) {
                int participantesComMultiplesEntradas = pontoDAO.contarParticipantesComMultiplesEntradas(eventoSelecionado.getId());
                if (participantesComMultiplesEntradas > 0) {
                    AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
                    alertaBuilder.setTitle("Atenção");
                    alertaBuilder.setMessage("Existem " + participantesComMultiplesEntradas + " participante(s) com múltiplas entradas. " +
                            "Alterar o tipo de contagem para 'Participantes Únicos' removerá entradas duplicadas. Deseja prosseguir?");
                    alertaBuilder.setPositiveButton("Sim", (confirmarDialog, which) -> {
                        pontoDAO.apagarEntradasDuplicadasPorEvento(eventoSelecionado.getId());
                        atualizarEvento(novoNome, novaData, novaDescricao, eventoSimples, maxParticipantesFinal, contagemUnicaFinal, dialog);
                        confirmarDialog.dismiss();
                    });
                    alertaBuilder.setNegativeButton("Não", (confirmarDialog, which) -> confirmarDialog.dismiss());
                    alertaBuilder.show();
                } else {
                    atualizarEvento(novoNome, novaData, novaDescricao, eventoSimples, maxParticipantesFinal, contagemUnicaFinal, dialog);
                }
            } else {
                atualizarEvento(novoNome, novaData, novaDescricao, eventoSimples, maxParticipantesFinal, contagemUnicaFinal, dialog);
            }
        });
    }

    /**
     * Atualiza os detalhes do evento no banco de dados.
     *
     * @param novoNome         O novo nome do evento.
     * @param novaData         A nova data do evento.
     * @param novaDescricao    A nova descrição do evento.
     * @param eventoSimples    O novo tipo booleano do evento.
     * @param maxParticipantes O limite máximo de participantes.
     * @param contagemUnica    O tipo de contagem (true para Únicos, false para Entradas).
     * @param dialog           O diálogo atual que está sendo editado.
     */
    private void atualizarEvento(String novoNome, String novaData, String novaDescricao, boolean eventoSimples,
                                 int maxParticipantes, boolean contagemUnica, AlertDialog dialog) {
        eventoSelecionado.setNome(novoNome);
        eventoSelecionado.setData(novaData);
        eventoSelecionado.setDescricao(novaDescricao);

        boolean tipoOriginal = eventoSelecionado.isEventoSimples();
        boolean contagemOriginal = eventoSelecionado.isContagemUnica();
        eventoSelecionado.setEventoSimples(eventoSimples);
        eventoSelecionado.setMaxParticipantes(maxParticipantes);
        eventoSelecionado.setContagemUnica(contagemUnica);

        // Caso específico: Evento completo e mudança de "contagem única" para "número de entradas"
        if (!eventoSimples && contagemOriginal && !contagemUnica) {
            int totalEntradas = pontoDAO.contarTotalEntradas(eventoSelecionado.getId());

            // Verifica se o total de entradas excede o limite máximo estabelecido
            if (maxParticipantes > 0 && totalEntradas > maxParticipantes) {
                new AlertDialog.Builder(this)
                        .setTitle("Excedendo o Limite de Participantes")
                        .setMessage("O número de entradas atual (" + totalEntradas + ") excede o limite estabelecido de " + maxParticipantes + " participantes. Deseja continuar mesmo assim?")
                        .setPositiveButton("Sim", (confirmDialog, which) -> {
                            atualizarEventoNoBanco();
                            dialog.dismiss();
                            confirmDialog.dismiss();
                        })
                        .setNegativeButton("Não", (confirmDialog, which) -> confirmDialog.dismiss())
                        .show();
                return; // Interrompe a execução para aguardar a confirmação do usuário
            }
        }

        // Verificação padrão para conversão de evento completo para simples
        if (!tipoOriginal && eventoSimples) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Conversão para Evento Simples")
                    .setMessage("Ao converter para evento simples, todas as saídas e entradas duplicadas serão excluídas. Deseja continuar?")
                    .setPositiveButton("Sim", (confirmDialog, which) -> {
                        pontoDAO.converterParaEventoSimples(eventoSelecionado.getId());
                        atualizarEventoNoBanco();
                        dialog.dismiss();
                        confirmDialog.dismiss();
                    })
                    .setNegativeButton("Não", (confirmDialog, which) -> confirmDialog.dismiss())
                    .show();
        } else {
            atualizarEventoNoBanco();
            dialog.dismiss();
        }
    }

    /**
     * Atualizar os detalhes do evento no banco de dados.
     * 
     * Este método executa a atualização do evento selecionado no banco de dados
     * 
     * @see EventoDAO#atualizarEvento(Evento)
     * @param eventoSelecionado O evento que será atualizado.
     */
    private void atualizarEventoNoBanco() {
        progressBar.setVisibility(View.VISIBLE); // Mostrar o ProgressBar

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final boolean sucesso = eventoDAO.atualizarEvento(eventoSelecionado);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE); // Esconder o ProgressBar
                        if (sucesso) {
                            Snackbar.make(findViewById(R.id.administrarEventoLayout), "Evento atualizado com sucesso.", Snackbar.LENGTH_LONG).show();
                            exibirInformacoesEvento();
                            carregarPontos(); // Atualizar a lista de pontos após a atualização
                        } else {
                            Snackbar.make(findViewById(R.id.administrarEventoLayout), "Erro ao atualizar o evento.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 
     * Método para confirmar a exclusão do evento selecionado.
     * 
     * Este método exibe um AlertDialog perguntando ao usuário se ele tem certeza   
     * 
     */
    private void confirmarExcluirEvento() {
        if (eventoSelecionado == null) {
            Toast.makeText(this, "Nenhum evento selecionado para excluir.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Excluir Evento");
        builder.setMessage("Tem certeza que deseja excluir este evento?");
        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                excluirEventoDoBanco();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void excluirEventoDoBanco() {
        progressBar.setVisibility(View.VISIBLE); // Mostrar o ProgressBar

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final boolean sucesso = eventoDAO.excluirEvento(eventoSelecionado.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE); // Esconder o ProgressBar
                        if (sucesso) {
                            Snackbar.make(findViewById(R.id.administrarEventoLayout), "Evento excluído com sucesso.", Snackbar.LENGTH_LONG).show();
                            carregarTodosOsEventos();
                        } else {
                            Snackbar.make(findViewById(R.id.administrarEventoLayout), "Erro ao excluir o evento.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    /**
     * 
     * Método para lidar com o resultado da solicitação de permissões.
     * 
     * Este método é chamado quando o usuário responde à solicitação de permissão
     * 
     * @param requestCode O código da solicitação de permissão.
     * @param permissions As permissões solicitadas.
     * @param grantResults Os resultados da solicitação de permissão.
     * 
     * @see #WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST
     * 
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gerarRelatorioTxt();
            } else {
                Snackbar.make(buttonGerarRelatorio, "Permissão de escrita negada. Não é possível gerar relatórios.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Método para abrir o DatePickerDialog.
     *
     * @param editTextData O EditText onde a data selecionada será exibida.
     */
    private void abrirDatePicker(final EditText editTextData) {
        Calendar calendario = Calendar.getInstance();
        String currentDateStr = editTextData.getText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date currentDate = sdf.parse(currentDateStr);
            if (currentDate != null) {
                calendario.setTime(currentDate);
            }
        } catch (ParseException e) {
        }

        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AdministrarEventoActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.getTime());
                        editTextData.setText(formattedDate);
                    }
                }, ano, mes, dia);
        datePickerDialog.show();
    }
}
