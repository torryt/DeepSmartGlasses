package no.uia.guchoo.imagerecognition;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class ARActivity extends ARViewActivity {
    Timer timer;

    @Override
    protected int getGUILayout() {
        return R.layout.ar_view;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {
    }


    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String filePath = takeImageAndSaveToSd();
                Log.i("loadContents", "Saved file to " + filePath);
                new ClassifyImageTask().classifyImage(filePath, ARActivity.this);
            }
        }, 3000, 5000);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onGeometryTouched(IGeometry geometry) {
    }


    public String takeImageAndSaveToSd() {
        Log.d("image", "Taking image");
        String dir = Environment.getExternalStorageDirectory().toString();
        File rootDir = new File(dir + File.separator + "album");
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }
        Random generator = new Random();
        int i = 10000;
        i = generator.nextInt(i);
        String fname = "GG-" + i + ".jpg";
        File file = new File(rootDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.flush();
            out.close();
        } catch (FileNotFoundException et) {
            et.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        requestScreenshot(fname);
        addImageGallery(file);

        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "Could not resolve file path : " + file.getName();
        }
    }

    private void deleteImagesOnDisk() {
        File albumDir = new File(Environment.getExternalStorageDirectory().toString()+ File.separator + "album");
        if (albumDir.isDirectory()) {
            for (File child : albumDir.listFiles()) child.delete();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    private void requestScreenshot(String fname){
        String imagePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "album" + File.separator + fname;
        metaioSDK.requestScreenshot(imagePath);
    }

    private void addImageGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void showResult(final JSONObject response) {
        ArrayList<ImageViewModel> resp = parseResult(response);
        sortImageViewModel(resp);
        DecimalFormat df = new DecimalFormat("#%");
        StringBuilder finalResult = new StringBuilder();

        for(int i=0;i<=2;i++) {
            String percentString = df.format(resp.get(i).Percent);
            String categoryString = resp.get(i).Category.toString();
            if(i==2)
                finalResult.append(categoryString + " : " + percentString);
            else
                finalResult.append(categoryString + " : " + percentString + "\n");
        }
        final String finalString = finalResult.toString();

        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(ARActivity.this,finalString,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
            }
        });
    }

    private void sortImageViewModel(ArrayList<ImageViewModel> resp) {
        List<ImageViewModel> sortedList = new ArrayList<>();

        // Sort list
        Collections.sort(resp,new Comparator<ImageViewModel>() {
            @Override
            public int compare(ImageViewModel lhs, ImageViewModel rhs) {
                return (int)Math.floor(rhs.Percent - lhs.Percent);
            }
        });

        for(int i = 0;i<resp.size();i++) {
            sortedList.add(resp.get(i));
        }
    }

    private ArrayList<ImageViewModel> parseResult(JSONObject jsonInput) {
        ArrayList<ImageViewModel> images = new ArrayList<>();

        Iterator<String> iter = jsonInput.keys();

        while (iter.hasNext()) {
            String key = iter.next();
            try {
                Float value = Float.parseFloat(jsonInput.get(key).toString());
                ImageViewModel image = new ImageViewModel(key, value);
                images.add(image);

            } catch (JSONException e) {
                // Something went wrong!
            }
        }
        return images;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  deleteImagesOnDisk();
        android.os.Process.killProcess(android.os.Process.myPid()); //Stop the app from restarting on exit
        System.exit(1);
    }
}

