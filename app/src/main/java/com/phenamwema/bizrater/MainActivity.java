package com.phenamwema.bizrater;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BusinessProfile.class);
                intent.putExtra("prev_activity","new_business");
                startActivity(intent);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_login:
                //Toast.makeText(this,"Deal has been deleted",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this,Login.class));
                return true;
            case R.id.action_logout:
                //deleteDeal();
                //Toast.makeText(this,"Deal has been deleted",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this,Login.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.action_newbusiness).setVisible(true);
        }else{
            menu.findItem(R.id.action_newbusiness).setVisible(false);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.Reference("businesses",this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        MainActivityRecyclerView adapter = new MainActivityRecyclerView();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        FirebaseUtil.attachListener();
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }
}
