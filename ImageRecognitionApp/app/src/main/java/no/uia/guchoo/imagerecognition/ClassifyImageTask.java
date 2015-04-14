package no.uia.guchoo.imagerecognition;

import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by torrytufteland on 14/04/15.
 */
public class ClassifyImageTask extends AsyncTask<String, Void, String> {
    String encodedString = "";
    File image;

    RequestParams params = new RequestParams();

    // Local tunnel address. Is only temporary and will change.
    String uploadServerUri = "https://pqnmxggmri.localtunnel.me/classify_upload";

    @Override
    protected String doInBackground(String... params) {
        Log.d("ClassifyImageTask", "Creating File from image: " + params[0]);
        image = new File(params[0]);
        return "File created";
    }

    @Override
    protected void onPostExecute(String msg) {
        Log.d("ClassifyImageTask", "Calling image upload");
        try {
            params.put("image_file", new FileInputStream(image));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        makeHTTPCall();
    }


    private void makeHTTPCall() {
        Log.d("makeHTTPCall", "Invoking API");
        AsyncHttpClient client = new AsyncHttpClient();

        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(uploadServerUri,
                params, new JsonHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("makeHTTPCall", "Status code: " + String.valueOf(statusCode));
                    }

                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.d("makeHTTPCall", "Request failed!\nStatus code: "+statusCode);
                    }

                });
    }
}