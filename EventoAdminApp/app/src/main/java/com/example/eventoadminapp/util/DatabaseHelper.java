package com.example.eventoadminapp.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper para criação e atualização do banco de dados SQLite da aplicação de eventos.
 *
 * <p>Gerencia a criação das tabelas EVENTOS, PARTICIPANTES e PONTOS, além de
 * aplicar migrações ao atualizar a versão do esquema.</p>
 *
 * @since 1.0
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /** Nome do arquivo do banco de dados. */
    private static final String DATABASE_NAME = "evento.db";
    /** Versão atual do esquema do banco de dados. */
    private static final int DATABASE_VERSION = 5;

    /**
     * Construtor que cria o helper com nome e versão definidos.
     *
     * @param context Contexto da aplicação usado para abrir/criar o banco.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Chamado na primeira vez que o banco é criado. Cria as tabelas:
     * EVENTOS, PARTICIPANTES e PONTOS.
     *
     * @param db Instância do banco de dados SQLite que será inicializado.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela EVENTOS
        db.execSQL(
            "CREATE TABLE EVENTOS (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NOME TEXT NOT NULL, " +
            "DATA TEXT NOT NULL, " +
            "DESCRICAO TEXT, " +
            "EVENTO_SIMPLES INTEGER NOT NULL, " +
            "MAX_PARTICIPANTES INTEGER DEFAULT 0, " +
            "CONTAGEM_UNICA INTEGER DEFAULT 0)"
        );

        // Tabela PARTICIPANTES
        db.execSQL(
            "CREATE TABLE PARTICIPANTES (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NOME TEXT NOT NULL, " +
            "RGM INTEGER UNIQUE NOT NULL)"
        );

        // Tabela PONTOS
        db.execSQL(
            "CREATE TABLE PONTOS (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "PARTICIPANTE_ID INTEGER NOT NULL, " +
            "EVENTO_ID INTEGER NOT NULL, " +
            "DATA_HORA TEXT NOT NULL, " +
            "TIPO TEXT NOT NULL, " +
            "FOREIGN KEY(PARTICIPANTE_ID) REFERENCES PARTICIPANTES(ID), " +
            "FOREIGN KEY(EVENTO_ID) REFERENCES EVENTOS(ID))"
        );
    }

    /**
     * Chamado quando a versão do banco é incrementada. Aplica alterações
     * de esquema necessárias de acordo com a versão anterior.
     *
     * @param db         Instância do banco de dados SQLite em modo de atualização.
     * @param oldVersion Versão anterior do banco de dados.
     * @param newVersion Nova versão do banco de dados definida.
     * @since 1.1
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // A partir da versão 3, adicionar coluna MAX_PARTICIPANTES
        if (oldVersion < 3) {
            db.execSQL(
                "ALTER TABLE EVENTOS " +
                "ADD COLUMN MAX_PARTICIPANTES INTEGER DEFAULT 0"
            );
        }
        // A partir da versão 5, adicionar coluna CONTAGEM_UNICA
        if (oldVersion < 5) {
            db.execSQL(
                "ALTER TABLE EVENTOS " +
                "ADD COLUMN CONTAGEM_UNICA INTEGER DEFAULT 0"
            );
        }
    }
}
