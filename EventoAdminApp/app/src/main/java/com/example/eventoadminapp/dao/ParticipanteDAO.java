package com.example.eventoadminapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.eventoadminapp.model.Participante;
import com.example.eventoadminapp.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ParticipanteDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    /**
     * Construtor da classe ParticipanteDAO.
     * Inicializa o DatabaseHelper e obtém uma instância do banco de dados para escrita.
     *
     * @param context Contexto da aplicação utilizado para acessar o banco de dados.
     */
    public ParticipanteDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Insere um novo participante na tabela PARTICIPANTES.
     *
     * @param participante Objeto Participante a ser inserido.
     * @return O ID da linha inserida, ou -1 se ocorrer um erro.
     */
    public long inserirParticipante(Participante participante) {
        ContentValues values = new ContentValues();
        values.put("NOME", participante.getNome());
        values.put("RGM", participante.getRgm());
        return db.insert("PARTICIPANTES", null, values);
    }

    /**
     * Busca um participante pelo RGM.
     *
     * @param rgm Número de RGM do participante.
     * @return Objeto Participante correspondente ao RGM, ou null se não encontrado.
     */
    public Participante buscarParticipantePorRgm(int rgm) {
        String[] colunas = {"ID", "NOME", "RGM"};
        String selection = "RGM = ?";
        String[] selectionArgs = {String.valueOf(rgm)};

        Cursor cursor = db.query("PARTICIPANTES", colunas, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Participante participante = new Participante();
            participante.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
            participante.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
            participante.setRgm(cursor.getInt(cursor.getColumnIndexOrThrow("RGM")));
            cursor.close();
            return participante;
        }

        return null;
    }

    /**
     * Busca um participante pelo ID.
     *
     * @param id Identificador único do participante.
     * @return Objeto Participante correspondente ao ID, ou null se não encontrado.
     */
    public Participante buscarParticipantePorId(int id) {
        String[] colunas = {"ID", "NOME", "RGM"};
        String selection = "ID = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query("PARTICIPANTES", colunas, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Participante participante = new Participante();
            participante.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
            participante.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
            participante.setRgm(cursor.getInt(cursor.getColumnIndexOrThrow("RGM")));
            cursor.close();
            return participante;
        }

        return null;
    }

    /**
     * Busca todos os participantes associados a um evento específico.
     *
     * @param eventoId Identificador do evento.
     * @return Lista de objetos Participante que participam do evento.
     */
    public List<Participante> buscarTodosParticipantesPorEvento(int eventoId) {
        List<Participante> participantes = new ArrayList<>();
        String query = "SELECT DISTINCT P.ID, P.NOME, P.RGM " +
                "FROM PARTICIPANTES P " +
                "INNER JOIN PONTOS PT ON P.ID = PT.PARTICIPANTE_ID " +
                "WHERE PT.EVENTO_ID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(eventoId)});

        if (cursor.moveToFirst()) {
            do {
                Participante participante = new Participante();
                participante.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                participante.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
                participante.setRgm(cursor.getInt(cursor.getColumnIndexOrThrow("RGM")));
                participantes.add(participante);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return participantes;
    }
}
public class ParticipanteDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ParticipanteDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // Inserir um novo participante
    public long inserirParticipante(Participante participante) {
        ContentValues values = new ContentValues();
        values.put("NOME", participante.getNome());
        values.put("RGM", participante.getRgm());
        return db.insert("PARTICIPANTES", null, values);
    }

    // Buscar participante por RGM
    public Participante buscarParticipantePorRgm(int rgm) {
        String[] colunas = {"ID", "NOME", "RGM"};
        String selection = "RGM = ?";
        String[] selectionArgs = {String.valueOf(rgm)};

        Cursor cursor = db.query("PARTICIPANTES", colunas, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Participante participante = new Participante();
            participante.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
            participante.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
            participante.setRgm(cursor.getInt(cursor.getColumnIndexOrThrow("RGM")));
            cursor.close();
            return participante;
        }

        return null;
    }

    // Buscar participante por ID
    public Participante buscarParticipantePorId(int id) {
        String[] colunas = {"ID", "NOME", "RGM"};
        String selection = "ID = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query("PARTICIPANTES", colunas, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Participante participante = new Participante();
            participante.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
            participante.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
            participante.setRgm(cursor.getInt(cursor.getColumnIndexOrThrow("RGM")));
            cursor.close();
            return participante;
        }

        return null;
    }

    // Buscar todos os participantes de um evento
    public List<Participante> buscarTodosParticipantesPorEvento(int eventoId) {
        List<Participante> participantes = new ArrayList<>();
        String query = "SELECT DISTINCT P.ID, P.NOME, P.RGM " +
                "FROM PARTICIPANTES P " +
                "INNER JOIN PONTOS PT ON P.ID = PT.PARTICIPANTE_ID " +
                "WHERE PT.EVENTO_ID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(eventoId)});

        if (cursor.moveToFirst()) {
            do {
                Participante participante = new Participante();
                participante.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                participante.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
                participante.setRgm(cursor.getInt(cursor.getColumnIndexOrThrow("RGM")));
                participantes.add(participante);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return participantes;
    }


}
