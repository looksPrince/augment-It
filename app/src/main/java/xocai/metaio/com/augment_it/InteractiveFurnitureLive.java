package xocai.metaio.com.augment_it;

import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by 343-Guilty-Spark on 3/8/2015.
 */
public class InteractiveFurnitureLive extends ARViewActivity {

    public IGeometry mTiger;
    public IGeometry mlamp[]=new IGeometry[6];
    public int i=0;
   // private TrackingValues mTrackingValues;
   boolean mIsCloseToTiger;

    /**
     * Media Player to play the sound of the tiger
     */
    MediaPlayer mMediaPlayer;
    private int mGestureMask;
    private Vector2d mMidPoint;
    private GestureHandlerAndroid mGestureHandler;
    private  boolean rotate=false;
    /**
     * metaio SDK callback handler
     */
    private MetaioSDKCallbackHandler mCallbackHandler;

   private View m2DSLAMExtrapolationButton;


    boolean mPreview = true;

    /**
     * Whether to set tracking configuration on onInstantTrackingEvent
     */
    boolean mMustUseInstantTrackingEvent = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mGestureMask = GestureHandler.GESTURE_ALL;
        mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
        mMidPoint = new Vector2d();
        try
        {
           // mlamp = null;

            mCallbackHandler = new MetaioSDKCallbackHandler();
            mTiger = null;

            mIsCloseToTiger = false;
            mMediaPlayer = new MediaPlayer();
            FileInputStream fis =
                    new FileInputStream(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InstantTracking/meow.mp3"));
            mMediaPlayer.setDataSource(fis.getFD());
            mMediaPlayer.prepare();
            fis.close();

            m2DSLAMExtrapolationButton = mGUIView.findViewById(R.id.instant2DSLAMExtrapolationButton);


        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, " failed: " + e);
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mCallbackHandler.delete();
        mCallbackHandler = null;
        try
        {
            mMediaPlayer.release();
        }
        catch (Exception e)
        {
        }

    }


    /**
     * This method is regularly called in the rendering loop. It calculates the distance between
     * device and the target and performs actions based on the proximity
     */
    private void checkDistanceToTarget() {
        if (i == 5) {
            // get tracking values for COS 1
            TrackingValues tv = metaioSDK.getTrackingValues(1);

            // Note, you can use this mechanism also to detect if something is tracking or not.
            // (e.g. for triggering an action as soon as some target is visible on screen)
            if (tv.isTrackingState()) {
                // calculate the distance as sqrt( x^2 + y^2 + z^2 )
                final float distance = tv.getTranslation().norm();

                // define a threshold distance
                final float threshold = 200;

                // moved close to the tiger
                if (distance < threshold) {
                    // if not already close to the model
                    if (!mIsCloseToTiger) {
                        MetaioDebug.log("Moved close to the tiger");
                        mIsCloseToTiger = true;
                        playSound();
                        mTiger.startAnimation("tap");
                    }
                } else {
                    if (mIsCloseToTiger) {
                        MetaioDebug.log("Moved away from the tiger");
                        mIsCloseToTiger = false;
                    }
                }

            }
        }
    }

