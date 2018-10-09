package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int SENT = 0;
    private static final int RECEIVED = 1;

    private String userId;
    private List<Chat> chats;

    ChatAdapter(List<Chat> chats, String userId) {
        this.chats = chats;
        this.userId = userId;
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chats.get(position).getSender().contentEquals(userId)) {
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_sent_message,
                    parent,
                    false
            );

            return new SentMessageViewHolder(view);

        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_received_message,
                    parent,
                    false);

            return new ReceivedMessageViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case SENT:
                initLayoutOne((SentMessageViewHolder) holder, position);
                break;
            case RECEIVED:
                initLayoutTwo((ReceivedMessageViewHolder) holder, position);
                break;
        }
    }


    private void initLayoutOne(SentMessageViewHolder holder, int pos) {

        holder.bind(chats.get(pos));

        if (pos == 0) {
            holder.messageTime.setVisibility(View.VISIBLE);
            holder.messageStatus.setVisibility(View.VISIBLE);
        } else {
            holder.messageTime.setVisibility(View.GONE);
            holder.messageStatus.setVisibility(View.GONE);
        }
    }

    private void initLayoutTwo(ReceivedMessageViewHolder holder, int pos) {
        holder.bind(chats.get(pos));

        if (pos == 0)
            holder.messageTime.setVisibility(View.VISIBLE);
        else
            holder.messageTime.setVisibility(View.GONE);

    }


    class SentMessageViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView messageText;
        TextView messageTime;
        ImageView messageStatus;
        ImageView messageImage;

        SentMessageViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            messageText = itemView.findViewById(R.id.message_body_text);
            messageTime = itemView.findViewById(R.id.message_time);
            messageStatus = itemView.findViewById(R.id.message_status);
            messageImage = itemView.findViewById(R.id.message_body_image);

        }

        void bind(Chat chat) {
            messageText.setText(chat.getMessage());

            getMessageTime(chat);
            getMessageStatus(chat);
            getMessageType(chat);
        }

        private void getMessageTime(Chat chat) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy\nhh-mm-ss a");
            String dateString = sdf.format(chat.getTime());

            messageTime.setText(dateString);
        }

        private void getMessageStatus(Chat chat) {
            if (chat.getMessage_status() != null) {
                if (chat.getMessage_status().equals("Delivered"))
                    messageStatus.setImageDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.message_delivered));
                else if (chat.getMessage_status().equals("Read"))
                    messageStatus.setImageDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.message_read));

            }
        }

        private void getMessageType(final Chat chat) {
            if (chat.getType().equals("text")) {
                messageText.setText(chat.getMessage());

                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
            } else {
                Picasso.get().load(chat.getMessage()).placeholder(R.mipmap.ic_image_black).into(messageImage);

                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.VISIBLE);

                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent fullScreenIntent = new Intent(itemView.getContext(), FullScreenImageActivity.class);
                        fullScreenIntent.setData(Uri.parse(chat.getMessage()));
                        itemView.getContext().startActivity(fullScreenIntent);
                    }
                });
            }
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;
        ImageView messageImage;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_body_text);
            messageTime = itemView.findViewById(R.id.message_time);
            messageImage = itemView.findViewById(R.id.message_body_image);
        }

        void bind(Chat chat) {

            getMessageType(chat);
            getMessageTime(chat);
        }

        private void getMessageTime(Chat chat) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy\nhh-mm-ss a");
            String dateString = sdf.format(chat.getTime());

            messageTime.setText(dateString);
        }

        private void getMessageType(final Chat chat) {
            if (chat.getType().equals("text")) {
                messageText.setText(chat.getMessage());

                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
            } else {
                Picasso.get().load(chat.getMessage()).placeholder(R.mipmap.ic_image_black).into(messageImage);

                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.VISIBLE);

                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent fullScreenIntent = new Intent(itemView.getContext(), FullScreenImageActivity.class);
                        fullScreenIntent.setData(Uri.parse(chat.getMessage()));
                        itemView.getContext().startActivity(fullScreenIntent);
                    }
                });

            }
        }
    }
}