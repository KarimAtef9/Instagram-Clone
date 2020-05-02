package com.example.instagram.Model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.MainActivity;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    private Context mContext;
    private ArrayList<Comment> commentsList;

    private FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, ArrayList<Comment> commentsList) {
        this.mContext = mContext;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = commentsList.get(position);

        holder.comment_textview.setText(comment.getComment());
        updateCommenterInfo(comment.getPublisher(), holder.username_textview, holder.profileImage_imageview);


        // open commenter profile on clicking his name or profile image
        holder.username_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
            }
        });
        holder.profileImage_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username_textview, comment_textview;
        public ImageView profileImage_imageview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username_textview = itemView.findViewById(R.id.username_textview);
            comment_textview = itemView.findViewById(R.id.comment_textview);
            profileImage_imageview = itemView.findViewById(R.id.profile_imageview);

        }
    }

    // update username and profile image of user (user to comment on the post)
    private void updateCommenterInfo(String publisherId, final TextView username, final ImageView profileImage) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(profileImage);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
