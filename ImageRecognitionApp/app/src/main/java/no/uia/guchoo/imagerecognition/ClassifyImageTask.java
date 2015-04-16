package no.uia.guchoo.imagerecognition;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;


public class ClassifyImageTask {
    String uploadServerUri = "https://deepsmart.localtunnel.me/classify_upload";


    public void classifyImage(String filePath, final ARActivity activity) {
        RequestParams params = new RequestParams();
        File imageFile = new File(filePath);

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
                        activity.showResult(response.toString());
                    }
                });
    }
    
}

