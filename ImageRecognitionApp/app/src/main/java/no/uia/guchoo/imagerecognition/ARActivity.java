package no.uia.guchoo.imagerecognition;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


/**
 * Created by Guro on 26.03.2015.
 */
public class ARActivity extends ARViewActivity {
    Timer timer;
    File imageFile;
    RequestParams params = new RequestParams();
    String uploadServerUri = "https://deepsmart.localtunnel.me/classify_upload";

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
        ClassifyImageTask.setContext(this);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String filePath = takeImageAndSaveToSd();
                Log.i("loadContents", "Saved file to " + filePath);

                classifyImage(filePath);
            }
        }, 3000, 5000);
    }

    private void classifyImage(String filePath) {
        Log.d("ClassifyImageTask", "Creating File from image: " + filePath);
        imageFile = new File(filePath);
        Log.d("ClassifyImageTask", "Calling image upload");
        try {
            params.put("image_file", imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AsyncHttpClient client = new SyncHttpClient();

        Log.d("makeHTTPCall", "Requesting API");

        client.post(uploadServerUri,
                params, new JsonHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("makeHTTPCall", "Status code: " + String.valueOf(statusCode));
                        Log.d("makeHTTPCall", "Response: " + response.toString());
                        showResult(response);
                    }

                    public void onFailure(int statusCode, Header[] headers, JSONObject response, Throwable e) {
                        Log.d("makeHTTPCall", "Request failed!\nStatus code: " + statusCode);
                    }

                });
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
        String imagePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "album" + File.separator + fname;
        metaioSDK.requestScreenshot(imagePath);
    }

    private void addImageGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    }

    public void showResult(final JSONObject response) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), response.toString() ,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
            }
        });
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
       // deleteImagesOnDisk();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}

