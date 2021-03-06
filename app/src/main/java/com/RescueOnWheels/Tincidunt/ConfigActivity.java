package com.RescueOnWheels.Tincidunt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        Button button = findViewById(R.id.button);
        final EditText ipText = findViewById(R.id.editText);
        final Context me = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, StreamActivity.class);
                intent.putExtra("ip", ipText.getText().toString());
                startActivity(intent);
            }
        });
    }
}
