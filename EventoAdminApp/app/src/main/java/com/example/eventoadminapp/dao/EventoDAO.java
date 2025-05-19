package com.example.eventoadminapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.eventoadminapp.model.Evento;
import com.example.eventoadminapp.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável pelo acesso e manipulação dos dados da tabela EVENTOS no banco de dados SQLite.
 * Fornece métodos para inserir, buscar, atualizar e excluir eventos.
 *
 * <p>
 * Exemplos de uso:
 * <pre>
 *     EventoDAO eventoDAO = new EventoDAO(context);
 *     long id = eventoDAO.inserirEvento(evento);
 *     List&lt;Evento&gt; eventos = eventoDAO.buscarTodosEventos();
 *     Evento evento = eventoDAO.buscarEventoPorId(id);
 *     boolean atualizado = eventoDAO.atualizarEvento(evento);
 *     boolean excluido = eventoDAO.excluirEvento(id);
 * </pre>
 * </p>
 *
 * <p>
 * Cada método lida com as operações CRUD (Create, Read, Update, Delete) para objetos do tipo Evento.
 * </p>
 *
 */
public class EventoDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public EventoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Insere um novo evento no banco de dados.
     *
     * @param evento O objeto Evento a ser inserido.
     * @return O ID do novo registro inserido ou -1 em caso de erro.
     */
    public long inserirEvento(Evento evento) {
        ContentValues values = new ContentValues();
        values.put("NOME", evento.getNome());
        values.put("DATA", evento.getData());
        values.put("DESCRICAO", evento.getDescricao());
        values.put("EVENTO_SIMPLES", evento.isEventoSimples() ? 1 : 0);
        values.put("MAX_PARTICIPANTES", evento.getMaxParticipantes());
        values.put("CONTAGEM_UNICA", evento.isContagemUnica() ? 1 : 0); // Adicionado

        return db.insert("EVENTOS", null, values);
    }

    /**
     * Busca todos os eventos no banco de dados.
     *
     * @return Uma lista de objetos Evento.
     */
    public List<Evento> buscarTodosEventos() {
        List<Evento> eventos = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM EVENTOS ORDER BY DATA DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Evento evento = new Evento();
                evento.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                evento.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
                evento.setData(cursor.getString(cursor.getColumnIndexOrThrow("DATA")));
                evento.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow("DESCRICAO")));
                evento.setEventoSimples(cursor.getInt(cursor.getColumnIndexOrThrow("EVENTO_SIMPLES")) == 1);
                evento.setMaxParticipantes(cursor.getInt(cursor.getColumnIndexOrThrow("MAX_PARTICIPANTES")));
                evento.setContagemUnica(cursor.getInt(cursor.getColumnIndexOrThrow("CONTAGEM_UNICA")) == 1); // Adicionado
                eventos.add(evento);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventos;
    }

    /**
     * Busca um evento específico pelo ID.
     *
     * @param id O ID do evento a ser buscado.
     * @return O objeto Evento correspondente ou null se não encontrado.
     */
    public Evento buscarEventoPorId(int id) {
        Cursor cursor = db.query("EVENTOS", null, "ID = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Evento evento = new Evento();
            evento.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
            evento.setNome(cursor.getString(cursor.getColumnIndexOrThrow("NOME")));
            evento.setData(cursor.getString(cursor.getColumnIndexOrThrow("DATA")));
            evento.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow("DESCRICAO")));
            evento.setEventoSimples(cursor.getInt(cursor.getColumnIndexOrThrow("EVENTO_SIMPLES")) == 1);
            evento.setMaxParticipantes(cursor.getInt(cursor.getColumnIndexOrThrow("MAX_PARTICIPANTES")));
            evento.setContagemUnica(cursor.getInt(cursor.getColumnIndexOrThrow("CONTAGEM_UNICA")) == 1); // Adicionado
            cursor.close();
            return evento;
        }
        return null;
    }

    /**
     * Atualiza os detalhes de um evento existente no banco de dados.
     *
     * @param evento O objeto Evento com os novos dados.
     * @return true se a atualização foi bem-sucedida, false caso contrário.
     */
    public boolean atualizarEvento(Evento evento) {
        ContentValues valores = new ContentValues();
        valores.put("NOME", evento.getNome());
        valores.put("DATA", evento.getData());
        valores.put("DESCRICAO", evento.getDescricao());
        valores.put("EVENTO_SIMPLES", evento.isEventoSimples() ? 1 : 0);
        valores.put("MAX_PARTICIPANTES", evento.getMaxParticipantes());
        valores.put("CONTAGEM_UNICA", evento.isContagemUnica() ? 1 : 0); // Adicionado

        int linhasAfetadas = db.update("EVENTOS", valores, "ID = ?", new String[]{String.valueOf(evento.getId())});
        return linhasAfetadas > 0;
    }

    /**
     * Exclui um evento do banco de dados.
     *
     * @param id O ID do evento a ser excluído.
     * @return true se a exclusão foi bem-sucedida, false caso contrário.
     */
    public boolean excluirEvento(int id) {
        int linhasAfetadas = db.delete("EVENTOS", "ID = ?", new String[]{String.valueOf(id)});
        return linhasAfetadas > 0;
    }
}
