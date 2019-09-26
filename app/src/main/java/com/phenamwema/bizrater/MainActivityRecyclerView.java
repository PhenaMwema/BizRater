package com.phenamwema.bizrater;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivityRecyclerView extends RecyclerView.Adapter<MainActivityRecyclerView.ViewHolder> {

    ArrayList<Model> list;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    MainActivity context;
    private ImageView businessImage;

    public MainActivityRecyclerView(){
        FirebaseUtil.Reference("businesses",context);
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        list = FirebaseUtil.list;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Model model = dataSnapshot.getValue(Model.class);
                list.add(model);
                notifyItemInserted(list.size()-1);
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
    
    @NonNull
    @Override
    public MainActivityRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_business,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityRecyclerView.ViewHolder viewHolder, int i) {

        Model model = list.get(i);
        viewHolder.bind(model);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, description,location,category;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bname);
            description = itemView.findViewById(R.id.bdescription);
            //category = itemView.findViewById(R.id.);
            //location = itemView.findViewById(R.id.tvLocation);
            cardView = itemView.findViewById(R.id.card_business);
            businessImage = (ImageView) itemView.findViewById(R.id.bimage);
            itemView.setOnClickListener(this);
        }

        public void bind(Model model) {
            title.setText(model.getName());
            description.setText(model.getDescription());
            //location.setText(model.getLocation());
            //category.setText(model.getCategory());
            showImage(model.getImageUrl());
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Model selectBusiness = list.get(position);
            Intent intent = new Intent(view.getContext(),BusinessProfile.class);
            intent.putExtra("Deal", (Serializable) selectBusiness);
            view.getContext().startActivity(intent);
        }

        private void showImage(String url){
            if(url != null && url.isEmpty()==false){
                Picasso.with(itemView.getContext())
                        .load(url)
                        .resize(300,300)
                        .centerCrop()
                        .into(businessImage);
            }
        }
    }
}
