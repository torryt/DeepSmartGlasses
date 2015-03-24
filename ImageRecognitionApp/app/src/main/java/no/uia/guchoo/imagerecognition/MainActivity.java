package no.uia.guchoo.imagerecognition;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.*;
import jp.epson.moverio.bt200.DisplayControl;

public class MainActivity extends ARViewActivity {

    @Override
    protected int getGUILayout() {
        return 0;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {

    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

    }

    /**
     * metaio SDK object

    protected IMetaioSDKAndroid metaioSDK;
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //String sign_str = "dCKk787jhXExgx5BH7NWkrssjWmjxZ4oSqg8kF423lo=";
        metaioSDK.setSeeThrough(true);

    }

    public void exitOnClick(View v){
        System.exit(0);
    }

    public void offOnClick(View v){
    DisplayControl dc = new DisplayControl(this);
        metaioSDK.setSeeThrough(true);
    }
    public void onOnClick(View v){
        DisplayControl dc = new DisplayControl(this);
        metaioSDK.setSeeThrough(false);
        dc.setBacklight(20);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
