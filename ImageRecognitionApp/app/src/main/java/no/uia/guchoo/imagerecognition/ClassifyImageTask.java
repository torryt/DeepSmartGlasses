package no.uia.guchoo.imagerecognition;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;


public class ClassifyImageTask {
    private static ARActivity parent;
    File imageFile;
    RequestParams params = new RequestParams();
    // Local tunnel address. Is only temporary and WILL change.
    String uploadServerUri = "https://deepsmart.localtunnel.me/classify_upload";


    public void run(String filePath) {
        Log.d("ClassifyImageTask", "Creating File from image: " + filePath);
        imageFile = new File(filePath);
        Log.d("ClassifyImageTask", "Calling image upload");
        try {
            params.put("image_file", imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        makeHTTPCall();
    }

    private void makeHTTPCall() {
        AsyncHttpClient client = new SyncHttpClient();
        Log.d("makeHTTPCall", "Requesting API");

        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(uploadServerUri,
                params, new JsonHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("makeHTTPCall", "Status code: " + String.valueOf(statusCode));
                        Log.d("makeHTTPCall", "Response: " + response.toString());
                        showResponse(response);
                    }

                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.d("makeHTTPCall", "Request failed!\nStatus code: "+statusCode);
                    }

                });
    }
    private void showResponse(JSONObject resp){
        //Use to display text result
        String message = resp.toString();
        Toast toast = Toast.makeText(parent.getApplicationContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}