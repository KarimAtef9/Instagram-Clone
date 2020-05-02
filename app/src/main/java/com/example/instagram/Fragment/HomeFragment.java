package com.example.instagram.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.instagram.Model.Post;
import com.example.instagram.Model.PostAdapter;
import com.example.instagram.Model.User;
import com.example.instagram.Model.UserAdapter;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class HomeFragment extends Fragment {
    private RecyclerView postsRecycleView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postsList;

    FirebaseUser firebaseUser;

    ArrayList<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        postsRecycleView = view.findViewById(R.id.posts_recycleView);
        postsRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postsRecycleView.setLayoutManager(linearLayoutManager);

        postsList = new ArrayList<>();
        followingList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postsList);
        postsRecycleView.setAdapter(postAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        fillFollowingList();
        readPosts();


        // Inflate the layout for this fragment
        return view;
    }

    // fill followingList with users who i follow
    private void fillFollowingList() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(firebaseUser.getUid()).child("Following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPosts() {

        final DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts");

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                // Posts contains each user id ,, and each user id contains his posts
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // check if following this user
                    for (String id : followingList) {
                        if (id.equals(snapshot.getKey())) {
                            // add his posts
                            for (DataSnapshot postsSnapshot : snapshot.getChildren()) {
                                Post post = postsSnapshot.getValue(Post.class);
                                postsList.add(post);
                            }
                        }
                    }
                }
                // sort according to post time
                Collections.sort(postsList, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o1.getTimeInMillis().compareTo(o2.getTimeInMillis());
                    }
                });
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
