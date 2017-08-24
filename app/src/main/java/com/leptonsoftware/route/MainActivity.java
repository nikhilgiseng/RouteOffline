package com.leptonsoftware.route;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.SearchView;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Constants;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;
import com.graphhopper.util.ProgressListener;
import com.graphhopper.util.StopWatch;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
public class MainActivity extends AppCompatActivity {
  /*  private Button btn;
    private EditText et1;
    private EditText et2;
    String source;
    String destination;

    private File mapsFolder;
    private MapView mapView;
    private ItemizedLayer<MarkerItem> itemizedLayer;*/
   private GraphHopper hopper;
    private MapView mapView;
    private File mapsFolder;
    private ItemizedLayer<MarkerItem> itemizedLayer;
    private GeoPoint start;
    private GeoPoint end;
    private PathLayer pathLayer;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/graphhopper/maps/");
        final File areaFolder = new File(mapsFolder, "north-america_us_new-york" + "-gh");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Tile.SIZE = Tile.calculateTileSize(getResources().getDisplayMetrics().scaledDensity);
        mapView=(MapView)findViewById(R.id.map);
        this.mapView.setClickable(true);
        mapView.map().layers().add(new MapEventsReceiver(mapView.map()));

        MapFileTileSource tileSource = new MapFileTileSource();
        tileSource.setMapFile("/storage/emulated/0/Download/graphhopper/maps/north-america_us_new-york-gh/north-america_us_new-york.map");
        //tileSource.setMapFile(new File(areaFolder, "/north-america_us_new-york" + ".map").getAbsolutePath());
        VectorTileLayer l = mapView.map().setBaseMap(tileSource);

        mapView.map().setTheme(VtmThemes.DEFAULT);
        mapView.map().layers().add(new BuildingLayer(mapView.map(), l));
        mapView.map().layers().add(new LabelLayer(mapView.map(), l));
        mapView.map().updateMap(true);
        // Markers layer
        itemizedLayer = new ItemizedLayer<>(mapView.map(), (MarkerSymbol) null);
        mapView.map().layers().add(itemizedLayer);

        // Map position
        GeoPoint mapCenter = tileSource.getMapInfo().boundingBox.getCenterPoint();
        //mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 15);
        mapView.map().setMapPosition(28.632027, 77.218793, 1 << 2000);

        //setContentView(mapView);

        start=new GeoPoint(28.632027, 77.218793);
        end=new GeoPoint(28.629896, 77.214192);
        itemizedLayer.addItem(createMarkerItem(start, R.drawable.marker_icon_green));
        mapView.map().updateMap(true);
        itemizedLayer.addItem(createMarkerItem(end, R.drawable.marker_icon_red));
        mapView.map().updateMap(true);


        new GHAsyncTask<Void, Void, Path>() {
            protected Path saveDoInBackground(Void... v) throws Exception {
                GraphHopper tmpHopp = new GraphHopper().forMobile();
                tmpHopp.load(new File(mapsFolder, "north-america_us_new-york").getAbsolutePath() + "-gh");
               // log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
                hopper = tmpHopp;
                return null;
            }

            protected void onPostExecute(Path o) {
                if (hasError()) {
                    //logUser("An error happened while creating graph:"
                            //+ getErrorMessage());
                } else {
                    //logUser("Finished loading graph. Long press to define where to start and end the route.");
                }

                //finishPrepare();
            }
        }.execute();




/*

        GHRequest req = new GHRequest(28.632027, 77.218793, 28.629896, 77.214192).
                setAlgorithm(Algorithms.DIJKSTRA_BI);
        req.getHints().
                put(Routing.INSTRUCTIONS, "true");
        GHResponse resp = hopper.route(req);
*/



        new AsyncTask<Void, Void, PathWrapper>() {
            float time;

            protected PathWrapper doInBackground(Void... v) {
                StopWatch sw = new StopWatch().start();

                GHRequest req = new GHRequest(28.632027, 77.218793, 28.629896, 77.214192).
                        setAlgorithm(Algorithms.DIJKSTRA_BI);
                req.getHints().
                        put(Routing.INSTRUCTIONS, "true");
                GHResponse resp = hopper.route(req);
                time = sw.stop().getSeconds();
                return resp.getBest();
            }

            protected void onPostExecute(PathWrapper resp) {
                if (!resp.hasErrors()) {
                    /*log("from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                            + toLon + " found path with distance:" + resp.getDistance()
                            / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                            + time + " " + resp.getDebugInfo());
                    logUser("the route is " + (int) (resp.getDistance() / 100) / 10f
                            + "km long, time:" + resp.getTime() / 60000f + "min, debug:" + time);*/

                    pathLayer = createPathLayer(resp);
                    mapView.map().layers().add(pathLayer);
                    mapView.map().updateMap(true);

                    Toast.makeText(MainActivity.this,"Nikhil Routing Done",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,"Nikhil Routing ERR",Toast.LENGTH_SHORT).show();
                }

            }
        }.execute();





    }

    private PathLayer createPathLayer(PathWrapper response) {
        Style style = Style.builder()
                .fixed(true)
                .generalization(Style.GENERALIZATION_SMALL)
                .strokeColor(0x9900cc33)
                .strokeWidth(4 * getResources().getDisplayMetrics().density)
                .build();
        PathLayer pathLayer = new PathLayer(mapView.map(), style);
        List<GeoPoint> geoPoints = new ArrayList<>();
        PointList pointList = response.getPoints();


        for (int i = 0; i < pointList.getSize(); i++)
            geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
        pathLayer.setPoints(geoPoints);




        return pathLayer;
    }

    private MarkerItem createMarkerItem(GeoPoint p, int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphics.drawableToBitmap(drawable);
        MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, 0.5f, 1);
        MarkerItem markerItem = new MarkerItem("", "", p);
        markerItem.setMarker(markerSymbol);
        return markerItem;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);

        searchView = (SearchView)item.getActionView();
        searchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Search Clicked",Toast.LENGTH_SHORT).show();
                Intent search = new Intent(MainActivity.this,search.class);
                startActivity(search);
            }
        });

       /* searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Toast.makeText(MainActivity.this,"Search Closed",Toast.LENGTH_SHORT).show();

                setContentView(mapView);
                return false;
            }
        });*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;

            }
            @Override
            public boolean onQueryTextChange(String newText) {
               // adapter.getFilter().filter(newText);
                return false;
            }


        });


        return super.onCreateOptionsMenu(menu);
    }

    class MapEventsReceiver extends Layer implements GestureListener {

        MapEventsReceiver(org.oscim.map.Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
           /* if (g instanceof Gesture.LongPress) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                return onLongPress(p);
            }*/
            return false;
        }
    }

}
