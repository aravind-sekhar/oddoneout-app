package com.example.space.oddoneoutdata;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.squareup.picasso.Picasso;

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
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ReoderImageActivity extends AppCompatActivity implements View.OnDragListener, View.OnLongClickListener{


    public static final int FIRST_IMAGE_REQUEST = 20;
    public static final int SECOND_IMAGE_REQUEST = 30;
    public static final int FIRSTA_IMAGE_REQUEST = 50;
    public static final int SECONDA_IMAGE_REQUEST = 60;
    public static String pathname = Environment.getExternalStorageDirectory() + "/oddoneout/oddoneout_scene_data.json";
    public String destinationFolder = "/storage/emulated/0/oddoneout/";
    public Uri firstImageUri;
    public Uri secondImageUri;
    public Uri thirdImageUri;
    public Uri fourthImageUri;


    public Map<String, String> map1 = new HashMap<String, String>();
    public Map<String, String> map2 = new HashMap<String, String>();
    public Map<String, String> map1a = new HashMap<String, String>();
    public Map<String, String> map2a = new HashMap<String, String>();
    public ArrayList<Map<String, String>> data = new ArrayList<>();
    public ArrayList<ArrayList<Map<String, String>>> sceneData = new ArrayList<>();
    ArrayList<String> imgList = new ArrayList<>();
    private ImageView imgView1,imgView1a;
    private ImageView imgView2,imgView2a;

    private ImageView dropTarget, dropped;
    private String tagDropTarget, tagDroppedImage;
    public int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        try {
            position = bundle.getInt("position");

        }catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            readFromJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //get a reference to the imageview that holds tha image that the user will see
        imgView1 = (ImageView) findViewById(R.id.imgView1);
        imgView2 = (ImageView) findViewById(R.id.imgView2);


        imgView1a = (ImageView) findViewById(R.id.imgView1a);
        imgView2a = (ImageView) findViewById(R.id.imgView2a);

        showImages();





        imgView1.setOnLongClickListener(this);
        imgView2.setOnLongClickListener(this);


        imgView1.setOnDragListener(this);
        imgView2.setOnDragListener(this);

        imgView1a.setOnLongClickListener(this);
        imgView2a.setOnLongClickListener(this);


        imgView1a.setOnDragListener(this);
        imgView2a.setOnDragListener(this);

        //check whether the permission to read from and write to memory is given
        //isStoragePermissionGranted();

    }


    public void showImages(){
        for (Map<String, String> innerMap : sceneData.get(position)) {
            for (String key : innerMap.keySet()) {
                if (key.equals("src")) {
                    String value = innerMap.get(key);
                    imgList.add(value);
                }
            }
        }
        firstImageUri = Uri.parse(imgList.get(0));
        secondImageUri = Uri.parse(imgList.get(1));
        thirdImageUri = Uri.parse(imgList.get(2));
        fourthImageUri = Uri.parse(imgList.get(3));


        Picasso.with(getBaseContext()).load(firstImageUri).into(imgView1);
        Picasso.with(getBaseContext()).load(secondImageUri).into(imgView2);
        Picasso.with(getBaseContext()).load(thirdImageUri).into(imgView1a);
        Picasso.with(getBaseContext()).load(fourthImageUri).into(imgView2a);


    }

    public void onButtonClick(View v) {



            try {
                copyFileOrDirectory((firstImageUri.toString()), destinationFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                copyFileOrDirectory((secondImageUri.toString()), destinationFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                copyFileOrDirectory((thirdImageUri.toString()), destinationFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                copyFileOrDirectory((fourthImageUri.toString()), destinationFolder);
            } catch (Exception e) {
                e.printStackTrace();

            }

            {


                try {
                    addToMapFirst();
                    addToMapSecond();
                    modScenedata();
                    jsonWriter();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finish();

            }



    }



    public void onDeleteClicked(View view){
        sceneData.remove(position);
        try {
            jsonWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        finish();
    }



    public static void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());
            if (src.isDirectory()) {
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Function to copy selected image to the destination folder
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public void jsonWriter() throws IOException{





        File json = new File(Environment.getExternalStorageDirectory() + "/oddoneout/oddoneout_scene_data.json");
        if (json.exists())
            json.delete();
        FileOutputStream out = new FileOutputStream(json);
        JsonWriter jsonWriter = new JsonWriter((new OutputStreamWriter(out, "UTF-8")));
        jsonWriter.beginObject();
        jsonWriter.name("scenes").beginArray();


        for (ArrayList<Map<String, String>> scene : sceneData) {
            jsonWriter.beginArray();
            for (Map<String, String> entry : scene) {
                jsonWriter.beginObject();
                for (String key : entry.keySet()) {
                    jsonWriter.name(key).value(entry.get(key));
                }
                jsonWriter.endObject();
            }

            jsonWriter.endArray();

        }

        jsonWriter.endArray();
        jsonWriter.endObject();
        jsonWriter.close();
        out.close();

    }

    public void modScenedata(){
        data.add(0, map1);
        data.add(1, map2);
        data.add(2, map1a);
        data.add(3, map2a);

        sceneData.set(position,data);
    }


    //this method will be invoked when user clicks first imageview to add an image
    public void onFirstImageViewClicked(View v) {
        //invoke image gallery using an implicit intent
        Intent firstImagePickerIntent = new Intent(Intent.ACTION_PICK);
        //where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        //get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);
        // System.out.println(">>>>>>>>"+data);
        //set data and type. Get all image types
        firstImagePickerIntent.setDataAndType(data, "image/*");
        //we will invoke this activity and get something from it
        startActivityForResult(firstImagePickerIntent, FIRST_IMAGE_REQUEST);
    }

    //this method will be invoked when user clicks second imageview to add an image
    public void onSecondImageViewClicked(View v) {
        Intent secondImagePickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        secondImagePickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(secondImagePickerIntent, SECOND_IMAGE_REQUEST);
    }

    public void onFirstAImageViewClicked(View v) {
        //invoke image gallery using an implicit intent
        Intent firstImagePickerIntent = new Intent(Intent.ACTION_PICK);
        //where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        //get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);
        // System.out.println(">>>>>>>>"+data);
        //set data and type. Get all image types
        firstImagePickerIntent.setDataAndType(data, "image/*");
        //we will invoke this activity and get something from it
        startActivityForResult(firstImagePickerIntent, FIRSTA_IMAGE_REQUEST);
    }

    //this method will be invoked when user clicks second imageview to add an image
    public void onSecondAImageViewClicked(View v) {
        Intent secondImagePickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        secondImagePickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(secondImagePickerIntent, SECONDA_IMAGE_REQUEST);
    }




    //control comes here when gallery is opened
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //if we are here everything processed successfully
            if (requestCode == FIRST_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery

                //Address of the image on the SD card
                firstImageUri = data.getData();
                newImagePathMaker(firstImageUri);
                Picasso.with(getBaseContext()).load(firstImageUri).into(imgView1);
            }


            if (requestCode == SECOND_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery
                //Address of the image on the SD card
                secondImageUri = data.getData();
                newImagePathMaker(secondImageUri);
                Picasso.with(getBaseContext()).load(secondImageUri).into(imgView2);



            }


            if (requestCode == FIRSTA_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery

                //Address of the image on the SD card
                thirdImageUri = data.getData();
                newImagePathMaker(thirdImageUri);
                Picasso.with(getBaseContext()).load(thirdImageUri).into(imgView1a);
            }


            if (requestCode == SECONDA_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery
                //Address of the image on the SD card
                fourthImageUri = data.getData();
                newImagePathMaker(fourthImageUri);
                Picasso.with(getBaseContext()).load(fourthImageUri).into(imgView2a);



            }


        }

    }


    //to get exact URI of the image chosen

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public String newImagePathMaker(Uri imageUri) {
        String destfirstImagePath;
        destfirstImagePath = destinationFolder;
        String imgPath = getPath(imageUri);
        String newImgPath = "";
        imgPath = new StringBuilder(imgPath).reverse().toString();
        int length = imgPath.length();

        for (int i = 0; i < length; i++) {
            char c = imgPath.charAt(i);
            if (c == '/') {
                newImgPath = new StringBuilder(newImgPath).reverse().toString();

                break;
            } else {
                newImgPath += c;
            }
        }
        destfirstImagePath += newImgPath;
        System.out.println(destfirstImagePath);
        return "file:///" + destfirstImagePath;
    }



    public void addToMapFirst() {
        map1.put("src", (firstImageUri.toString()));
        map1.put("answer", "true");
        map1a.put("src", (thirdImageUri.toString()));
        map1a.put("answer", "false");

    }

    public void addToMapSecond() {
        map2.put("src", (secondImageUri.toString()));
        map2.put("answer", "false");
        map2a.put("src", (fourthImageUri.toString()));
        map2a.put("answer", "false");
    }


    @Override
    public boolean onLongClick(View imageView) {

        ClipData clipData = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(imageView);
        imageView.startDrag(clipData, shadowBuilder, imageView, 0);
        // imageView.setVisibility(View.INVISIBLE);
        return true;
    }


    @Override
    public boolean onDrag(View receivingLayoutView, DragEvent dragEvent) {

        View draggedImageView = (View) dragEvent.getLocalState();
        // ImageView draggedImageView = (ImageView) dragEvent.getLocalState();

        switch (dragEvent.getAction()) {

            case DragEvent.ACTION_DRAG_STARTED:


                return true;

            case DragEvent.ACTION_DRAG_ENTERED:

                return true;

            case DragEvent.ACTION_DRAG_LOCATION:

                return true;

            case DragEvent.ACTION_DRAG_EXITED:

                return true;

            case DragEvent.ACTION_DROP:

                dropTarget = (ImageView) receivingLayoutView;
                dropped = (ImageView) draggedImageView;

                tagDropTarget = (String) dropTarget.getTag();
                tagDroppedImage = (String) dropped.getTag();

                Drawable d = dropTarget.getDrawable();

                dropTarget.setImageDrawable(dropped.getDrawable());
                dropped.setImageDrawable(d);



                changeUri();


                return true;


            case DragEvent.ACTION_DRAG_ENDED:

                if (!dragEvent.getResult()) {

                    draggedImageView.setVisibility(View.VISIBLE);
                }

                return true;

            default:

                break;
        }
        return false;
    }

    public void changeUri(){

        if(tagDropTarget.equals("3") && tagDroppedImage.equals("1")){
            Uri tempUri = thirdImageUri;
            thirdImageUri = firstImageUri;
            firstImageUri = tempUri;

        }

        if(tagDropTarget.equals("3") && tagDroppedImage.equals("2")){
            Uri tempUri = thirdImageUri;
            thirdImageUri =secondImageUri;
            secondImageUri = tempUri;
        }

        if(tagDropTarget.equals("3") && tagDroppedImage.equals("4")){
            Uri tempUri = thirdImageUri;
            thirdImageUri =fourthImageUri;
            fourthImageUri = tempUri;
        }

        if(tagDropTarget.equals("2") && tagDroppedImage.equals("3")){
            Uri tempUri = secondImageUri;
            secondImageUri =thirdImageUri;
            thirdImageUri = tempUri;
        }

        if(tagDropTarget.equals("2") && tagDroppedImage.equals("1")){
            Uri tempUri = secondImageUri;
            secondImageUri =firstImageUri;
            firstImageUri = tempUri;
        }

        if(tagDropTarget.equals("2") && tagDroppedImage.equals("4")){
            Uri tempUri = secondImageUri;
            secondImageUri =fourthImageUri;
            fourthImageUri = tempUri;
        }



        if(tagDropTarget.equals("1") && tagDroppedImage.equals("2")){
            Uri tempUri = firstImageUri;
            firstImageUri =secondImageUri;
            secondImageUri = tempUri;
        }

        if(tagDropTarget.equals("1") && tagDroppedImage.equals("3")){
            Uri tempUri = firstImageUri;
            firstImageUri =thirdImageUri;
            thirdImageUri = tempUri;
        }

        if(tagDropTarget.equals("1") && tagDroppedImage.equals("4")){
            Uri tempUri = firstImageUri;
            firstImageUri =fourthImageUri;
            fourthImageUri = tempUri;
        }



        if(tagDropTarget.equals("4") && tagDroppedImage.equals("2")){
            Uri tempUri = fourthImageUri;
            fourthImageUri =secondImageUri;
            secondImageUri = tempUri;
        }

        if(tagDropTarget.equals("4") && tagDroppedImage.equals("3")){
            Uri tempUri = fourthImageUri;
            fourthImageUri =thirdImageUri;
            thirdImageUri = tempUri;
        }

        if(tagDropTarget.equals("4") && tagDroppedImage.equals("1")){
            Uri tempUri = fourthImageUri;
            fourthImageUri =firstImageUri;
            firstImageUri = tempUri;
        }



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































}

