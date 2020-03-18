package com.shovo.phone_varification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Welcome extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private StorageReference profilereferance;
    private static final int GALLERY =1;


    private TextView datatext;
    private Button deletbtn,uplodbtn;
    private ImageView profileImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        datatext=findViewById(R.id.dataTextId);
        deletbtn=findViewById(R.id.deletId);
        uplodbtn=findViewById(R.id.btanId);
        profileImg=findViewById(R.id.imgId);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("User");
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
       profilereferance= FirebaseStorage .getInstance().getReference();
       databaseReference.keepSynced(true);


       String userId =currentUser.getUid();

       databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.hasChild("profile Image")){


                   String imagurl =dataSnapshot.child("profile Image").getValue().toString();
                   Picasso.get().load(imagurl).into(profileImg);
               }


           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });



         deletbtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 databaseReference.child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {

                     }
                 });

             }
         });

         uplodbtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent gallary =new Intent();
                 gallary.setType("image/*");
                 gallary.setAction(Intent.ACTION_GET_CONTENT);
                 startActivityForResult(Intent.createChooser(gallary,"Select an Image"),GALLERY);
             }
         });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==GALLERY && requestCode==RESULT_OK){

            Uri imageUri =data.getData();
            final String cUser = currentUser.getUid();
            final StorageReference filepath =profilereferance.child("Profile_img").child(cUser + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url =uri.toString();

                                databaseReference.child(cUser).child("profile Image ").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(Welcome.this, "Out put success", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                    }
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
