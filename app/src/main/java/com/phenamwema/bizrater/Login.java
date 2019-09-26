package com.phenamwema.bizrater;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText email,pswrd;
    String semail,spswrd;
    TextView register, no_data;
    Button btnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.txEmail);
        pswrd = (EditText) findViewById(R.id.txPswrd);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        register = (TextView) findViewById(R.id.tvRegister);

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

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(Login.this,CreateAccount.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });

    }

    private String checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if(activeNetwork != null){
            userLogin();//login
        }else {
            Toast.makeText(this,"You are not connected to the internet.", Toast.LENGTH_LONG).show();
        }
        return null;
    }


    private void userLogin()
    {
        semail = email.getText().toString().trim();
        spswrd = pswrd.getText().toString().trim();

        if (TextUtils.isEmpty(semail))
        {
            //email is empty
            //Toast.makeText(this,"Please enter your email address",Toast.LENGTH_SHORT).show();
            email.setError("Email is required");
            //stop function from executing further
            return;
        }

        if (TextUtils.isEmpty(spswrd))
        {
            //password is empty
            //Toast.makeText(this,"Please enter your password",Toast.LENGTH_SHORT).show();
            pswrd.setError("Password is required");
            //stop function from executing further
            return;
        }
        if(!isEmail(email))
        {
            email.setError("Please enter a valid email address");
        }

        mAuth.signInWithEmailAndPassword(semail,spswrd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(Login.this,"You have logged in sucessfully",Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else{
                            try{
                                throw task.getException();
                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                            //Toast.makeText(Login.this,"Could not sign in. Please try again"+task.getException(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    boolean isEmail(EditText editText)
    {
        CharSequence email = editText.getText().toString();
        return (Patterns.EMAIL_ADDRESS.matcher(email).matches());
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
}
