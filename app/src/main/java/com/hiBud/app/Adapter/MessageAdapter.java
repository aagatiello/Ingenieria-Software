package com.hiBud.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hiBud.app.DAO.UserDAO;
import com.hiBud.app.Holder.MessageHolder;
import com.hiBud.app.Logic.MessageLogic;
import com.hiBud.app.Logic.UserLogic;
import com.hiBud.app.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

    private List<MessageLogic> messageList = new ArrayList<>();
    private Context c;

    public MessageAdapter(Context c) {
        this.c = c;
    }

    public int addMessage(MessageLogic messageLogic) {
        messageList.add(messageLogic);
        int position = messageList.size() - 1;
        notifyItemInserted(messageList.size());
        return position;
    }

    public void updateMessage(int position, MessageLogic messageLogic) {
        messageList.set(position, messageLogic);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(c).inflate(R.layout.card_view_mensajes_enviados, parent, false);
        } else {
            view = LayoutInflater.from(c).inflate(R.layout.card_view_mensajes_recibidos, parent, false);
        }
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {

        MessageLogic messageLogic = messageList.get(position);

        UserLogic userLogic = messageLogic.getUserLogic();

        if (userLogic != null) {
            holder.getNombre().setText(userLogic.getUser().getNombre());
            Glide.with(c).load(userLogic.getUser().getFotoPerfilURL()).into(holder.getFotoMensajePerfil());
        }

        holder.getMensaje().setText(messageLogic.getMessage().getMensaje());
        if (messageLogic.getMessage().isContieneFoto()) {
            holder.getFotoMensaje().setVisibility(View.VISIBLE);
            holder.getMensaje().setVisibility(View.VISIBLE);
            Glide.with(c).load(messageLogic.getMessage().getUrlFoto()).into(holder.getFotoMensaje());
        } else {
            holder.getFotoMensaje().setVisibility(View.GONE);
            holder.getMensaje().setVisibility(View.VISIBLE);
        }

        holder.getHora().setText(messageLogic.messageCreationDate());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getUserLogic() != null) {
            if (messageList.get(position).getUserLogic().getKey().equals(UserDAO.getInstance().getUserKey())) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}