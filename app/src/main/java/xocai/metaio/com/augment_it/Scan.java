package xocai.metaio.com.augment_it;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Xocai on 16-01-2015.
 */
public class Scan extends ARViewActivity  {

    private  boolean rotate=false;
    private int mGestureMask;
    private Vector2d mMidPoint;
    private GestureHandlerAndroid mGestureHandler;
   private  IGeometry suit[]=new IGeometry[4];
  public int i=0;

    //ImageButton right_button=(ImageButton)findViewById(R.id.right_arrow);
  //   ImageButton left_button=(ImageButton)findViewById(R.id.left_arrow);

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mGestureMask = GestureHandler.GESTURE_ALL;
        mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
        mMidPoint = new Vector2d();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();


    }


    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        super.onTouch(v, event);

        mGestureHandler.onTouch(v, event);

        return true;
    }

    @Override
    public void onSurfaceChanged(int width, int height)
    {
        super.onSurfaceChanged(width, height);

        // Update mid point of the view
        mMidPoint.setX(width / 2f);
        mMidPoint.setY(height / 2f);
    }

    @Override
    protected int getGUILayout()
    {
        // Attaching layout to the activity
        return R.layout.scan;
    }



    @Override
    public void onDrawFrame()
    {
        super.onDrawFrame();

        if (metaioSDK != null)
        {
            // get all detected poses/targets
            TrackingValuesVector poses = metaioSDK.getTrackingValues();

            //if we have detected one, attach our metaio man to this coordinate system Id
            if (poses.size() != 0 && rotate)
            {
                poses.get(0).getCoordinateSystemID();
                suit[i].setRotation(new Rotation(0f, 0f,0.01f), true);
            }
        }
    }


    public void onButtonClick(View v)
    {
        finish();
    }

    public void onRight(){

        setVisibilitysuit(false,i);
        if(i!=2)i++;else i=0;
        setVisibilitysuit(true,i);


    }

    public void onLeft(){

        setVisibilitysuit(false,i);
        if(i!=0)i--;else i=2;
        setVisibilitysuit(true,i);

        //   left_button.setVisibility(View.INVISIBLE);
     //   right_button.setVisibility(View.VISIBLE);
       /* metaioModel=f1;
        left.setEnabled(false);
        left.setVisibility(View.INVISIBLE);
        right.setVisibility(View.VISIBLE);
        right.setEnabled(true);*/

    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.menu_left:
                // Single menu item is selected do something
                // Ex: launching new activity/screen or show alert message
                Toast.makeText(getApplicationContext(),"Previous Item",Toast.LENGTH_SHORT).show();
                onLeft();
                return true;

            case R.id.menu_right:
                onRight();
                Toast.makeText(getApplicationContext(),"Next Item",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_rotate:
                if(!rotate)Toast.makeText(Scan.this, "Rotating", Toast.LENGTH_SHORT).show();
                rotate=!rotate;
                return true;
            case                    R.id.menu_buy:
                Toast.makeText(Scan.this, "Taking you the buy portal", Toast.LENGTH_SHORT).show();
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
                myWebLink.setData(Uri.parse("http://google.com"));
                startActivity(myWebLink);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void loadContents()
    {   //left_button.setVisibility(View.INVISIBLE);
        try
        {
            // Getting a file path for tracking configuration XML file
            File trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(), "ScanIt/Assets/TrackingData_MarkerlessFast.xml");

            // Assigning tracking configuration
            boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
            MetaioDebug.log("Tracking data loaded: " + result);

            // Getting a file path for a 3D geometry
             File metaioModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "ScanIt/Assets/floornew.mfbx");


            if (metaioModel!=null)
            {
                // Loading 3D geometry

                 suit[0] = metaioSDK.createGeometry(metaioModel);
               //  geometry2=metaioSDK.createGeometry(f2);

                if (suit[0]!=null)
                {

                    // Set geometry properties
                    suit[0].setScale(60f);

                   // geometry.setTranslation(new Vector3d(0,-10,0));
                    suit[0].setTranslation(new Vector3d(0.0f,0.0f,0.0f));
                   suit[0].setRotation(new Rotation(new Vector3d(-3f*(float)Math.PI/4f,-(float)Math.PI,(float)Math.PI)) );

                    setVisibilitysuit(true,i);
                    mGestureHandler.addObject(suit[0],1);
                  //  Toast.makeText(getApplicationContext(),"loaded",Toast.LENGTH_LONG);
                    MetaioDebug.log(Log.DEBUG, " loading geometry: "+metaioModel);

                }
                else
                { MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioModel);
                //Toast.makeText(getApplicationContext(),"not loaded",Toast.LENGTH_LONG);
                }
            }

            // Getting a file path for a 3D geometry
             metaioModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "ScanIt/Assets/suitnew.mfbx");


            if (metaioModel!=null)
            {
                // Loading 3D geometry

                suit[1] = metaioSDK.createGeometry(metaioModel);
                //  geometry2=metaioSDK.createGeometry(f2);

                if (suit[1]!=null)
                {

                    // Set geometry properties
                    suit[1].setScale(20f);

                    // geometry.setTranslation(new Vector3d(0,-10,0));
                    suit[1].setTranslation(new Vector3d(0.0f,0.0f,0.0f));
                    suit[1].setRotation(new Rotation(new Vector3d((float)Math.PI/4f,0f,0f)) );

                    setVisibilitysuit(false,1);
                    mGestureHandler.addObject(suit[1],2);
                   // Toast.makeText(getApplicationContext(),"loaded",Toast.LENGTH_LONG);
                    MetaioDebug.log(Log.DEBUG, " loading geometry: "+metaioModel);

                }
                else
                {MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioModel);
              //  Toast.makeText(getApplicationContext(),"not loaded",Toast.LENGTH_LONG);
                }
            }

            // Getting a file path for a 3D geometry
            metaioModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "ScanIt/Assets/shoe.obj");


            if (metaioModel!=null)
            {
                // Loading 3D geometry

                suit[2] = metaioSDK.createGeometry(metaioModel);
                //  geometry2=metaioSDK.createGeometry(f2);

                if (suit[2]!=null)
                {

                    // Set geometry properties
                    suit[2].setScale(100f);

                    // geometry.setTranslation(new Vector3d(0,-10,0));
                    suit[2].setTranslation(new Vector3d(0.0f,-1.0f,0.0f));
                    suit[2].setRotation(new Rotation(new Vector3d(0f,0f,0f)) );

                    setVisibilitysuit(false,2);
                    mGestureHandler.addObject(suit[2],3);
                    // Toast.makeText(getApplicationContext(),"loaded",Toast.LENGTH_LONG);
                    MetaioDebug.log(Log.DEBUG, " loading geometry: "+metaioModel);

                }
                else
                {MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioModel);
                    //  Toast.makeText(getApplicationContext(),"not loaded",Toast.LENGTH_LONG);
                }
            }

            // Getting a file path for a 3D geometry
            metaioModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "ScanIt/Assets/gown.mfbx");


            if (metaioModel!=null)
            {
                // Loading 3D geometry

                suit[3] = metaioSDK.createGeometry(metaioModel);
                //  geometry2=metaioSDK.createGeometry(f2);

                if (suit[3]!=null)
                {

                    // Set geometry properties
                    suit[3].setScale(20f);

                    // geometry.setTranslation(new Vector3d(0,-10,0));
                    suit[3].setTranslation(new Vector3d(0.0f,0.0f,0.0f));
                    suit[3].setRotation(new Rotation(new Vector3d(3f*(float)Math.PI/2f,0f,0f)) );

                    setVisibilitysuit(false,3);
                    mGestureHandler.addObject(suit[3],4);
                    // Toast.makeText(getApplicationContext(),"loaded",Toast.LENGTH_LONG);
                    MetaioDebug.log(Log.DEBUG, " loading geometry: "+metaioModel);

                }
                else
                {MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioModel);
                    //  Toast.makeText(getApplicationContext(),"not loaded",Toast.LENGTH_LONG);
                }
            }
            
        }
        catch (Exception e)
        {
            MetaioDebug.printStackTrace(Log.ERROR, e);
        }


    }

    private void setVisibilitysuit(boolean visible,int i)
    {


        if (suit[i] != null)
        {
            suit[i].setVisible(visible);
        }
    }


    @Override
    protected void onGeometryTouched(IGeometry geometry)
    {
        // Not used in this tutorial
    }


    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        // No callbacks needed in this tutorial
        return null;
    }
}
