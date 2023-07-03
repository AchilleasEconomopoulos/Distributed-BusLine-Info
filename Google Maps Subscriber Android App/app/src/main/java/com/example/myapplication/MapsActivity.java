package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.example.myapplication.Infra.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import static com.example.myapplication.Infra.Node.b;
import static com.example.myapplication.Infra.Node.info;

import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Spinner toSend;

    private Button btnConnect;

    private HashMap<Integer, NodeInfo> brokerTopics;
    private Map<String,Boolean> currentlySubscribed;
    private Map<String, ArrayList<Marker>> markersForCurrentlySubscribed;
    private Hashing h;

    private GoogleMap mMap;
    MarkerOptions markerOptions = new MarkerOptions(); // san constructor tou add marker

    public int getAnswer;
    public double latitude;
    public double longitude;
    public String busLineId;
    public String OnSnippet;
    boolean creation = true;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync( (OnMapReadyCallback) this );

        currentlySubscribed = new HashMap<>();
        markersForCurrentlySubscribed = new HashMap<>();
        initializeData();

        b.add(new NodeInfo(0, "10.0.2.2", 5050)); //CHANGE THIS IP

        brokerTopics = new HashMap<>();
        h = new Hashing();


        toSend = ((Spinner) findViewById(R.id.spinner));
        btnConnect = ((Button) findViewById(R.id.btnConnect));

        getAnswer=0;


        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String answer = toSend.getSelectedItem().toString();
                if(!currentlySubscribed.get(answer)) {
                    currentlySubscribed.put(answer,true);
                    AsyncTaskReceiver runner = new AsyncTaskReceiver();
                    runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{
                    currentlySubscribed.put(answer,false);
                }
            }
        });


    }


    private void errorMessage(String message){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(message);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }
    public void callOnMapReady(int code){
        getAnswer = code;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync( (OnMapReadyCallback) this );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(getAnswer == 1) {
            ArrayList<Marker> listToChange;


            LatLng latLng = new LatLng(latitude,longitude);
            markerOptions.position( latLng );
            markerOptions.title( busLineId );
            markerOptions.snippet( OnSnippet );
            Marker m = mMap.addMarker( markerOptions );
            listToChange = markersForCurrentlySubscribed.get(busLineId);
            listToChange.add(m);
            if (listToChange.size() > 3) {
                listToChange.get(0).remove();
                listToChange.remove(0);
            }
        }else if(getAnswer == 2){
            ArrayList<Marker> listToChange = markersForCurrentlySubscribed.get(busLineId);
            if(listToChange!=null) {
                for (int i = 0; i < listToChange.size(); i++)
                    listToChange.get(i).remove();

                markersForCurrentlySubscribed.remove(busLineId);
            }
        }

        if(creation) {
            LatLng athens = new LatLng(37.994129, 23.731960);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(athens));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
            creation = false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //xrhsimopoioume to easy permissions gia eukolia
        mMap.setMyLocationEnabled(true); // apo thn klash google maps logika gia na deixnei thn topo8esia tou xrhsth. Me alt-enter sumplhrwnei ton kwdika
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }


    private class AsyncTaskReceiver extends AsyncTask<String, String, Integer> {
        Socket con = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        int hashedTopic;
        String answer = toSend.getSelectedItem().toString();
        int result = 0;

        protected Integer doInBackground(String... params) {

            markersForCurrentlySubscribed.put(answer,new ArrayList<Marker>());

            if (brokerTopics.isEmpty()) {
                register(b.get(0), new Topic(answer));
            } else {
                Topic t = new Topic(answer);
                hashedTopic = h.getIntValue(h.hashMD5(t.getBusLine())) % 32;

                register(brokerTopics.get(hashedTopic), t);
            }


            return result;

        }

        protected void onPreExecute() {

        }

        protected void onPostExecute(Integer result) {
            if (result == 1){
                errorMessage("Updates for topic " + answer + " have timed out.");
            }else if(result==2){
                errorMessage("Sensor tracking topic " + answer + " is currently down. Try again later.");
            }
        }

        protected void onProgressUpdate(String... text) {

            if(text[0].compareTo("1")==0) {
                latitude = Double.parseDouble(text[2]);
                longitude = Double.parseDouble(text[3]);
                busLineId = text[1];
                OnSnippet = text[4];
                //                Toast.makeText( MapsActivity.this,String.valueOf(latitude) , Toast.LENGTH_SHORT ).show();
                callOnMapReady(1);
            }else {
                busLineId = text[1];
                callOnMapReady(2);
            }


        }

        public void connect() {
        }

        public void disconnect() {
            try {
                in.close();
                out.close();
                con.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void register(NodeInfo broker, Topic t) {
            String response;
            ArrayList<NodeInfo> infos;
            Hashing h = new Hashing();
            int hashedTopic = h.getIntValue(h.hashMD5(t.getBusLine())) % 32;
            ObjectMapper mapper = new ObjectMapper();

            try {
                con = new Socket(broker.getIP(), broker.getPort());
                out = new ObjectOutputStream(con.getOutputStream());
                in = new ObjectInputStream(con.getInputStream());

                out.writeUTF("sub");
                out.writeUTF(info.getID() + "");
                out.writeUTF("This is the topic I want to register for: ");
                out.writeUTF(mapper.writeValueAsString(t));
                out.flush();


                if (in.readUTF().compareTo("entry found") != 0) {
                    infos = new ArrayList<NodeInfo>();
                    infos = mapper.readValue(in.readUTF(),new TypeReference<ArrayList<NodeInfo>>(){});
                    int id;

                    for (int i = 0; i < infos.size(); i++) {
                        id = infos.get(i).getID();

                        if (id != b.get(0).getID()) { //If it's not the info of the subscriber's first contact
                            b.add(infos.get(i));
                            if(b.get(i).getIP().equals("127.0.0.1"))
                                b.get(i).setIP("10.0.2.2");
                        } else {
                            b.get(i).setFirst(infos.get(i).getFirst());
                            b.get(i).setLast(infos.get(i).getLast());
                        }

                    }

                    setHashMap();

                    Log.d("MESSAGE","Broker information has been acquired.");
                }

                response = in.readUTF();

                Log.d("MESSAGE",con.getInetAddress().getHostAddress() + " > " + response);

                if (response.compareTo("Topic located. Standby for updates.") == 0) {
                    while (currentlySubscribed.get(answer)) {
                        response = in.readUTF();
                        Log.d("MESSAGE",con.getInetAddress().getHostAddress() + " > " + response);

                        if (response.compareTo("TIMEOUT") == 0) {
                            currentlySubscribed.put(answer,false);
                            result = 1;
                            publishProgress("2",answer);
                            return;
                        }

                        if (response.compareTo("Sensor is down.") == 0) {
                            currentlySubscribed.put(answer,false);
                            result = 2;
                            publishProgress("2",answer);
                            return;
                        }

                        Value v = mapper.readValue(in.readUTF(), Value.class);

                        visualiseData(t, v);

                        out.writeUTF("renew");
                        out.flush();

                    }

                    publishProgress("2",answer);

                    out.writeUTF("disconnect");
                    out.flush();

                    disconnect();
                } else if (response.compareTo("Sensor is down.") == 0) {
                    disconnect();
                } else {
                    disconnect();
                    register(brokerTopics.get(hashedTopic), t);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void visualiseData(Topic t, Value v) {
            //Currently a simple print of the Value item
            publishProgress( "1",t.getBusLine(), String.valueOf(v.getLatitude()) ,String.valueOf(v.getLongitude()),v.getBus().getLineName());
        }

        private void setHashMap() {
            brokerTopics.clear();
            for (int i = 0; i < b.size(); i++) {
                if (b.get(i).getFirst() > b.get(i).getLast()) {
                    for (int j = b.get(i).getFirst(); j < 32; j++)
                        brokerTopics.put(j, b.get(i));
                    for (int j = b.get(i).getLast(); j >= 0; j--)
                        brokerTopics.put(j, b.get(i));
                } else {
                    for (int j = b.get(i).getFirst(); j <= b.get(i).getLast(); j++) {
                        brokerTopics.put(j, b.get(i));
                    }
                }
            }
        }
    }

    public void initializeData(){
        currentlySubscribed.put("021",false);
        currentlySubscribed.put("022",false);
        currentlySubscribed.put("024",false);
        currentlySubscribed.put("025",false);
        currentlySubscribed.put("026",false);
        currentlySubscribed.put("027",false);
        currentlySubscribed.put("032",false);
        currentlySubscribed.put("035",false);
        currentlySubscribed.put("036",false);
        currentlySubscribed.put("040",false);
        currentlySubscribed.put("046",false);
        currentlySubscribed.put("049",false);
        currentlySubscribed.put("051",false);
        currentlySubscribed.put("054",false);
        currentlySubscribed.put("057",false);
        currentlySubscribed.put("060",false);
        currentlySubscribed.put("1",false);
        currentlySubscribed.put("10",false);



    }


}