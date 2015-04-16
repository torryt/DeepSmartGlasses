package no.uia.guchoo.imagerecognition;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;


public class ClassifyImageTask {
    File imageFile;
    RequestParams params = new RequestParams();
    String uploadServerUri = "https://deepsmart.localtunnel.me/classify_upload";
    private static Context context;
    Context c = ClassifyImageTask.getContext();



    public void run(String filePath) {
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

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context con) {
        context = con;
    }

    public void showResult(JSONObject response) {
       Toast toast = Toast.makeText(c.getApplicationContext(), response.toString() ,Toast.LENGTH_LONG);
       toast.setGravity(Gravity.BOTTOM, 0, 0);
       toast.show();
    }
}

