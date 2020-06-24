package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.Model.Comment;
import com.example.instagram.Model.CommentAdapter;
import com.example.instagram.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView commentsRecycleView;
    private CommentAdapter commentAdapter;
    private ArrayList<Comment> commentsList;

    private ImageView profile_imageview;
    private TextView postComment_textview;
    private EditText comment_edittext;

    private String postId;
    private String publisherId;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // receive postId & publisherId
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        publisherId = intent.getStringExtra("publisherId");

        commentsRecycleView = findViewById(R.id.comments_recycleview);
        commentsRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentsRecycleView.setLayoutManager(linearLayoutManager);

        commentsList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentsList, postId);
        commentsRecycleView.setAdapter(commentAdapter);

        profile_imageview = findViewById(R.id.profile_imageview);
        postComment_textview = findViewById(R.id.postComment_textview);
        comment_edittext = findViewById(R.id.comment_edittext);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // change Post button (textView) color on typing
        comment_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // activate post button (textView)
                if (charSequence.toString().trim().length() > 0) {
                    postComment_textview.setTextColor(ContextCompat.getColor(CommentActivity.this, R.color.colorPrimary));
                } else {
                    postComment_textview.setTextColor(ContextCompat.getColor(CommentActivity.this, R.color.colorAccent));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        postComment_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!comment_edittext.getText().toString().equals("")) {
                    addComment();
                }
            }
        });

        // in comment bar
        updateCommenterImage();
        readComments();
    }

    // posting comment to database & add notification to publisher
    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        String commentId = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", comment_edittext.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("commentId", commentId);

        reference.child(commentId).setValue(hashMap);
        addNotification();
        comment_edittext.setText("");
    }

    private void updateCommenterImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_imageview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    commentsList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("note", "commented: " +comment_edittext.getText().toString());
        hashMap.put("posterId", publisherId);
        hashMap.put("postId", postId);
        hashMap.put("isPost", true);

        reference.push().setValue(hashMap);
    }
}
