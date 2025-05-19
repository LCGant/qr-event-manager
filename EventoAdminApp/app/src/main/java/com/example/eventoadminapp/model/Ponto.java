package com.example.eventoadminapp.model;

public class Ponto {
    private int id;
    private int participanteId;
    private int eventoId;
    private String dataHora;
    private String tipo; // "ENTRADA" ou "SAIDA"

    public Ponto() {}

    public Ponto(int id, int participanteId, int eventoId, String dataHora, String tipo) {
        this.id = id;
        this.participanteId = participanteId;
        this.eventoId = eventoId;
        this.dataHora = dataHora;
        this.tipo = tipo;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public int getParticipanteId() {
        return participanteId;
    }

    public int getEventoId() {
        return eventoId;
    }

    public String getDataHora() {
        return dataHora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setParticipanteId(int participanteId) {
        this.participanteId = participanteId;
    }

    public void setEventoId(int eventoId) {
        this.eventoId = eventoId;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
