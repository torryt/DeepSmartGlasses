package no.uia.guchoo.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;

/**
 * Created by torrytufteland on 14/04/15.
 */
public class ClassifyImageTask extends AsyncTask<String, Void, String> {
    String encodedString = "";
    RequestParams params = new RequestParams();
    String uploadServerUri = "http://10.0.2.2:5000/classify_upload";

    @Override
    protected String doInBackground(String... params) {
        Log.d("ClassifyImageTask", "Encoding image to string: " + params[0]);
        return encodeImageToString(params[0]);
    }

    @Override
    protected void onPostExecute(String msg) {
        Log.d("ClassifyImageTask", "Calling image upload");
        params.put("imagefile", encodedString);

        // Trigger Image upload
        makeHTTPCall();
    }


    private void makeHTTPCall() {
        Log.d("makeHTTPCall", "Invoking API");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(uploadServerUri,
                params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("Post.success", "Status code: "+String.valueOf(statusCode));
                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Log.d("AsyncHttpClient.post", "Requested resource not found");

                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Log.d("AsyncHttpClient.post", "Something went wrong at server end");
                        }
                        // When Http response code other than 404, 500
                        else {
                            Log.d("AsyncHttpClient.post", "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                    + statusCode);
                        }
                    }
                });
    }

    private String encodeImageToString(String imgPath) {
        BitmapFactory.Options options = null;

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Must compress the Image to reduce image size to make upload easy
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byte_arr = stream.toByteArray();
        // Encode Image to String
        String encodedString = Base64.encodeToString(byte_arr, Base64.DEFAULT);
        return encodedString;
    }
}