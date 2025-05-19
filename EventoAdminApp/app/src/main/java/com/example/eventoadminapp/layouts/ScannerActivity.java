package com.example.eventoadminapp.layouts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.example.eventoadminapp.R;
import com.example.eventoadminapp.dao.EventoDAO;
import com.example.eventoadminapp.dao.ParticipanteDAO;
import com.example.eventoadminapp.dao.PontoDAO;
import com.example.eventoadminapp.model.Evento;
import com.example.eventoadminapp.model.Participante;
import com.example.eventoadminapp.model.Ponto;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.google.zxing.ResultPoint;

import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Activity responsável por escanear QR Codes e registrar entradas/saídas
 * de participantes em um evento.
 */
public class ScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private DecoratedBarcodeView barcodeView;
    private String tipo; // "ENTRADA" ou "SAIDA"
    private ParticipanteDAO participanteDAO;
    private PontoDAO pontoDAO;
    private EventoDAO eventoDAO;
    private Evento eventoAtivo;

    /**
     * Chamado quando a activity é criada pela primeira vez. Inicializa os DAOs, verifica os dados do evento,
     * solicita permissão de câmera e inicia o scanner.
     *
     * @param savedInstanceState Bundle contendo o estado anterior da Activity, ou null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        participanteDAO = new ParticipanteDAO(this);
        pontoDAO        = new PontoDAO(this);
        eventoDAO       = new EventoDAO(this);

        tipo     = getIntent().getStringExtra("tipo");
        int eventoId = getIntent().getIntExtra("eventoId", -1);

        if (eventoId == -1 || tipo == null) {
            Toast.makeText(this, "Dados de evento inválidos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        eventoAtivo = eventoDAO.buscarEventoPorId(eventoId);
        if (eventoAtivo == null) {
            Toast.makeText(this, "Evento não encontrado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setTitle(( "ENTRADA".equals(tipo) ? "Registrar Entrada - " : "Registrar Saída - " )
                 + eventoAtivo.getNome());

        barcodeView = findViewById(R.id.zxing_barcode_scanner);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.CAMERA },
                    CAMERA_PERMISSION_REQUEST
            );
        } else {
            iniciarScanner();
        }
    }

    /**
     * Inicia o scanner configurando leitura contínua de códigos.
     */
    private void iniciarScanner() {
        barcodeView.decodeContinuous(callback);
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.pause();
                processarQRCode(result.getText());
            }
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) { }
    };

    /**
     * Processa o texto lido do QR Code, validando formato e registrando o ponto.
     *
     * @param qrCodeText Texto completo extraído do QR Code.
     */
    private void processarQRCode(String qrCodeText) {
        String[] partes = qrCodeText.split(",");
        if (partes.length < 2) {
            mostrarErro("Formato de QR Code inválido.");
            return;
        }

        String nomePart = partes[0].replace("Nome: ", "").trim();
        String rgmPart  = partes[1].replace("RGM: ", "").trim();

        if (nomePart.isEmpty() || rgmPart.isEmpty()) {
            mostrarErro("Informações do QR Code estão incompletas.");
            return;
        }

        int rgm;
        try {
            rgm = Integer.parseInt(rgmPart);
        } catch (NumberFormatException e) {
            mostrarErro("RGM inválido no QR Code.");
            return;
        }

        Participante participante = participanteDAO.buscarParticipantePorRgm(rgm);
        if (participante == null) {
            participante = new Participante();
            participante.setNome(nomePart);
            participante.setRgm(rgm);
            long ins = participanteDAO.inserirParticipante(participante);
            if (ins == -1) {
                mostrarErro("Erro ao adicionar novo participante.");
                return;
            }
            participante = participanteDAO.buscarParticipantePorRgm(rgm);
            if (participante == null) {
                mostrarErro("Erro ao recuperar participante após inserção.");
                return;
            }
        }

        String ultimoTipo = pontoDAO.buscarUltimoTipoPonto(
                eventoAtivo.getId(), participante.getId()
        );

        if (eventoAtivo.isEventoSimples()) {
            if (!"ENTRADA".equals(tipo)) {
                mostrarErro("Este evento é simples e só permite registrar entradas.");
                return;
            }
            registrarEntradaSimples(participante);
        } else {
            if ("ENTRADA".equals(tipo)) {
                if ("ENTRADA".equals(ultimoTipo)) {
                    mostrarErro("O participante já registrou uma entrada.");
                    return;
                }
                verificarERegistrarEntrada(participante);
            } else {
                if (!"ENTRADA".equals(ultimoTipo)) {
                    mostrarErro("Não há entrada registrada para este participante.");
                    return;
                }
                registrarSaida(participante);
            }
        }
    }

    /**
     * Verifica limites e registra entrada conforme tipo de contagem do evento.
     *
     * @param participante Participante para o qual será registrado o ponto.
     */
    private void verificarERegistrarEntrada(Participante participante) {
        if (eventoAtivo.isContagemUnica()) {
            boolean ja = pontoDAO.verificarSeParticipou(
                eventoAtivo.getId(), participante.getId()
            );
            if (!ja) {
                int totalUnicos = pontoDAO.contarParticipantesUnicos(eventoAtivo.getId());
                if (eventoAtivo.getMaxParticipantes() != 0
                        && totalUnicos >= eventoAtivo.getMaxParticipantes()) {
                    mostrarErro("Limite de participantes atingido.");
                    return;
                }
            }
        } else {
            int totalEntradas = pontoDAO.contarTotalEntradas(eventoAtivo.getId());
            if (eventoAtivo.getMaxParticipantes() != 0
                    && totalEntradas >= eventoAtivo.getMaxParticipantes()) {
                mostrarErro("Limite de participantes atingido.");
                return;
            }
        }
        registrarEntrada(participante);
    }

    /**
     * Registra entrada para evento simples (sem saídas).
     *
     * @param participante Participante a ser registrado.
     */
    private void registrarEntradaSimples(Participante participante) {
        boolean ja = pontoDAO.verificarParticipanteRegistrado(
            eventoAtivo.getId(), participante.getId()
        );
        if (ja) {
            mostrarErro("Participante já registrado.");
            return;
        }
        verificarERegistrarEntrada(participante);
    }

    /**
     * Cria e insere um registro de entrada no banco.
     *
     * @param participante Participante para o qual registra a entrada.
     */
    private void registrarEntrada(Participante participante) {
        String dataHora = DateFormat.format(
            "dd/MM/yyyy HH:mm:ss", new Date()
        ).toString();
        Ponto ponto = new Ponto();
        ponto.setParticipanteId(participante.getId());
        ponto.setEventoId(eventoAtivo.getId());
        ponto.setDataHora(dataHora);
        ponto.setTipo("ENTRADA");
        long res = pontoDAO.inserirPonto(ponto);
        if (res != -1) {
            mostrarSucesso("Entrada registrada: " + participante.getNome());
        } else {
            mostrarErro("Erro ao registrar entrada.");
        }
    }

    /**
     * Cria e insere um registro de saída no banco.
     *
     * @param participante Participante para o qual registra a saída.
     */
    private void registrarSaida(Participante participante) {
        String dataHora = DateFormat.format(
            "dd/MM/yyyy HH:mm:ss", new Date()
        ).toString();
        Ponto ponto = new Ponto();
        ponto.setParticipanteId(participante.getId());
        ponto.setEventoId(eventoAtivo.getId());
        ponto.setDataHora(dataHora);
        ponto.setTipo("SAIDA");
        long res = pontoDAO.inserirPonto(ponto);
        if (res != -1) {
            mostrarSucesso("Saída registrada: " + participante.getNome());
        } else {
            mostrarErro("Erro ao registrar saída.");
        }
    }

    /**
     * Exibe mensagem de erro em Snackbar e reinicia o scanner.
     *
     * @param mensagem Texto a ser exibido.
     */
    private void mostrarErro(String mensagem) {
        Snackbar.make(barcodeView, mensagem, Snackbar.LENGTH_LONG).show();
        barcodeView.resume();
    }

    /**
     * Exibe mensagem de sucesso em Snackbar e reinicia o scanner.
     *
     * @param mensagem Texto a ser exibido.
     */
    private void mostrarSucesso(String mensagem) {
        Snackbar.make(barcodeView, mensagem, Snackbar.LENGTH_SHORT).show();
        barcodeView.resume();
    }

    /**
     * Reinicia o scanner ao voltar ao estado resumido da Activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    /**
     * Pausa o scanner quando a Activity entra em pausa.
     */
    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    /**
     * Callback para resultado de permissão de câmera. Inicia scanner ou finaliza.
     *
     * @param requestCode  Código da requisição de permissão.
     * @param permissions  Array de permissões solicitadas.
     * @param grantResults Resultados de permissão (PackageManager.GRANTED ou DENIED).
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarScanner();
            } else {
                Toast.makeText(
                    this,
                    "Permissão da câmera negada. Não é possível escanear QR Codes.",
                    Toast.LENGTH_LONG
                ).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
