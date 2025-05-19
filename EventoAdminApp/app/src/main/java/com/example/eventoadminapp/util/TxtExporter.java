package com.example.eventoadminapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.eventoadminapp.model.Evento;
import com.example.eventoadminapp.model.Ponto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class TxtExporter {
    private static final String TAG = "TxtExporter";
    private Context context;

    public TxtExporter(Context context) {
        this.context = context;
    }

    /**
     * Exporta os pontos de um evento para um arquivo TXT na pasta Downloads.
     *
     * @param evento O evento para o qual o relatório será gerado.
     * @param pontos A lista de pontos (entradas e saídas) do evento.
     * @return true se a exportação for bem-sucedida, false caso contrário.
     */
    public boolean exportarPontosParaTxt(Evento evento, List<Ponto> pontos) {
        if (evento == null || pontos == null) {
            return false;
        }

        String fileName = "Relatorio_" + evento.getNome().replaceAll("\\s+", "_") + ".txt";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Utilizar MediaStore para Android 10 e acima
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                     OutputStreamWriter osWriter = new OutputStreamWriter(outputStream);
                     BufferedWriter writer = new BufferedWriter(osWriter)) {

                    escreverRelatorio(writer, evento, pontos);
                    writer.flush();
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao exportar para TXT: " + e.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "Falha ao obter URI para o arquivo.");
                return false;
            }
        } else {
            // Utilizar método tradicional para Android 9 e abaixo
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.e(TAG, "Armazenamento externo não está disponível.");
                return false;
            }

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File arquivo = new File(downloadsDir, fileName);

            try (OutputStream outputStream = new FileOutputStream(arquivo);
                 OutputStreamWriter osWriter = new OutputStreamWriter(outputStream);
                 BufferedWriter writer = new BufferedWriter(osWriter)) {

                escreverRelatorio(writer, evento, pontos);
                writer.flush();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Erro ao exportar para TXT: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Escreve o conteúdo do relatório no BufferedWriter.
     *
     * @param writer  O BufferedWriter para escrever o relatório.
     * @param evento  O evento para o qual o relatório está sendo gerado.
     * @param pontos  A lista de pontos do evento.
     * @throws IOException Se ocorrer um erro durante a escrita.
     */
    private void escreverRelatorio(BufferedWriter writer, Evento evento, List<Ponto> pontos) throws IOException {
        writer.append("Relatório do Evento: ").append(evento.getNome()).append("\n");
        writer.append("Data: ").append(evento.getData()).append("\n");
        writer.append("Descrição: ").append(evento.getDescricao()).append("\n\n");

        writer.append("ID | Participante | Data e Hora | Tipo\n");
        writer.append("-------------------------------------------\n");

        for (Ponto ponto : pontos) {
            String linha = ponto.getId() + " | " + ponto.getParticipanteId() + " | " + ponto.getDataHora() + " | " + ponto.getTipo() + "\n";
            writer.append(linha);
        }
    }
}
