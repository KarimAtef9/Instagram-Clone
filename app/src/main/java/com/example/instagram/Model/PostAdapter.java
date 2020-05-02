package com.example.instagram.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostAdapter extends ArrayAdapter<Post> {
    ArrayList<Post> postsList;
    // required for getting image
    private Context mContext;

    private ImageView profile_imageview, postImage_imageview, like_imageview, comment_imageview, save_imageview;
    private TextView username_textview, likes_textview, publisher_textview, description_textview, comments_textview;

    private FirebaseUser firebaseUser;



    public PostAdapter(Context context, ArrayList<Post> posts) {
        super(context, 0, posts);
        this.mContext = context;
        this.postsList = posts;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.post_item, parent, false);
        }

        profile_imageview = listItemView.findViewById(R.id.profile_image);
        postImage_imageview = listItemView.findViewById(R.id.post_image);
        like_imageview = listItemView.findViewById(R.id.like_imageview);
        comment_imageview = listItemView.findViewById(R.id.comment_imageview);
        save_imageview = listItemView.findViewById(R.id.save_imageview);

        username_textview = listItemView.findViewById(R.id.username_textview);
        likes_textview = listItemView.findViewById(R.id.likes_textview);
        publisher_textview = listItemView.findViewById(R.id.publisher_textview);
        description_textview = listItemView.findViewById(R.id.description_textview);
        comments_textview = listItemView.findViewById(R.id.viewComments_textview);

        final Post post = postsList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // update post specs
        Glide.with(mContext).load(post.getImageUrl()).into(postImage_imageview);
        if (post.getDescription().equals("")) {
            description_textview.setVisibility(View.GONE);
        } else {
            description_textview.setText(post.getDescription());
        }
        updatePublisherInfo(post.getPublisher());

        update_LikeButton_NoLikes(post.getPostId());
        updateNumberofLikes(post.getPostId());

        like_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // like post
                if (like_imageview.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);
                } else {
                    // remove like
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });


        return listItemView;
    }

    private void updatePublisherInfo(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username_textview.setText(user.getUsername());
                publisher_textview.setText(user.getUsername());
                Glide.with(mContext).load(user.getImageUrl()).into(profile_imageview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update like button whether this post liked or not && update number of likes
    private void update_LikeButton_NoLikes(String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    like_imageview.setImageResource(R.drawable.ic_liked);
                    like_imageview.setTag("liked");
                } else {
                    like_imageview.setImageResource(R.drawable.ic_like);
                    like_imageview.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateNumberofLikes(String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes_textview.setText(dataSnapshot.getChildrenCount() +" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
