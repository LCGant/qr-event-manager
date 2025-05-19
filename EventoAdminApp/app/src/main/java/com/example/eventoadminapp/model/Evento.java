package com.example.eventoadminapp.model;

public class Evento {
    private int id;
    private String nome;
    private String data;
    private String descricao;
    private boolean eventoSimples;
    private int maxParticipantes;
    private boolean contagemUnica;

    public Evento() {}

    public Evento(int id, String nome, String data, String descricao, boolean eventoSimples, int maxParticipantes, boolean contagemUnica) {
        this.id = id;
        this.nome = nome;
        this.data = data;
        this.descricao = descricao;
        this.eventoSimples = eventoSimples;
        this.maxParticipantes = maxParticipantes;
        this.contagemUnica = contagemUnica;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isEventoSimples() {
        return eventoSimples;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }

    public boolean isContagemUnica() {
        return contagemUnica;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setEventoSimples(boolean eventoSimples) {
        this.eventoSimples = eventoSimples;
    }

    public void setMaxParticipantes(int maxParticipantes) {
        this.maxParticipantes = maxParticipantes;
    }

    public void setContagemUnica(boolean contagemUnica) {
        this.contagemUnica = contagemUnica;
    }
}
