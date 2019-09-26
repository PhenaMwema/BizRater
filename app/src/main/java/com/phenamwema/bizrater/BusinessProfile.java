package com.phenamwema.bizrater;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class BusinessProfile extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText bname, bdescription, bcategory, blocation;
    Model model;
    private String prev_activity;
    ImageView imageView;
    UploadTask storageTask;
    private static final int PICTURE_RESULT = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("businesses");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.bimage2);
        bname = (EditText) findViewById(R.id.name);
        bdescription = (EditText) findViewById(R.id.description);
        bcategory = (EditText) findViewById(R.id.category);
        blocation = (EditText) findViewById(R.id.location);

        Intent intent = getIntent();
        model = (Model) intent.getSerializableExtra("Deal");
        if(model == null){
            model = new Model();
        }
        prev_activity = intent.getStringExtra("prev_activity");

        if(prev_activity!=null){

        }else{
            this.model = model;
            bname.setText(model.getName());
            bdescription.setText(model.getDescription());
            bcategory.setText(model.getCategory());
            blocation.setText(model.getLocation());
            showImage(model.getImageUrl());
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,"Insert picture"),PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                upload();
                Toast.makeText(this,"Saved!",Toast.LENGTH_LONG).show();
                clean();
                return true;
            case R.id.action_delete:
                deleteDeal();
                Toast.makeText(this,"Business has been deleted!",Toast.LENGTH_LONG).show();
                startActivity(new Intent(BusinessProfile.this,MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_save).setVisible(true);
            enableEditTexts(true);
            //findViewById(R.id.cardView).setEnabled(true);
        }else{
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            enableEditTexts(false);
            //findViewById(R.id.cardView).setEnabled(false);
        }
        return true;
    }
    
    private void upload(){
        model.setName(bname.getText().toString());
        model.setDescription(bdescription.getText().toString());
        model.setCategory(bcategory.getText().toString());
        model.setLocation(blocation.getText().toString());
        if(model.getId()==null){
            databaseReference.push().setValue(model);//insert object into database
        } else{
            databaseReference.child(model.getId()).setValue(model);
        }
    }

    private void deleteDeal(){
        if(model==null){
            Toast.makeText(this,"Please save model before deletion.",Toast.LENGTH_LONG).show();
            return;
        }
        databaseReference.child(model.getId()).removeValue();
        if(model.getImageUrl()!=null&&model.getImageUrl().isEmpty()==false){
            StorageReference pictureReference = FirebaseUtil.firebaseStorage.getReference().child(model.getImageUrl());
            pictureReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete image","Image deleted");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Delete image", "Failed to delete");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            StorageReference reference = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            reference.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri downloadUrl = uri.getResult();
                    model.setImageUrl(downloadUrl.toString());
                    showImage(downloadUrl.toString());
                }
            });
        }

    }

    private void enableEditTexts(boolean isEnabled){
        bname.setEnabled(isEnabled);
        bdescription.setEnabled(isEnabled);
        bcategory.setEnabled(isEnabled);
        blocation.setEnabled(isEnabled);
    }
    
    private void showImage(String url){
        if(url!=null && url.isEmpty() ==false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;//get width of the screen
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }

    private void clean(){
        //clear textviews and set focus to title edittext
        bname.setText("");
        bdescription.setText("");
        blocation.setText("");
        bcategory.setText("");
        imageView.setImageResource(0);
        bname.requestFocus();
    }
    public void showMenu(){
        invalidateOptionsMenu();
    }
}
