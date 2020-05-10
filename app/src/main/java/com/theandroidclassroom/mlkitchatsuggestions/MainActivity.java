package com.theandroidclassroom.mlkitchatsuggestions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.name)EditText mName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final EditText mobileEt = findViewById(R.id.mobile);
        Button login = findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                intent.putExtra("mobile",mobileEt.getText().toString());
                intent.putExtra("name",mName.getText().toString());
                startActivity(intent);
            }
        });
    }
}
