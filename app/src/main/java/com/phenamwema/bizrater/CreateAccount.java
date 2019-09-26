package com.phenamwema.bizrater;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class CreateAccount extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Model model;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private EditText email,password,password2,username;
    private String semail,spswrd,spswrd2,id,susername;
    private Button btnCreateAccount;
    private ImageView btnLoadPicture;
    private UploadTask storageTask;
    private int sharedRequestType;
    private RadioButton rbusiness, ruser;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //FirebaseApp.initializeApp(this);
        storageReference = FirebaseStorage.getInstance().getReference("user_images");
        databaseReference = FirebaseDatabase.getInstance().getReference("user_data");

        email = (EditText) findViewById(R.id.txEmail);
        password = (EditText) findViewById(R.id.txPswrd);
        username = (EditText) findViewById(R.id.txName);
        password2 = (EditText) findViewById(R.id.txPswrd2);
        rbusiness = (RadioButton) findViewById(R.id.rbusiness);
        ruser = (RadioButton) findViewById(R.id.ruser);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    return;
                }
            }
        };

        password2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password2.setError(null);
            }
        });
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setError(null);
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(storageTask !=null && storageTask.isInProgress()){
                    Toast.makeText(CreateAccount.this,"Upload in progress..",Toast.LENGTH_SHORT).show();
                }else{
                    checkConnection();//check internet connection

                }
            }
        });
    }

    private String checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if(activeNetwork != null){
            Register();//register new user
        }else {
            Toast.makeText(this,"You are not connected to the internet.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void loadPic(){
        sharedRequestType = 2;
        if (isPermissionGranted()) {
            /*Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);*/
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,1);
        }
    }

    //get image file extension
    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mtm = MimeTypeMap.getSingleton();
        return mtm.getExtensionFromMimeType(cr.getType(uri));
    }

    private void Register(){
        semail = email.getText().toString().trim();
        spswrd = password.getText().toString().trim();
        spswrd2 = password2.getText().toString().trim();
        susername = username.getText().toString().trim();
        if (TextUtils.isEmpty(semail)) {
            //email is empty
            Toast.makeText(this,"Please enter your email address",Toast.LENGTH_SHORT).show();
            //stop function from executing further
            return;
        }
        if (TextUtils.isEmpty(spswrd)) {
            //password is empty
            Toast.makeText(this,"Please enter your password.",Toast.LENGTH_SHORT).show();
            //stop function from executing further
            return;
        }
        if (TextUtils.isEmpty(spswrd2)) {
            //confirm_password is empty
            Toast.makeText(this,"Please confirm your password.",Toast.LENGTH_SHORT).show();
            //stop function from executing further
            return;
        }
        //check if the length of password is atleast 6 characters long for both passwords
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    if(spswrd.length()<6){
                        password.setError("Password must be atleast 6 characters long. ");
                        handled = true;
                    }
                }
                return handled;
            }
        });

        password2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    if(spswrd2.length()<6){
                        password2.setError("Password must be atleast 6 characters long. ");
                        handled = true;
                    }
                }
                return handled;
            }
        });
        if (!spswrd.equals(spswrd2)) {
            //Toast.makeText(this,"Passsword mismatch.",Toast.LENGTH_SHORT).show();
            password2.setError("Password does not match the first one");
            //password2.setBackgroundColor(Color.RED);
            return;
        }

        mAuth.createUserWithEmailAndPassword(semail,spswrd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //user is successfully registered
                    id = mAuth.getCurrentUser().getUid();
                    String key = databaseReference.getKey();
                    model = new Model(semail,spswrd);
                    if(rbusiness.isChecked()){
                        databaseReference.child("users").child("administrators").child(id).setValue(model,susername);
                        userProfile();
                        Toast.makeText(CreateAccount.this, "You have registered sucessfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }else if(ruser.isChecked()){
                        databaseReference.child("users").child("regular").child(id).setValue(model,susername);
                        userProfile();
                        Toast.makeText(CreateAccount.this, "You have registered sucessfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }

                } else {
                    try{
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    //Toast.makeText(CreateAccount.this, "Could not register. Please try again"+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void userProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null) {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username.getText().toString().trim()).build();

            user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData()!=null){
            selectedImage = data.getData();
            Picasso.with(this)
                    .load(selectedImage)
                    .into(btnLoadPicture);
        }
    }
}
