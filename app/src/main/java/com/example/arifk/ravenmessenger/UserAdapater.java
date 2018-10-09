package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class UserAdapater extends FirestoreRecyclerAdapter<User, UserAdapater.UserHolder> {


    private int mBackgroundResourceImage;


    public UserAdapater(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull final UserHolder holder, int position, @NonNull User model) {

        if (mBackgroundResourceImage != 0)
        holder.background.setImageResource(mBackgroundResourceImage);

        holder.displayname.setText(model.getName());
        holder.username.setText(model.getUsername());
        Picasso.get().load(model.getAvatar()).placeholder(R.drawable.avatar_default).into(holder.avatar);



       final String user_id = getSnapshots().getSnapshot(position).getReference().getId();

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent profileIntent = new Intent(holder.mView.getContext(), UserProfileActivity.class);
                profileIntent.putExtra("user_id", user_id);
                holder.mView.getContext().startActivity(profileIntent);

            }
        });
    }




    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);

        return new UserHolder(mView);
    }

    public void setmBackgroundResourceImage(int mBackgroundResourceImage) {
        this.mBackgroundResourceImage = mBackgroundResourceImage;
    }

    class UserHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView displayname;
        TextView username;
        ImageView avatar;
        ImageView background;

        public UserHolder(View itemView) {
            super(itemView);

            mView = itemView;
            displayname = itemView.findViewById(R.id.user_item_displayname);
            username = itemView.findViewById(R.id.user_item_username);
            avatar = itemView.findViewById(R.id.user_item_avatar);
            background = itemView.findViewById(R.id.imgview_background);
        }
    }
}
