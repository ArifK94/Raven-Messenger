package com.example.arifk.ravenmessenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ConversationAdapater extends FirestoreRecyclerAdapter<Chat, ConversationAdapater.ConversationViewHolder> {


    private String currentUserID;
    private CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("Users");
    private CollectionReference messagesCollection = FirebaseFirestore.getInstance().collection("Messages");

    private String name;


    public ConversationAdapater(@NonNull FirestoreRecyclerOptions<Chat> options, String currentUserID) {
        super(options);

        this.currentUserID = currentUserID;

    }


    @Override
    protected void onBindViewHolder(@NonNull final ConversationViewHolder holder, int position, @NonNull final Chat model) {

        holder.bind(model);


        // if current user is sender, then show recipient details
        // else show sender's details
        if (currentUserID.equals(model.getSender())) {
            getUserDetails(holder, model.getRecipient(), model.getMessage_status());

            sendToChatRoom(holder, model.getRecipient(), model.getMessage_status());

            updateMessageStatus(holder, model);


        } else {

            getUserDetails(holder, model.getSender(), model.getMessage_status());

            sendToChatRoom(holder, model.getSender(), "Read");

            updateLastMessage(holder, model);

        }
    }


    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);

        return new ConversationViewHolder(mView);
    }


    private void getUserDetails(@NonNull final ConversationViewHolder holder, @NonNull final String userID, final String messageStatus) {
        usersCollection.document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Picasso.get().load(documentSnapshot.getString("avatar")).placeholder(R.drawable.avatar_default).into(holder.avatar);

                name = documentSnapshot.getString("name");
                holder.displayname.setText(name);

                openChatOption(holder, userID, name, messageStatus);
            }
        });
    }


    private void sendToChatRoom(@NonNull final ConversationViewHolder holder, final String userID, final String messageStatus) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chatRoomIntent = new Intent(holder.mView.getContext(), ChatRoomActivity.class);
                chatRoomIntent.putExtra("profileUser_ID", userID);
                chatRoomIntent.putExtra("profileUser_Name", name);
                chatRoomIntent.putExtra("message_status", messageStatus);
                holder.mView.getContext().startActivity(chatRoomIntent);

            }

        });
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }


    private void openChatOption(@NonNull final ConversationViewHolder holder, final String userID, final String name, final String messageStatus) {
        holder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                holder.showChatOption(name, userID, holder, messageStatus);

                return true;
            }
        });
    }

    private void updateMessageStatus(@NonNull final ConversationViewHolder holder, @NonNull final Chat model) {

        if (model.getMessage_status() != null) {
            if (model.getMessage_status().equals("Delivered"))
                holder.status.setImageDrawable(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.message_delivered));
            else if (model.getMessage_status().equals("Read"))
                holder.status.setImageDrawable(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.message_read));

        }

    }

    private void updateLastMessage(@NonNull final ConversationViewHolder holder, @NonNull final Chat model) {

        if (model.getMessage_status() != null)
        {
            if (model.getMessage_status().equals("Delivered")) {
                holder.lastTextMessage.setTypeface(Typeface.DEFAULT_BOLD);
                holder.status.setImageDrawable(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.message_received_closed));

            } else if (model.getMessage_status().equals("Read")) {
                holder.lastTextMessage.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                holder.status.setImageDrawable(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.message_received_opened));
            }
        }
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView lastTextMessage;
        ImageView lastImageMessage;
        TextView displayname;
        TextView time;
        ImageView avatar;
        ImageView status;
        CardView root;


        ConversationViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            lastTextMessage = itemView.findViewById(R.id.chat_last_message_text);
            lastImageMessage = itemView.findViewById(R.id.chat_last_message_image);

            displayname = itemView.findViewById(R.id.chat_display_name);
            avatar = itemView.findViewById(R.id.chat_profile_image);
            time = itemView.findViewById(R.id.chat_time);
            status = itemView.findViewById(R.id.chat_message_status);
            root = itemView.findViewById(R.id.root_card_view);
        }

        void bind(Chat chat) {

            if (chat.getType().equals("text")) {
                lastTextMessage.setText(chat.getMessage());

                lastTextMessage.setVisibility(View.VISIBLE);
                lastImageMessage.setVisibility(View.GONE);
            } else {
                Picasso.get().load(chat.getMessage()).placeholder(R.mipmap.ic_image_black).into(lastImageMessage);

                lastTextMessage.setVisibility(View.GONE);
                lastImageMessage.setVisibility(View.VISIBLE);
            }

            getMessageTime(chat);
        }

        private void getMessageTime(Chat chat) {

            long minute = (chat.getTime() / (1000 * 60)) % 60;
            long hour = (chat.getTime() / (1000 * 60 * 60)) % 24;

            String time1 = String.format("%02d:%02d", hour, minute);

            time.setText(time1);
        }


        private void showChatOption(String name, final String userID, @NonNull final ConversationViewHolder holder, final String messageStatus) {
            final String[] options = {"View", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
            builder.setTitle(name);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == 0) {
                        Intent chatRoomIntent = new Intent(holder.mView.getContext(), ChatRoomActivity.class);
                        chatRoomIntent.putExtra("profileUser_ID", userID);
                        chatRoomIntent.putExtra("message_status", messageStatus);
                        holder.mView.getContext().startActivity(chatRoomIntent);
                    } else if (which == 1) {
                        deleteChat(userID);
                    }

                }
            });
            builder.show();
        }

        private void deleteChat(final String userID) {
            messagesCollection.document(currentUserID).collection("Conversations").document(userID).delete();

            messagesCollection.document(currentUserID).collection("Conversations").document(userID).collection("OnetoOneChat").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {

                        List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();

                        for (DocumentSnapshot documentSnapshot : myListOfDocuments)
                            documentSnapshot.getReference().delete();


                    }
                }
            });
        }
    }
}