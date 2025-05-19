package com.example.eventoadminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventoadminapp.R;
import com.example.eventoadminapp.dao.ParticipanteDAO;
import com.example.eventoadminapp.model.Ponto;
import com.example.eventoadminapp.model.Participante;

import java.util.List;

/**
 * Adapter para exibir uma lista de objetos {@link Ponto} em um {@link RecyclerView}.
 * Cada item da lista mostra o nome do participante, data/hora e tipo do ponto.
 *
 * <p>O adapter utiliza um {@link ParticipanteDAO} para buscar informações do participante
 * associado a cada ponto.</p>
 *
 */
public class PontoAdapter extends RecyclerView.Adapter<PontoAdapter.PontoViewHolder> {

    // Lista de pontos a serem exibidos
    private List<Ponto> pontos;
    // DAO para buscar informações dos participantes
    private ParticipanteDAO participanteDAO;

    // Construtor recebe a lista de pontos e o DAO de participante
    public PontoAdapter(List<Ponto> pontos, ParticipanteDAO participanteDAO) {
        this.pontos = pontos;
        this.participanteDAO = participanteDAO;
    }

    @NonNull
    @Override
    public PontoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item da lista
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ponto, parent, false);
        return new PontoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PontoViewHolder holder, int position) {
        // Verifica se a lista é válida e se a posição é válida
        if (pontos == null || position >= pontos.size()) {
            // Evita acesso inválido à lista
            holder.textViewNome.setText("Desconhecido");
            holder.textViewDataHora.setText("N/A");
            holder.textViewTipo.setText("N/A");
            return;
        }

        // Obtém o ponto da posição atual
        Ponto ponto = pontos.get(position);
        // Busca o participante pelo ID
        Participante participante = participanteDAO.buscarParticipantePorId(ponto.getParticipanteId());

        // Define o nome do participante, se encontrado
        if (participante != null) {
            holder.textViewNome.setText(participante.getNome());
        } else {
            holder.textViewNome.setText("Desconhecido");
        }

        // Define a data/hora e o tipo do ponto
        holder.textViewDataHora.setText(ponto.getDataHora());
        holder.textViewTipo.setText(ponto.getTipo());
    }

    @Override
    public int getItemCount() {
        // Retorna o tamanho da lista de pontos
        return pontos != null ? pontos.size() : 0;
    }

    // Atualiza a lista de pontos e notifica o adapter
    public void setPontos(List<Ponto> novosPontos) {
        this.pontos = novosPontos;
        notifyDataSetChanged();
    }

    // ViewHolder para os itens da lista
    static class PontoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNome;
        TextView textViewDataHora;
        TextView textViewTipo;

        public PontoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializa os TextViews do layout
            textViewNome = itemView.findViewById(R.id.textViewNomeParticipante);
            textViewDataHora = itemView.findViewById(R.id.textViewDataHora);
            textViewTipo = itemView.findViewById(R.id.textViewTipo);
        }
    }
}
