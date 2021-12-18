package test.pei.textdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import test.pei.textdetector.R;

public class MainActivity extends AppCompatActivity {

    private Button captureBtn;
    private Button translateBtn;
    private Button scannerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureBtn = findViewById(R.id.idBtnCapture);
        captureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(i);
            }
        });

        translateBtn = findViewById(R.id.idBtnTranslator);
        translateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TranslatorActivity.class);
                startActivity(i);
            }
        });

        scannerBtn = findViewById(R.id.idBtnCapture1);
        scannerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, scanActivity.class);
                startActivity(i);
            }
        });


    }
}