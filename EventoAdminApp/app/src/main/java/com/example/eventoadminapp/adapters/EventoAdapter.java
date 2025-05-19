package com.example.eventoadminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventoadminapp.R;
import com.example.eventoadminapp.model.Evento;

import java.util.List;

/**
 * Adapter para exibir uma lista de eventos em um RecyclerView.
 * <p>
 * Este adapter é responsável por criar e vincular os itens da lista de eventos,
 * além de fornecer uma interface para capturar cliques nos itens.
 * </p>
 *
 * @author SeuNome
 */
public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    /**
     * Interface para capturar cliques nos itens do RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Método chamado quando um item é clicado.
         *
         * @param evento O evento clicado.
         */
        void onItemClick(Evento evento);
    }

    /**
     * Construtor do adapter.
     *
     * @param listaEventos Lista de eventos a ser exibida.
     * @param listener     Listener para capturar cliques nos itens.
     */
    public EventoAdapter(List<Evento> listaEventos, OnItemClickListener listener) { ... }

    /**
     * Cria um novo ViewHolder para um item da lista.
     *
     * @param parent   O ViewGroup ao qual a nova View será anexada.
     * @param viewType O tipo de view do novo item.
     * @return Um novo EventoViewHolder.
     */
    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { ... }

    /**
     * Vincula os dados de um evento ao ViewHolder.
     *
     * @param holder   O ViewHolder que deve ser atualizado.
     * @param position A posição do item na lista.
     */
    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) { ... }

    /**
     * Retorna o número total de itens na lista.
     *
     * @return O tamanho da lista de eventos.
     */
    @Override
    public int getItemCount() { ... }

    /**
     * ViewHolder para os itens do RecyclerView.
     */
    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView para exibir o nome do evento.
         */
        TextView textViewNomeEvento;

        /**
         * TextView para exibir a data do evento.
         */
        TextView textViewDataEvento;

        /**
         * Construtor do ViewHolder.
         *
         * @param itemView A view do item.
         */
        public EventoViewHolder(@NonNull View itemView) { ... }
    }
}