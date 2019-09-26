package com.phenamwema.bizrater;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

//import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil extends AppCompatActivity {

    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseAuth firebaseAuth;
    public static FirebaseAuth.AuthStateListener authStateListener;
    public static ArrayList<Model> list;
    private static MainActivity caller;
    public static FirebaseStorage firebaseStorage;
    public static StorageReference storageReference;
    private static final int RC_SIGN_IN = 1;
    public static boolean isAdmin;

    private FirebaseUtil(){};

    public static void Reference(String ref, final MainActivity callActivity){
        if(firebaseUtil == null){
            firebaseUtil= new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            caller = callActivity;

            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() == null) {
                        //FirebaseUtil.signIn();
                        callActivity.startActivity(new Intent(callActivity.getBaseContext(),Login.class));
                    }else{
                        String uid = firebaseAuth.getUid();
                        checkAdmin(uid);
                    }
                    Toast.makeText(callActivity.getBaseContext(),"Welcome back!", Toast.LENGTH_LONG).show();
                }
            };
            connectToStorage();
        }
        list = new ArrayList<Model>();
        databaseReference = firebaseDatabase.getReference().child(ref);
    }

    private static void checkAdmin(String uid){
        FirebaseUtil.isAdmin = false;
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("user_data").child("users").child("administrators").child(uid);
        final ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showMenu();
                Log.d("Admin","You are an admin");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    /*private static void signIn(){

    }*/

    public static void attachListener(){
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public static void detachListener(){
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public static void connectToStorage(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("businesspics");
    }
}
