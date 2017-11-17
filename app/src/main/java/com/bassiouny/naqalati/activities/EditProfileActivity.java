package com.bassiouny.naqalati.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mvc.imagepicker.ImagePicker;
import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.Utils;
import com.bassiouny.naqalati.model.FirebaseRoot;
import com.bassiouny.naqalati.model.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout container;
    private CircleImageView profileImage;
    private TextView tvChooseImage;
    private ProgressBar progress;
    private EditText etUserName,etUserID,etUserAddress;
    private Uri photoUri;
    String userId;
    Button signout;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        container = findViewById(R.id.container);
        profileImage = findViewById(R.id.profile_image);
        tvChooseImage = findViewById(R.id.tv_choose_image);
        etUserName=findViewById(R.id.et_user_name);
        etUserID=findViewById(R.id.et_user_id);
        etUserAddress=findViewById(R.id.et_user_address);
        signout=findViewById(R.id.signout);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AwesomeInfoDialog(EditProfileActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.you_want_signout)
                        .setColoredCircle(R.color.dialogInfoBackgroundColor)
                        .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                        .setCancelable(true)
                        .setPositiveButtonText(getString(R.string.yes))
                        .setPositiveButtonbackgroundColor(R.color.red_logo)
                        .setPositiveButtonTextColor(R.color.white)
                        .setNegativeButtonText(getString(R.string.no))
                        .setNegativeButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
                        .setNegativeButtonTextColor(R.color.white)
                        .setPositiveButtonClick(new Closure() {
                            @Override
                            public void exec() {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(EditProfileActivity.this, SigninActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButtonClick(new Closure() {
                            @Override
                            public void exec() {
                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.btn_edit).setOnClickListener(this);
        progress = findViewById(R.id.progress);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadData();
        tvChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.pickImage(EditProfileActivity.this, getString(R.string.choose_image));

            }
        });
    }

    private void loadData() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progress.setVisibility(View.GONE);
                if(dataSnapshot==null)
                    finish();
                user=dataSnapshot.getValue(User.class);
                etUserName.setText(user.getUserName());
                etUserID.setText(user.getNumberID());
                etUserAddress.setText(user.getAddress());
                if(!user.getUserAvatar().isEmpty())
                    Utils.showImage(EditProfileActivity.this,user.getUserAvatar(),profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (etUserName.getText().toString().trim().isEmpty()) {
            etUserName.setError(getString(R.string.invalid_user_name));
            return;
        }else if(etUserID.getText().toString().length()!=14){
            etUserID.setError("برجاء ادخال رقم البطاقة بطريقة صحيحة");
            return;
        }
        progress.setVisibility(View.VISIBLE);
        signout.setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_edit).setVisibility(View.INVISIBLE);
        if(photoUri==null){
            // update name
            updateUser();
        }else {
            // update image and name
            uploadImage();
        }
    }

    private void uploadImage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(userId);
        storageRef.putFile(photoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        user.setUserAvatar(downloadUrl.toString());
                        updateUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Utils.showErrorDialog(EditProfileActivity.this, exception.getLocalizedMessage());
                    }
                });
    }

    private void updateUser(){
        user.setUserName(etUserName.getText().toString());
        user.setNumberID(etUserID.getText().toString());
        user.setAddress(etUserAddress.getText().toString());
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete())
                    onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photoBitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        if (photoBitmap != null && data != null) {
            profileImage.setImageBitmap(photoBitmap);
            photoUri = data.getData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditProfileActivity.this,HomeActivity.class));
        finish();
    }
}
