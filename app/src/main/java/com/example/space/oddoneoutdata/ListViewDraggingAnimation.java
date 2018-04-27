package com.example.space.oddoneoutdata;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

public class ListViewDraggingAnimation extends AppCompatActivity {

    ArrayList<String> mCheeseList = new ArrayList<String>();
    public ArrayList<ArrayList<Map<String, String>>> sceneData = new ArrayList<>();
    ArrayList<String> imgList = new ArrayList<>();
    Uri firstImage;
    Uri secondImage;
    Uri thirdImage;
    ImageView img1,img2,img3;
    DynamicListView listView;
    String folderName = "oddoneout";
    String fileName = "oddoneout_scene_data.json";
    String path = "/oddoneout/oddoneout_scene_data.json";
    public static String pathname = Environment.getExternalStorageDirectory() + "/oddoneout/oddoneout_scene_data.json";
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isStoragePermissionGranted();



            File f = new File(Environment.getExternalStorageDirectory(), folderName);
            if (!f.exists()) {
                f.mkdirs();
            }
            File json = new File(Environment.getExternalStorageDirectory() + "/oddoneout/oddoneout_scene_data.json");
            if (!(json.exists()))
                try {
                    json.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_list_view);
            try {
                readFromJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            reloader();


            dragFunction();
            makeListView();
        }




    public String loadJson() {
        String json = null;
        try {
            FileInputStream fis = new FileInputStream(new File(pathname));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;


    }

    public void readFromJson() throws JSONException {

        sceneData.clear();

        JSONObject obj = new JSONObject(loadJson());
        JSONArray m_jArry = obj.getJSONArray("scenes");
        HashMap<String, String> m_li;
        String src;
        String answer;

        for (int i = 0; i < m_jArry.length(); i++) {
            ArrayList formList = new ArrayList<HashMap<String, String>>();
            JSONArray jo_very_inside = m_jArry.getJSONArray(i);
            for (int j = 0; j < jo_very_inside.length(); j++) {
                JSONObject jo_inside = jo_very_inside.getJSONObject(j);
                src = jo_inside.getString("src");
                answer = jo_inside.getString("answer");
                m_li = new HashMap<String, String>();
                m_li.put("src", src);
                m_li.put("answer", answer);
                formList.add(m_li);
            }
            sceneData.add(formList);

        }
    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                out.println("Permission is granted");
                //return true;
            } else {

                out.println("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                //return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            out.println("Permission is granted");
            //return true;
        }
    }


    public void makeListView(){


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                for (Map<String, String> innerMap : sceneData.get(position)) {
                    for (String key : innerMap.keySet()) {
                        if (key.equals("src")) {
                            String value = innerMap.get(key);
                            imgList.add(value);
                        }
                    }
                }
                firstImage = Uri.parse(imgList.get(0));
                secondImage = Uri.parse(imgList.get(1));
                thirdImage = Uri.parse(imgList.get(2));

                onReorderClicked(position);





            }
        });



    }

    public void reloader(){
        for (int i=0;i<sceneData.size();i++){
            mCheeseList.add("SCENE "+(i+1));
        }
    }

    public void onClick(View view){
        Intent addImageIntent = new Intent(this,AddImageActivity.class);
        startActivity(addImageIntent);

    }

    public void dragFunction(){
        StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.text_view, mCheeseList);
        listView = (DynamicListView) findViewById(R.id.listview);
        listView.setCheeseList(mCheeseList);
        listView.setSceneData(sceneData);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    }

    @Override
    public void onBackPressed() {



        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        if( this.doubleBackToExitPressedOnce = true){
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
           
        }
        // Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void onReorderClicked(int pos){
        Intent reorderImageIntent = new Intent(this,ReoderImageActivity.class);
        reorderImageIntent.putExtra("position",pos);
        reorderImageIntent.putExtra("sceneData",sceneData);

        startActivity(reorderImageIntent);

        onResume();
    }


    @Override
    public void onRestart()
    {  // After a pause OR at startup
        super.onRestart();
        startActivity(new Intent(this, ListViewDraggingAnimation.class));
        finish();

    }
}
