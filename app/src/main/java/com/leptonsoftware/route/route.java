package com.leptonsoftware.route;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by Hp on 22-Aug-17.
 */

public class route extends Fragment{
    private EditText source;
    private EditText destination;
    private Button route;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route, container, false);
        source=(EditText)rootView.findViewById(R.id.editText);
        destination=(EditText)rootView.findViewById(R.id.editText2);
        route=(Button)rootView.findViewById(R.id.button);



        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(source.getText().length()<3)
              {
                  Toast.makeText(route.getContext(),"Source Cannot be left Blank",Toast.LENGTH_SHORT).show();
                  source.setHintTextColor(Color.RED);
                  return;
              }

                if(destination.getText().length()<3)
                {
                    Toast.makeText(route.getContext(),"Destination Cannot be left Blank",Toast.LENGTH_SHORT).show();
                    destination.setHintTextColor(Color.RED);
                    return;
                }
                Toast.makeText(route.getContext(),"Calculating Route",Toast.LENGTH_SHORT).show();

            }
        });

        return rootView;
    }
}
