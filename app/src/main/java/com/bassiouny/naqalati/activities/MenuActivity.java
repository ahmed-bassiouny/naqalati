package com.bassiouny.naqalati.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bassiouny.naqalati.R;

public class MenuActivity extends AppCompatActivity {

    String[] values = new String[] {"الرسائل الجديدة",  "ارسال صورة للادارة" ,"الاتصال بنا"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ListView listView = findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        startActivity(new Intent(MenuActivity.this,UploadFileActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MenuActivity.this,ContactUsActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MenuActivity.this,MessageActivity.class));
                        break;
                }
            }
        });
    }
}
