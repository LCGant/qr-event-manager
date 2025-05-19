package com.example.eventoadminapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.eventoadminapp.model.Ponto;
import com.example.eventoadminapp.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável pelo acesso e manipulação dos registros de pontos (entradas e saídas)
 * dos participantes em eventos no banco de dados SQLite.
 *
 * Fornece métodos para inserir, consultar, contar e remover registros de pontos,
 * além de utilitários para relatórios e controle de duplicidade.
 *
 * Métodos principais:
 * <ul>
 *   <li>{@link #inserirPonto(Ponto)} - Insere um novo ponto (entrada ou saída) para um participante em um evento.</li>
 *   <li>{@link #buscarPontosPorEvento(int)} - Busca todos os pontos registrados em um evento.</li>
 *   <li>{@link #buscarUltimoTipoPonto(int, int)} - Retorna o último tipo de ponto (entrada/saída) de um participante em um evento.</li>
 *   <li>{@link #verificarParticipanteRegistrado(int, int)} - Verifica se um participante já registrou entrada em um evento.</li>
 *   <li>{@link #contarTotalEntradasPorEvento(int)} - Conta o número total de entradas em um evento.</li>
 *   <li>{@link #contarTotalSaidasPorEvento(int)} - Conta o número total de saídas em um evento.</li>
 *   <li>{@link #contarParticipantesSaidosPorEvento(int)} - Conta o número de participantes que saíram
 *
 */
public class PontoDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public PontoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Insere um novo ponto (entrada ou saída) para um participante em um evento.
     *
     * @param ponto Objeto Ponto a ser inserido.
     * @return ID do registro inserido.
     */
    public long inserirPonto(Ponto ponto) {
        ContentValues valores = new ContentValues();
        valores.put("PARTICIPANTE_ID", ponto.getParticipanteId());
        valores.put("EVENTO_ID", ponto.getEventoId());
        valores.put("DATA_HORA", ponto.getDataHora());
        valores.put("TIPO", ponto.getTipo());
        return db.insert("PONTOS", null, valores);
    }

    /**
     * Busca todos os pontos registrados em um evento.
     *
     * @param eventoId ID do evento.
     * @return Lista de pontos do evento.
     */
    public List<Ponto> buscarPontosPorEvento(int eventoId) {
        List<Ponto> pontos = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM PONTOS WHERE EVENTO_ID = ? ORDER BY DATA_HORA DESC",
                new String[]{String.valueOf(eventoId)});
        if (cursor.moveToFirst()) {
            do {
                Ponto ponto = new Ponto();
                ponto.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                ponto.setParticipanteId(cursor.getInt(cursor.getColumnIndexOrThrow("PARTICIPANTE_ID")));
                ponto.setEventoId(cursor.getInt(cursor.getColumnIndexOrThrow("EVENTO_ID")));
                ponto.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow("DATA_HORA")));
                ponto.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("TIPO")));
                pontos.add(ponto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pontos;
    }

    /**
     * Retorna o último tipo de ponto (entrada/saída) de um participante em um evento.
     *
     * @param eventoId       ID do evento.
     * @param participanteId ID do participante.
     * @return Último tipo de ponto registrado.
     */
    public String buscarUltimoTipoPonto(int eventoId, int participanteId) {
        Cursor cursor = db.rawQuery("SELECT TIPO FROM PONTOS WHERE EVENTO_ID = ? AND PARTICIPANTE_ID = ? ORDER BY DATA_HORA DESC LIMIT 1",
                new String[]{String.valueOf(eventoId), String.valueOf(participanteId)});
        String tipo = null;
        if (cursor.moveToFirst()) {
            tipo = cursor.getString(cursor.getColumnIndexOrThrow("TIPO"));
        }
        cursor.close();
        return tipo;
    }

    /**
     * Verifica se um participante já está registrado no evento (apenas entrada).
     *
     * @param eventoId       ID do evento.
     * @param participanteId ID do participante.
     * @return true se já estiver registrado, false caso contrário.
     */
    public boolean verificarParticipanteRegistrado(int eventoId, int participanteId) {
        Cursor cursor = db.rawQuery("SELECT * FROM PONTOS WHERE EVENTO_ID = ? AND PARTICIPANTE_ID = ? AND TIPO = 'ENTRADA'",
                new String[]{String.valueOf(eventoId), String.valueOf(participanteId)});
        boolean registrado = cursor.getCount() > 0;
        cursor.close();
        return registrado;
    }

    /**
     * Conta o número total de entradas em um evento.
     *
     * @param eventoId ID do evento.
     * @return Número total de entradas.
     */
    public int contarTotalEntradasPorEvento(int eventoId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'ENTRADA'",
                new String[]{String.valueOf(eventoId)});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Conta o número total de saídas em um evento.
     *
     * @param eventoId ID do evento.
     * @return Número total de saídas.
     */
    public int contarTotalSaidasPorEvento(int eventoId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'SAIDA'",
                new String[]{String.valueOf(eventoId)});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Conta o número de participantes que saíram de um evento.
     *
     * @param eventoId ID do evento.
     * @return Número de participantes que saíram.
     */
    public int contarParticipantesSaidosPorEvento(int eventoId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT PARTICIPANTE_ID) FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'SAIDA'",
                new String[]{String.valueOf(eventoId)});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Busca os pontos ativos (entradas sem saídas correspondentes) em um evento.
     *
     * @param eventoId ID do evento.
     * @return Lista de pontos ativos.
     */
    public List<Ponto> buscarPontosAtivosPorEvento(int eventoId) {
        List<Ponto> pontosAtivos = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT P1.* FROM PONTOS P1 WHERE P1.EVENTO_ID = ? AND P1.TIPO = 'ENTRADA' AND NOT EXISTS (" +
                        "SELECT 1 FROM PONTOS P2 WHERE P2.EVENTO_ID = P1.EVENTO_ID AND P2.PARTICIPANTE_ID = P1.PARTICIPANTE_ID AND P2.TIPO = 'SAIDA' AND P2.DATA_HORA > P1.DATA_HORA" +
                        ")",
                new String[]{String.valueOf(eventoId)});
        if (cursor.moveToFirst()) {
            do {
                Ponto ponto = new Ponto();
                ponto.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                ponto.setParticipanteId(cursor.getInt(cursor.getColumnIndexOrThrow("PARTICIPANTE_ID")));
                ponto.setEventoId(cursor.getInt(cursor.getColumnIndexOrThrow("EVENTO_ID")));
                ponto.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow("DATA_HORA")));
                ponto.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("TIPO")));
                pontosAtivos.add(ponto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pontosAtivos;
    }

    /**
     * Apaga todos os registros de saída ('SAIDA') de um evento.
     *
     * @param eventoId ID do evento.
     */
    public void apagarSaidasPorEvento(int eventoId) {
        db.delete("PONTOS", "EVENTO_ID = ? AND TIPO = ?", new String[]{String.valueOf(eventoId), "SAIDA"});
    }

    /**
     * Converte o evento para o modo simples, removendo saídas e entradas duplicadas.
     *
     * @param eventoId ID do evento.
     */
    public void converterParaEventoSimples(int eventoId) {
        // Remover todas as saídas do evento
        apagarSaidasPorEvento(eventoId);

        // Remover entradas duplicadas, mantendo apenas a primeira entrada para cada participante
        String sql = "DELETE FROM PONTOS WHERE TIPO = 'ENTRADA' AND EVENTO_ID = ? " +
                "AND ID NOT IN (SELECT MIN(ID) FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'ENTRADA' GROUP BY PARTICIPANTE_ID)";
        db.execSQL(sql, new Object[]{eventoId, eventoId});
    }

    /**
     * Apaga entradas duplicadas ('ENTRADA') de um evento.
     * Mantém apenas uma entrada por participante (a com menor ID).
     *
     * @param eventoId ID do evento.
     */
    public void apagarEntradasDuplicadasPorEvento(int eventoId) {
        String sql = "DELETE FROM PONTOS " +
                "WHERE TIPO = 'ENTRADA' AND EVENTO_ID = " + eventoId + " " +
                "AND ID NOT IN ( " +
                "    SELECT MIN(ID) FROM PONTOS " +
                "    WHERE EVENTO_ID = " + eventoId + " AND TIPO = 'ENTRADA' " +
                "    GROUP BY PARTICIPANTE_ID " +
                ")";
        db.execSQL(sql);
    }

    /**
     * Verifica se o participante já participou do evento (já tem uma entrada registrada).
     *
     * @param eventoId       ID do evento.
     * @param participanteId ID do participante.
     * @return true se já participou, false caso contrário.
     */
    public boolean verificarSeParticipou(int eventoId, int participanteId) {
        Cursor cursor = db.query("PONTOS",
                new String[]{"COUNT(DISTINCT PARTICIPANTE_ID) AS TOTAL"},
                "EVENTO_ID = ? AND PARTICIPANTE_ID = ?",
                new String[]{String.valueOf(eventoId), String.valueOf(participanteId)},
                null, null, null, "1"); // Limitar a 1 para otimização

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int total = cursor.getInt(cursor.getColumnIndexOrThrow("TOTAL"));
                cursor.close();
                return total > 0;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * Conta o número de participantes únicos (com pelo menos uma entrada) em um evento.
     *
     * @param eventoId ID do evento.
     * @return Número de participantes únicos.
     */
    public int contarParticipantesUnicos(int eventoId) {
        int totalParticipantesUnicos = 0;
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(DISTINCT PARTICIPANTE_ID) AS TOTAL FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'ENTRADA'",
                new String[]{String.valueOf(eventoId)}
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalParticipantesUnicos = cursor.getInt(cursor.getColumnIndexOrThrow("TOTAL"));
            }
            cursor.close();
        }
        return totalParticipantesUnicos;
    }

    /**
     * Conta o número total de entradas em um evento.
     *
     * @param eventoId ID do evento.
     * @return Número total de entradas.
     */
    public int contarTotalEntradas(int eventoId) {
        int totalEntradas = 0;
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) AS TOTAL FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'ENTRADA'",
                new String[]{String.valueOf(eventoId)}
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalEntradas = cursor.getInt(cursor.getColumnIndexOrThrow("TOTAL"));
            }
            cursor.close();
        }
        return totalEntradas;
    }

    /**
     * Conta participantes com múltiplas entradas em um evento.
     *
     * @param eventoId ID do evento.
     * @return Número de participantes com mais de uma entrada.
     */
    public int contarParticipantesComMultiplesEntradas(int eventoId) {
        int total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT PARTICIPANTE_ID, COUNT(*) AS CNT FROM PONTOS WHERE EVENTO_ID = ? AND TIPO = 'ENTRADA' GROUP BY PARTICIPANTE_ID HAVING CNT > 1",
                new String[]{String.valueOf(eventoId)}
        );

        if (cursor != null) {
            total = cursor.getCount();
            cursor.close();
        }
        return total;
    }

}
