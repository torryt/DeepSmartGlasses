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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Guro on 26.03.2015.
 */
public class ARActivity extends ARViewActivity {
    Timer timer;
    String uploadServerUri = "http://10.0.2.2:5000/classify_upload";
    int serverResponseCode = 0;

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = takeImageAndSaveToSd();
                Log.i("loadContents", "Saved file to " + file.getAbsolutePath());

                String categories = classifyImage(file.getAbsolutePath());
                Log.d("loadContents",categories);
            }
        }).start();


//        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                File file = takeImageAndSaveToSd();
//            }
//        }, 0, 2000);

    }

    private String classifyImage(String sourceFileUri) {

        final String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int maxBufferSize = 1 * 1024 * 1024;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;

        File sourceFile = new File(sourceFileUri);

        if(!sourceFile.isFile()) {
            Log.e("classifyImage", "Source File not exist: "
                    +sourceFileUri);
            return null;
        }
        else{
            try {
                // Set up connection to API
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(uploadServerUri);

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("imagefile", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"imagefile\";filename=\""
                                + fileName + "\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                dos.writeBytes(boundary);


                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Request is: "
                        + conn.getHeaderFields());
                Log.i("uploadFile", "HTTP Response is: "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ARActivity.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return serverResponseMessage;
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(ARActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return String.valueOf(serverResponseCode);
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {
    }

    public File takeImageAndSaveToSd() {
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
            finish();
        }
        return file;
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

