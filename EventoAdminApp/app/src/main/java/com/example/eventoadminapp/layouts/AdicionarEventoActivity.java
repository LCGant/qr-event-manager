package com.example.eventoadminapp.layouts;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.example.eventoadminapp.R;
import com.example.eventoadminapp.dao.EventoDAO;
import com.example.eventoadminapp.model.Evento;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdicionarEventoActivity extends AppCompatActivity {

    private EditText editTextNomeEvento;
    private EditText editTextDataEvento;
    private EditText editTextDescricaoEvento;
    private RadioGroup radioGroupTipoEvento;
    private RadioGroup radioGroupContagemParticipantes;
    private Button buttonAdicionarEvento;

    private CheckBox checkBoxLimiteParticipantes;
    private TextInputLayout textInputLayoutMaxParticipantes;
    private EditText editTextMaxParticipantes;
    private LinearLayout layoutContagemParticipantes;

    /**
     * Chamado quando a atividade é criada pela primeira vez. Inicializa as views e listeners.
     *
     * @param savedInstanceState Se a atividade está sendo reinicializada após ter sido finalizada anteriormente,
     *                           este Bundle contém os dados mais recentes fornecidos. Caso contrário, é nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_evento);

        // Inicialização das Views
        editTextNomeEvento            = findViewById(R.id.editTextNomeEvento);
        editTextDataEvento            = findViewById(R.id.editTextDataEvento);
        editTextDescricaoEvento       = findViewById(R.id.editTextDescricaoEvento);
        radioGroupTipoEvento          = findViewById(R.id.radioGroupTipoEvento);
        radioGroupContagemParticipantes = findViewById(R.id.radioGroupContagemParticipantes);
        buttonAdicionarEvento         = findViewById(R.id.buttonAdicionarEvento);
        layoutContagemParticipantes   = findViewById(R.id.layoutContagemParticipantes);

        // Inicializar CheckBox e campo de limite de participantes
        checkBoxLimiteParticipantes   = findViewById(R.id.checkBoxLimiteParticipantes);
        textInputLayoutMaxParticipantes = findViewById(R.id.textInputLayoutMaxParticipantes);
        editTextMaxParticipantes      = findViewById(R.id.editTextMaxParticipantes);

        // Alterna visibilidade do campo de máximo de participantes
        checkBoxLimiteParticipantes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                textInputLayoutMaxParticipantes.setVisibility(View.VISIBLE);
            } else {
                textInputLayoutMaxParticipantes.setVisibility(View.GONE);
                editTextMaxParticipantes.setText("");
            }
        });

        // Padrões: evento completo + participantes únicos
        radioGroupTipoEvento.check(R.id.radioButtonEventoCompleto);
        radioGroupContagemParticipantes.check(R.id.radioButtonParticipantesUnicos);
        layoutContagemParticipantes.setVisibility(View.VISIBLE);

        // Alterna layout de contagem conforme tipo de evento
        radioGroupTipoEvento.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonEventoCompleto) {
                layoutContagemParticipantes.setVisibility(View.VISIBLE);
            } else {
                layoutContagemParticipantes.setVisibility(View.GONE);
            }
        });

        // DatePicker para o campo de data
        editTextDataEvento.setFocusable(false);
        editTextDataEvento.setOnClickListener(view -> mostrarDatePickerDialog());

        // Listener do botão Adicionar Evento
        buttonAdicionarEvento.setOnClickListener(view -> adicionarEvento());
    }

    /**
     * Exibe um DatePickerDialog e preenche o campo de data com o valor selecionado.
     */
    private void mostrarDatePickerDialog() {
        final Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                String dataSelecionada = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                editTextDataEvento.setText(dataSelecionada);
            }, ano, mes, dia);
        datePickerDialog.show();
    }

    /**
     * Coleta os dados do formulário, valida e insere um novo evento no banco.
     * Exibe mensagens de erro ou sucesso conforme o caso.
     */
    private void adicionarEvento() {
        String nome       = editTextNomeEvento.getText().toString().trim();
        String data       = editTextDataEvento.getText().toString().trim();
        String descricao  = editTextDescricaoEvento.getText().toString().trim();

        int selectedTipoId = radioGroupTipoEvento.getCheckedRadioButtonId();
        boolean eventoSimples = (selectedTipoId == R.id.radioButtonEventoSimples);

        if (TextUtils.isEmpty(nome)) {
            editTextNomeEvento.setError("Por favor, insira o nome do evento");
            return;
        }

        if (TextUtils.isEmpty(data)) {
            editTextDataEvento.setError("Por favor, insira a data do evento");
            return;
        }

        if (!isDataValida(data)) {
            editTextDataEvento.setError("Por favor, insira uma data válida no formato dd/MM/yyyy");
            return;
        }

        if (!isDataHojeOuFuturo(data)) {
            editTextDataEvento.setError("A data do evento deve ser hoje ou no futuro");
            return;
        }

        int maxParticipantes = 0; // 0 = ilimitado
        if (checkBoxLimiteParticipantes.isChecked()) {
            String maxStr = editTextMaxParticipantes.getText().toString().trim();
            if (TextUtils.isEmpty(maxStr)) {
                editTextMaxParticipantes.setError("Insira o número máximo de participantes");
                return;
            }
            maxParticipantes = Integer.parseInt(maxStr);
            if (maxParticipantes <= 0) {
                editTextMaxParticipantes.setError("Insira um número maior que zero");
                return;
            }
        }

        // Define tipo de contagem somente para eventos completos
        boolean contagemUnica = true;
        if (!eventoSimples) {
            int selectedContagemId = radioGroupContagemParticipantes.getCheckedRadioButtonId();
            contagemUnica = (selectedContagemId == R.id.radioButtonParticipantesUnicos);
        }

        Evento evento = new Evento();
        evento.setNome(nome);
        evento.setData(data);
        evento.setDescricao(descricao);
        evento.setEventoSimples(eventoSimples);
        evento.setMaxParticipantes(maxParticipantes);
        evento.setContagemUnica(contagemUnica);

        EventoDAO eventoDAO = new EventoDAO(this);
        long resultado = eventoDAO.inserirEvento(evento);

        if (resultado != -1) {
            Toast.makeText(this, "Evento adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            // Limpa formulário
            editTextNomeEvento.setText("");
            editTextDataEvento.setText("");
            editTextDescricaoEvento.setText("");
            radioGroupTipoEvento.check(R.id.radioButtonEventoCompleto);
            radioGroupContagemParticipantes.check(R.id.radioButtonParticipantesUnicos);
            checkBoxLimiteParticipantes.setChecked(false);
            textInputLayoutMaxParticipantes.setVisibility(View.GONE);
            layoutContagemParticipantes.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Erro ao adicionar evento", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verifica se uma string de data está no formato dd/MM/yyyy e é válida.
     *
     * @param data String representando a data.
     * @return true se a data for válida, false caso contrário.
     */
    private boolean isDataValida(String data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(data);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Verifica se a data informada é hoje ou está no futuro.
     *
     * @param data String de data no formato dd/MM/yyyy.
     * @return true se a data for hoje ou futura, false se for passada ou inválida.
     */
    private boolean isDataHojeOuFuturo(String data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date dataEvento = sdf.parse(data);
            Calendar hoje = Calendar.getInstance();
            hoje.set(Calendar.HOUR_OF_DAY, 0);
            hoje.set(Calendar.MINUTE, 0);
            hoje.set(Calendar.SECOND, 0);
            hoje.set(Calendar.MILLISECOND, 0);
            return dataEvento != null && !dataEvento.before(hoje.getTime());
        } catch (ParseException e) {
            return false;
        }
    }
}
