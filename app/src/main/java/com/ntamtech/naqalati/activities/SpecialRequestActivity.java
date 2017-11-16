package com.ntamtech.naqalati.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ntamtech.naqalati.R;

public class SpecialRequestActivity extends AppCompatActivity implements View.OnClickListener {


    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText requestType;
    private TextInputEditText requestDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_request);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        requestType = findViewById(R.id.request_type);
        requestDetails = findViewById(R.id.request_details);
        findViewById(R.id.send).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(etName.getText().toString().trim().isEmpty()){
            etName.setError("برجاء ادخال اسمك");
            return;
        }else if(etPhone.getText().toString().trim().isEmpty()){
            etName.setError("برجاء ادخال رقم تليفونك");
            return;
        }else {
            // send email
            String body = "الاسم :"+etName.getText().toString()+"\n"
                    +"رقم تليفون : "+etPhone.getText().toString()+"\n"
                    +"نوع الطلب :"+requestType.getText().toString()+"\n"
                    +"تفاصيل الطلب:"+requestDetails.getText().toString()+"\n";
            Intent send = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:" + Uri.encode("anwar.abdo1980@gmail.com") +
                    "?subject=" + Uri.encode("نقلتى") +
                    "&body=" + Uri.encode(body);
            Uri uri = Uri.parse(uriText);

            send.setData(uri);
            startActivity(Intent.createChooser(send, "Send mail..."));
        }
    }
}
