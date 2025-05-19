package com.example.eventousuarioapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.eventousuarioapp.util.QRCodeGenerator;

/**
 * Activity principal que permite ao usuário inserir nome e RGM
 * e gerar um QR Code correspondente.
 */
public class MainActivity extends AppCompatActivity {

    private EditText editTextNome;
    private EditText editTextRgm;
    private Button buttonGerarQRCode;
    private ImageView imageViewQRCode;

    /**
     * Chamado na criação da Activity. Inicializa as views e listener.
     *
     * @param savedInstanceState Bundle com estado anterior, ou null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextNome        = findViewById(R.id.editTextNome);
        editTextRgm         = findViewById(R.id.editTextRGM);
        buttonGerarQRCode   = findViewById(R.id.buttonGerarQRCode);
        imageViewQRCode     = findViewById(R.id.imageViewQRCode);

        buttonGerarQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gerarQRCode();
            }
        });
    }

    /**
     * Lê os campos de nome e RGM, valida-os, formata o texto
     * e solicita ao QRCodeGenerator a criação do bitmap.
     * Exibe o resultado ou mensagem de erro.
     */
    private void gerarQRCode() {
        String nome = editTextNome.getText().toString().trim();
        String rgm  = editTextRgm.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            editTextNome.setError("Por favor, insira seu nome");
            return;
        }
        if (TextUtils.isEmpty(rgm)) {
            editTextRgm.setError("Por favor, insira seu RGM");
            return;
        }

        String qrCodeText = "Nome: " + nome + ", RGM: " + rgm;
        Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(qrCodeText, 500, 500);

        if (qrCodeBitmap != null) {
            imageViewQRCode.setImageBitmap(qrCodeBitmap);
            imageViewQRCode.setVisibility(View.VISIBLE);
            Toast.makeText(this, "QR Code gerado com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao gerar QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
