package com.bassiouny.naqalati.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.Utils;
import com.bassiouny.naqalati.model.FirebaseRoot;
import com.bassiouny.naqalati.model.SpecialRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SpecialRequestActivity extends AppCompatActivity implements View.OnClickListener {


    private TextInputEditText etName;
    private TextInputEditText etAddress;
    private TextInputEditText etEmail;
    private TextInputEditText requestType;
    private TextInputEditText size;
    private TextInputEditText startpoint;
    private TextInputEditText endpoint;
    private TextInputEditText pay;
    private TextInputEditText number;
    private TextInputEditText cartype;
    private TextInputEditText numberofcar;
    private TextInputEditText et_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_request);
        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        etEmail = findViewById(R.id.et_email);
        requestType = findViewById(R.id.request_type);
        size = findViewById(R.id.size);
        startpoint = findViewById(R.id.startpoint);
        endpoint = findViewById(R.id.endpoint);
        pay = findViewById(R.id.pay);
        number = findViewById(R.id.number);
        cartype = findViewById(R.id.cartype);
        numberofcar = findViewById(R.id.numberofcar);
        et_phone = findViewById(R.id.et_phone);
        findViewById(R.id.send).setOnClickListener(this);
        FirebaseDatabase.getInstance().getReference().child(FirebaseRoot.DB_DRIVER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(SpecialRequestActivity.this, dataSnapshot.getChildrenCount() + "", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("برجاء ادخال اسم الشركة");
            return;
        } else if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("برجاء ادخال البريد الالكترونى");
            return;
        } else {
            // send email
            Utils.showDialog(this);
            final String body = "الاسم :" + etName.getText().toString() + "\n"
                    + "رقم الهاتف :" + et_phone.getText().toString() + "\n"
                    + "العنوان: " + etAddress.getText().toString() + "\n"
                    + "البريد الالكترونى :" + etEmail.getText().toString() + "\n"
                    + "نوع الحمولة:" + requestType.getText().toString() + "\n"
                    + "الوزن:" + size.getText().toString() + "\n"
                    + "مكان التحميل:" + startpoint.getText().toString() + "\n"
                    + "مكان النهاية:" + endpoint.getText().toString() + "\n"
                    + "طرق الدفع:" + pay.getText().toString() + "\n"
                    + "عدد النقلات:" + number.getText().toString() + "\n"
                    + "نوع السيارة:" + cartype.getText().toString() + "\n"
                    + "عدد السيارات:" + numberofcar.getText().toString() + "\n";
            SpecialRequest specialRequest = new SpecialRequest(etName.getText().toString(),
                    et_phone.getText().toString(), etAddress.getText().toString(),
                    etEmail.getText().toString(), requestType.getText().toString(),
                    size.getText().toString(), startpoint.getText().toString(),
                    endpoint.getText().toString(), pay.getText().toString(),
                    number.getText().toString(), cartype.getText().toString(),
                    numberofcar.getText().toString());
            String key = FirebaseDatabase.getInstance().getReference().child(FirebaseRoot.DB_SPECAIL_REQUEST)
                    .push().getKey();
            FirebaseDatabase.getInstance().getReference().child(FirebaseRoot.DB_SPECAIL_REQUEST)
                    .child(key).setValue(specialRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent send = new Intent(Intent.ACTION_SENDTO);
                        String uriText = "mailto:" + Uri.encode("anwar.abdo1980@gmail.com") +
                                "?subject=" + Uri.encode("نقلتى") +
                                "&body=" + Uri.encode(body);
                        Uri uri = Uri.parse(uriText);
                        Utils.dismissDialog();
                        send.setData(uri);
                        startActivity(Intent.createChooser(send, "Send mail..."));
                    } else {
                        Utils.dismissDialog();
                        Utils.showErrorDialog(SpecialRequestActivity.this, "ﻻ نستطيع ارسال الطلب الان");
                    }
                }
            });

        }
    }
}
