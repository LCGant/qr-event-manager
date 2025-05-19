package com.example.eventoadminapp.model;

public class Participante {
    private int id;
    private String nome;
    private int rgm;

    public Participante() {}

    public Participante(int id, String nome, int rgm) {
        this.id = id;
        this.nome = nome;
        this.rgm = rgm;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getRgm() {
        return rgm;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setRgm(int rgm) {
        this.rgm = rgm;
    }
}
