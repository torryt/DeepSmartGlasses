package no.uia.guchoo.imagerecognition;

import android.content.ContentValues;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Guro on 26.03.2015.
 */
public class ARActivity extends ARViewActivity {
    Timer timer;
 String dir = Environment.getExternalStorageDirectory().toString();
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

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String filePath = takeImageAndSaveToSd();
                Log.i("loadContents", "Saved file to " + filePath);
                new ClassifyImageTask().execute(filePath);
            }
        }, 3000, 8000);

//        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                takeImageAndSaveToSd();
//            }
//        },3000, 2000);
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

        if(metaioSDK!=null) {
            requestScreenshot(fname);
            addImageGallery(file);
        }
        else {
            timer.cancel();
             //   deleteImagesOnDisk(rootDir);
            finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
        }
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "Could not resolve file path : " + file.getName();
        }
    }

    private void deleteImagesOnDisk(File dir) {
           if (dir.isDirectory()) {
               for (File child : dir.listFiles())
                   deleteImagesOnDisk(child);
           }
            dir.delete();
    }

    private void requestScreenshot(String fname){
        String imagePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "album" + File.separator + fname;
        metaioSDK.requestScreenshot(imagePath);
    }
    private void showMessage(){
        //Use to display text result
        String message = "";
        Toast toast = Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,0);
        toast.show();
    }

    private void addImageGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    }

    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