    /**
     * Play sound that has been loaded
     */
    private void playSound()
    {
        try
        {
            MetaioDebug.log("Playing sound");
            mMediaPlayer.start();
        }
        catch (Exception e)
        {
            MetaioDebug.log("Error playing sound: " + e.getMessage());
        }
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
                if(!rotate)Toast.makeText(InteractiveFurnitureLive.this, "Rotating", Toast.LENGTH_SHORT).show();
                rotate=!rotate;
                return true;


            case R.id.menu_buy:
                Toast.makeText(InteractiveFurnitureLive.this, "Taking you the buy portal", Toast.LENGTH_SHORT).show();
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
                myWebLink.setData(Uri.parse("http://google.com"));
                startActivity(myWebLink);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected int getGUILayout()
    {
        return R.layout.instant_tracking;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return mCallbackHandler;
    }

    @Override
    public void onDrawFrame()
    {
        super.onDrawFrame();
        checkDistanceToTarget();
        if (metaioSDK != null)
        {
            // get all detected poses/targets
            TrackingValuesVector poses = metaioSDK.getTrackingValues();

            //if we have detected one, attach our metaio man to this coordinate system Id
            if (poses.size() != 0 && rotate)
            {
                poses.get(0).getCoordinateSystemID();
                mlamp[i].setRotation(new Rotation(0f, 0f, 0.01f), true);
            }
        }

    }

    public void onButtonClick(View v)
    {

        finish();
    }

    public void onRight(){
        mlamp[i].setVisible(false);mPreview=false;
        if(i!=5)i++;else i=0;
       // geometry=mlamp[i];
        metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY_SLAM_EXTRAPOLATED", new File(""), mPreview);
        mPreview = !mPreview;
    }

    public void onLeft(){
        mlamp[i].setVisible(false);mPreview=false;
        if(i!=0)i--;else i=5;
        //geometry=mlamp[i];
        metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY_SLAM_EXTRAPOLATED", new File(""), mPreview);
        mPreview = !mPreview;
    }

    public void on2DSLAMExtrapolationButtonClicked(View v)
    {
        mMustUseInstantTrackingEvent = true;
        mlamp[i].setVisible(false);


        metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY_SLAM_EXTRAPOLATED", new File(""), mPreview);
        mPreview = !mPreview;

        ImageButton button = (ImageButton)v;
        button.setSelected(!button.isSelected());

        if (button.isSelected())
        {
            button.setImageResource(R.drawable.button_chair_selected);


        }
        else
            button.setImageResource(R.drawable.button_chair_unselected);

    }

    @Override
    protected void loadContents()
    {
        try
        {
            // Load chair model
             File ModelPath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "InteractiveFurniture/Assets/stuhl.obj");

            if(ModelPath!=null){
            mlamp[0] = metaioSDK.createGeometry(ModelPath);

            // Set geometry properties and initially hide it
            mlamp[0].setScale(40f);
            mlamp[0].setTranslation(new Vector3d(0f, 0f, 0f));
            mlamp[0].setRotation(new Rotation((float) Math.PI / 2f, 0f, 0f));
            mlamp[0].setVisible(false);
            mGestureHandler.addObject(mlamp[0],1);
            MetaioDebug.log("Loaded geometry " + ModelPath);

               // geometry=mlamp[0];
            }

            ModelPath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "InteractiveFurniture/Assets/lamp1.mfbx");
            if(ModelPath!=null){
                mlamp[1] = metaioSDK.createGeometry(ModelPath);

                // Set geometry properties and initially hide it
                mlamp[1].setScale(30f);
                mlamp[1].setTranslation(new Vector3d(0f, 0f, 0f));
                mlamp[1].setRotation(new Rotation((float) Math.PI / 2f, 0f, 0f));
                mlamp[1].setVisible(false);
                mGestureHandler.addObject(mlamp[1],2);
                MetaioDebug.log("Loaded geometry " + ModelPath);
            }
            ModelPath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "InteractiveFurniture/Assets/lamp2.mfbx");
            if(ModelPath!=null){
                mlamp[2] = metaioSDK.createGeometry(ModelPath);

                // Set geometry properties and initially hide it
                mlamp[2].setScale(3f);
                mlamp[2].setTranslation(new Vector3d(0f, 0f, 0f));
                mlamp[2].setRotation(new Rotation((float) Math.PI / 2f, 0f, 0f));
                mlamp[2].setVisible(false);
                mGestureHandler.addObject(mlamp[2],3);
                MetaioDebug.log("Loaded geometry " + ModelPath);
            }
            ModelPath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "InteractiveFurniture/Assets/lamp3.mfbx");
            if(ModelPath!=null){
                mlamp[3] = metaioSDK.createGeometry(ModelPath);

                // Set geometry properties and initially hide it
                mlamp[3].setScale(3f);
                mlamp[3].setTranslation(new Vector3d(0f, 0f, 0f));
                mlamp[3].setRotation(new Rotation((float) Math.PI , 0f, 0f));
                mlamp[3].setVisible(false);
                mGestureHandler.addObject(mlamp[3],4);
                MetaioDebug.log("Loaded geometry " + ModelPath);
            }
            ModelPath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "InteractiveFurniture/Assets/lamp4.mfbx");
            if(ModelPath!=null){
                mlamp[4] = metaioSDK.createGeometry(ModelPath);

                // Set geometry properties and initially hide it
                mlamp[4].setScale(0.3f);
                mlamp[4].setTranslation(new Vector3d(0f, 0f, 0f));
                mlamp[4].setRotation(new Rotation((float) Math.PI/2f, 0f, 0f));
                mlamp[4].setVisible(false);
                mGestureHandler.addObject(mlamp[4],5);
                MetaioDebug.log("Loaded geometry " + ModelPath);
            }

              ModelPath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "InstantTracking/tiger.md2");
           if(ModelPath!=null){
            mTiger = metaioSDK.createGeometry(ModelPath);

            // Set geometry properties and initially hide it
            mTiger.setScale(8f);
            mTiger.setRotation(new Rotation(0f, 0f, (float)Math.PI));
            mTiger.setVisible(false);
            mTiger.setAnimationSpeed(60f);
            mTiger.startAnimation("meow");
               mlamp[5]=mTiger;
            MetaioDebug.log("Loaded geometry " + ModelPath);
        }
        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + e.getMessage());
        }
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry)
    {
        playSound();
        geometry.startAnimation("tap");
    }

    final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {

        @Override
        public void onSDKReady() {
            // show GUI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGUIView.setVisibility(View.VISIBLE);
                }
            });
        }


        @Override
        public void onInstantTrackingEvent(boolean success, File filePath) {
            if (success) {
                // Since SDK 6.0, INSTANT_3D doesn't create a new tracking configuration anymore
                // (see changelog)
                if (mMustUseInstantTrackingEvent) {
                    MetaioDebug.log("MetaioSDKCallbackHandler.onInstantTrackingEvent: " + filePath.getPath());
                    metaioSDK.setTrackingConfiguration(filePath);
                }

                mlamp[i].setVisible(true);
            } else {
                MetaioDebug.log(Log.ERROR, "Failed to create instant tracking configuration!");
            }
        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String animationName)
        {
            // Play a random animation from the list
            final String[] animations = {"meow", "scratch", "look", "shake", "clean"};
            final int random = (int)(Math.random() * animations.length);
            geometry.startAnimation(animations[random]);
        }
    }
}
