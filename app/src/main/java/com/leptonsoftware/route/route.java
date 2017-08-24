package com.leptonsoftware.route;
import android.app.TabActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.database.sqlite.SQLiteDatabase;
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
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.database.SQLException;
import android.view.View;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.leptonsoftware.route.TabbedMain.mapView;

/**
 * Created by Hp on 22-Aug-17.
 */

public class route extends Fragment{
    private CustomViewPager mViewPager;
    private EditText source;
    private EditText destination;
    private Button route;
    Cursor c = null;
    Double fromLat;
    Double fromLong;
    Double toLat;
    Double toLong;
    String DB_PATH = null;
    private static String DB_NAME = "geocode";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route, container, false);
        this.DB_PATH = "/data/data/" + this.getContext().getPackageName() + "/" + "databases/";
        source=(EditText)rootView.findViewById(R.id.editText);
        destination=(EditText)rootView.findViewById(R.id.editText2);
        route=(Button)rootView.findViewById(R.id.button);
        mViewPager = (CustomViewPager)rootView.findViewById(R.id.container);


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


                database_geocode myDbHelper = new database_geocode(route.getContext());
                String myPath = DB_PATH + DB_NAME;
                SQLiteDatabase sql=SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

                try {
                    myDbHelper.createDataBase();
                } catch (IOException ioe) {
                    throw new Error("Unable to create database");
                }
                try {
                    myDbHelper.openDataBase();
                } catch (SQLException sqle) {
                    throw sqle;
                }
                Toast.makeText(route.getContext(), "Success", Toast.LENGTH_SHORT).show();

                Cursor sourceCursor=sql.rawQuery("SELECT * FROM "+"geocode"+ " WHERE name LIKE '%"+source.getText().toString()+"%'", null);
                if (sourceCursor.moveToFirst())
                {
                    fromLat = Double.parseDouble(sourceCursor.getString(2));
                    fromLong = Double.parseDouble(sourceCursor.getString(3));
                    do {
                       Toast.makeText(route.getContext(),
                                "id: " + sourceCursor.getString(0) + "\n" +
                                        "Name: " + sourceCursor.getString(1) + "\n" +
                                        "Latitude: " + sourceCursor.getString(2) + "\n" +
                                        "Longitude:  " + sourceCursor.getString(3),
                                Toast.LENGTH_LONG).show();
                    } while (sourceCursor.moveToNext());
                }


                Cursor destinationCursor=sql.rawQuery("SELECT * FROM "+"geocode"+ " WHERE name LIKE '%"+destination.getText().toString()+"%'", null);
                if (destinationCursor.moveToFirst())

                {
                    toLat=Double.parseDouble(destinationCursor.getString(2));
                    toLong=Double.parseDouble(destinationCursor.getString(3));
                    do {
                        Toast.makeText(route.getContext(),
                                "id: " + destinationCursor.getString(0) + "\n" +
                                        "Name: " + destinationCursor.getString(1) + "\n" +
                                        "Latitude: " + destinationCursor.getString(2) + "\n" +
                                        "Longitude:  " + destinationCursor.getString(3),
                                Toast.LENGTH_LONG).show();
                    } while (destinationCursor.moveToNext());
                }
                   if(sourceCursor==null||destinationCursor==null)
                   {
                       Toast.makeText(route.getContext(),"Geocode Failed Try Again",Toast.LENGTH_SHORT).show();
                   }
                else
                   {
                      // TabHost host = (TabHost) getActivity().findViewById(android.R.id.tabhost);
                       //host.setCurrentTab(1);
                       TabbedMain.itemizedLayer.removeAllItems();  //WE ARE DOING THIS TO REMOVE THE PREVIOUS ROUTES AND POINTS
                       TabbedMain.mapView.map().layers().remove(TabbedMain.pathLayer);//WE ARE DOING THIS TO REMOVE THE PREVIOUS ROUTES AND POINTS
                       GeoPoint src=new GeoPoint(fromLat,fromLong);
                       GeoPoint dest=new GeoPoint(toLat,toLong);
                       TabbedMain.itemizedLayer.addItem(createMarkerItem(src, R.drawable.marker_icon_green));
                       mapView.map().updateMap(true);
                       TabbedMain.itemizedLayer.addItem(createMarkerItem(dest, R.drawable.marker_icon_red));
                       mapView.map().updateMap(true);


                       new AsyncTask<Void, Void, PathWrapper>() {
                           float time;

                           protected PathWrapper doInBackground(Void... v) {
                               StopWatch sw = new StopWatch().start();
                               GHRequest req = new GHRequest(fromLat, fromLong, toLat, toLong).
                                       setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
                               req.getHints().
                                       put(Parameters.Routing.INSTRUCTIONS, "true");
                               GHResponse resp = TabbedMain.hopper.route(req);
                               time = sw.stop().getSeconds();
                               return resp.getBest();
                           }

                           protected void onPostExecute(PathWrapper resp) {
                               if (!resp.hasErrors()) {
                                   //Toast.makeText(this,"Nikhil Routing Done",Toast.LENGTH_SHORT).show();
                                   TabbedMain.pathLayer = createPathLayer(resp);
                                   mapView.map().layers().add(TabbedMain.pathLayer);
                                   mapView.map().updateMap(true);
                   /* Viewport mapPosition =mapView.map().viewport();
                    mapPosition.setMaxX(77.12);
                    mapPosition.setMaxY(30.44);
                    mapPosition.setMinX(76.47);
                    mapPosition.setMinY(28.38);*/
                               } else {
                                   // Toast.makeText("Nikhil Routing Err!",Toast.LENGTH_SHORT).show();
                               }
                               //shortestPathRunning = false;
                           }
                       }.execute();

                       // mViewPager.setCurrentItem(mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager())).);
                      // mViewPager.setCu
                     // mViewPager.setCurrentItem(1,true);
                      /* Intent intent = new Intent(route.getContext(), map.class);
                       intent.putExtra("fromLat", fromLat);
                       intent.putExtra("fromLong", fromLong);
                       intent.putExtra("toLat", toLat);
                       intent.putExtra("toLong", toLong);
                       startActivity(intent);*/
                   }



            }
        });

        return rootView;
    }

    private MarkerItem createMarkerItem(GeoPoint p, int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphics.drawableToBitmap(drawable);
        MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, 0.5f, 1);
        MarkerItem markerItem = new MarkerItem("", "", p);
        markerItem.setMarker(markerSymbol);
        return markerItem;
    }

    private PathLayer createPathLayer(PathWrapper response) {
        Style style = Style.builder()
                .fixed(true)
                .generalization(Style.GENERALIZATION_SMALL)
                .strokeColor(Color.RED)
                .strokeWidth(4 * getResources().getDisplayMetrics().density)
                .build();
        PathLayer pathLayer = new PathLayer(TabbedMain.mapView.map(), style);
        List<GeoPoint> geoPoints = new ArrayList<>();
        PointList pointList = response.getPoints();


        for (int i = 0; i < pointList.getSize(); i++)
            geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
        pathLayer.setPoints(geoPoints);




        return pathLayer;
    }
}
