package com.leptonsoftware.route;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Hp on 21-Aug-17.
 */

public class search extends AppCompatActivity{
    private Button btn;
    private EditText et1;
    private EditText et2;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

    btn=(Button)findViewById(R.id.button);
    et1=(EditText)findViewById(R.id.editText);
    et2=(EditText)findViewById(R.id.editText2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main = new Intent(search.this,MainActivity.class);
                startActivity(main);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return false;


    }
}
