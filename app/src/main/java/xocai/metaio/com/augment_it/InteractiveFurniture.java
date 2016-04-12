package xocai.metaio.com.augment_it;

/**
 * Created by Xocai on 16-01-2015.
 */
// Copyright 2007-2014 metaio GmbH. All rights reserved.

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
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
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.StringVector;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;





public class InteractiveFurniture extends ARViewActivity
{
    private MetaioSDKCallbackHandler mCallbackHandler;

    private IGeometry mTV;
    private IGeometry mScreen;
    private IGeometry mChair;
  //  private boolean furniture=false;
    private GestureHandlerAndroid mGestureHandler;
    private TrackingValues mTrackingValues;
    private int mGestureMask;
    boolean mImageTaken;
    private Vector2d mMidPoint;
    private View mLayoutGeometries;
    private boolean rotate=false;
    //public IGeometry mfurniture[]=new IGeometry[3];
    private int i=0;

    /**
     * File where camera image will be temporarily stored
     */
    private File mImageFile;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mGestureMask = GestureHandler.GESTURE_ALL;
        mImageTaken = false;

        mCallbackHandler = new MetaioSDKCallbackHandler();
        mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
        mMidPoint = new Vector2d();

        mImageFile = new File(Environment.getExternalStorageDirectory(), "target.jpg");

    }


    @Override
    protected void onPause()
    {
        super.onPause();

        try
        {
            // Pause movie texture
            mScreen.pauseMovieTexture();
        }
        catch (NullPointerException e)
        {
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            // Resume movie texture
            mScreen.startMovieTexture(true);
        }
        catch (NullPointerException e)
        {
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mLayoutGeometries = mGUIView.findViewById(R.id.layoutGeometries);

        // if a tracking target image exists, then the app is still running in the background
        if (mImageFile.exists() && mTrackingValues != null)
        {
            // the tracking target has to be reset and so are the tracking values
            metaioSDK.setImage(mImageFile);
            metaioSDK.setCosOffset(1, mTrackingValues);

        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mCallbackHandler.delete();
        mCallbackHandler = null;

        // delete the tracking target image before exit if it has been generated
        if (mImageFile.exists())
        {
            boolean result = mImageFile.delete();
            MetaioDebug.log("The file has been deleted: " + result);
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
    protected int getGUILayout()
    {
        return R.layout.interactive_furniture;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return mCallbackHandler;
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
                if(mChair.isVisible())
                mChair.setRotation(new Rotation(0f, 0f, 0.01f), true);
                else if(mTV.isVisible()){
                    mTV.setRotation(new Rotation(0f, 0f, 0.01f), true);
                    mScreen.setRotation(new Rotation(0f, 0f, 0.01f), true);
            }
            }
        }

        // reset the location and scale of the geometries
        if (mImageTaken == true)
        {
            // load the dummy tracking config file
            boolean result = metaioSDK.setTrackingConfiguration("DUMMY");
            MetaioDebug.log("Tracking data Dummy loaded: " + result);

            metaioSDK.setCosOffset(1, mTrackingValues);

            mImageTaken = false;
        }


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

               // if(furniture){onLeft();}
                return true;

            case R.id.menu_right:
            //    if(furniture)onRight();
                return true;
            case R.id.menu_rotate:
                if(!rotate)Toast.makeText(InteractiveFurniture.this, "Rotating", Toast.LENGTH_SHORT).show();
                rotate=!rotate;
                return true;

            case R.id.menu_buy:
                Toast.makeText(InteractiveFurniture.this, "Taking you the buy portal", Toast.LENGTH_SHORT).show();
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
                myWebLink.setData(Uri.parse("http://google.com"));
                startActivity(myWebLink);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonClick(View v)
    {
        finish();
    }

    // called when the save screenshot button has been pressed
    public void onSaveScreen(View v)
    {
        // request screen shot
        metaioSDK.requestScreenshot();
    }

    // called when the reset button has been pressed
    public void onClearScreen(View v)
    {
        // Start camera again

        startCamera();

        // delete the tracking target if generated
        String imagepath = Environment.getExternalStorageDirectory().getPath() + "/target.jpg";
        File file = new File(imagepath);
        if (file.exists())
        {
            boolean result = file.delete();
            MetaioDebug.log("The file has been deleted: " + result);
        }

        // load the ORIENTATION tracking config file again
        boolean result = metaioSDK.setTrackingConfiguration("ORIENTATION_FLOOR");
        MetaioDebug.log("Tracking data loaded: " + result);

        // reset the geometry buttons to unselected and hide the geometries
        ImageButton button = (ImageButton)findViewById(R.id.buttonTV);
        button.setImageResource(R.drawable.button_tv_unselected);
        button = (ImageButton)findViewById(R.id.buttonChair);
        button.setSelected(false);
        button.setImageResource(R.drawable.button_chair_unselected);

        button.setSelected(false);

        setVisibleTV(false);
        setVisibleChair(false);

        mLayoutGeometries.setVisibility(View.GONE);
        mGUIView.bringToFront();
    }

    // called when the take picture button has been pressed
    public void onTakePicture(View v)
    {
        // take a picture using the SDK and save it to external storage
        metaioSDK.requestCameraImage(mImageFile);

    }


    // called when the TV button has been pressed
    public void onTVButtonClick(View v)
    {
        ImageButton button = (ImageButton)v;
        button.setSelected(!button.isSelected());

        if (button.isSelected())
        {
            button.setImageResource(R.drawable.button_tv_selected);

            // reset the location and scale of the geometries

            Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
            mTV.setTranslation(translation);
            mScreen.setTranslation(translation);
            mTV.setScale(100f);
            mScreen.setScale(100f);
        }
        else
            button.setImageResource(R.drawable.button_tv_unselected);
        setVisibleTV(button.isSelected());

    }

    public void onChairButtonClick(View v)
    {
        ImageButton button = (ImageButton)v;
        button.setSelected(!button.isSelected());

        if (button.isSelected())
        {
            button.setImageResource(R.drawable.button_chair_selected);

            Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
            mChair.setTranslation(translation);
            mChair.setScale(100f);
        }
        else
            button.setImageResource(R.drawable.button_chair_unselected);
        setVisibleChair(button.isSelected());

    }



    @Override
    protected void loadContents()
    {
        try
        {
            // TODO: Load desired tracking data for planar marker tracking
            boolean result = metaioSDK.setTrackingConfiguration("ORIENTATION_FLOOR");
            MetaioDebug.log("Tracking data loaded: " + result);

            // Load all the geometries
            // Load TV

            File filepath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InteractiveFurniture/Assets/tv.obj");
            if (filepath != null)
            {
                mTV = metaioSDK.createGeometry(filepath);

                if (mTV != null)
                {
                    mTV.setScale(100f);
                    mTV.setRotation(new Rotation((float)Math.PI / 2f, 0f, -(float)Math.PI / 4f));
                    mTV.setTranslation(new Vector3d(0f, 10f, 0f));

                    mGestureHandler.addObject(mTV, 1);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
                }
            }


            // Load screen
            filepath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InteractiveFurniture/Assets/screen.obj");
            if (filepath != null)
            {
                mScreen = metaioSDK.createGeometry(filepath);

                // the parameters for the screen should be exactly the same as the ones for the TV
                if (mScreen != null)
                {
                    mScreen.setScale(100f);
                    mScreen.setRotation(new Rotation((float)Math.PI / 2f, 0f, -(float)Math.PI / 4f));
                    mScreen.setTranslation(new Vector3d(0f, 10f, 0f));

                    mScreen.setMovieTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InteractiveFurniture/Assets/sintel.3g2"));
                    mScreen.startMovieTexture(true);

                    mGestureHandler.addObject(mScreen, 1);
                    setVisibleTV(false);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
                }
            }

            // Load chair
            filepath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InteractiveFurniture/Assets/stuhl.obj");
            if (filepath != null)
            {
                mChair = metaioSDK.createGeometry(filepath);

                if (mChair != null)
                {mChair.setScale(100f);
                    mChair.setTranslation(new Vector3d(0f, 0f, 0f));
                    mChair.setRotation(new Rotation((float)Math.PI / 2f, 0f, 0f));

                    mGestureHandler.addObject(mChair, 2);
                    setVisibleChair(false);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
                }
            }

            /*filepath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InteractiveFurniture/Assets/stuhl.obj");
            if (filepath != null)
            {
                mfurniture[1] = metaioSDK.createGeometry(filepath);

                if (mfurniture[1] != null)
                {
                    mfurniture[1].setScale(100f);
                    mfurniture[1].setTranslation(new Vector3d(0f, 0f, 0f));
                    mfurniture[1].setRotation(new Rotation((float)Math.PI / 2f, 0f, 0f));

                    mGestureHandler.addObject(mfurniture[1], 3);
                    setVisibility(false,1);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
                }
            }
            filepath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(),
                            "InteractiveFurniture/Assets/chair.obj");
            if (filepath != null)
            {
                mfurniture[2] = metaioSDK.createGeometry(filepath);

                if (mfurniture[2] != null)
                {
                    mfurniture[2].setScale(100f);
                    mfurniture[2].setTranslation(new Vector3d(0f, 0f, 0f));
                    mfurniture[2].setRotation(new Rotation((float)Math.PI / 2f, 0f, 0f));

                    mGestureHandler.addObject(mfurniture[2], 4);
                    setVisibility(false,2);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
                }
            }*/




        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "loadContents failed: " + e);
        }
    }

    private void setVisibleTV(boolean visible)
    {
        if (mTV != null && mScreen != null)
        {
            mTV.setVisible(visible);
            mScreen.setVisible(visible);
        }
        if (visible)
        {
            mScreen.startMovieTexture();
        }
        else
        {
            mScreen.stopMovieTexture();
        }
    }

    private void setVisibleChair(boolean visible)
    {
       if (mChair != null)
        {
            mChair.setVisible(visible);
        }
    }


    @Override
    protected void onGeometryTouched(final IGeometry geometry)
    {
        MetaioDebug.log("MetaioSDKCallbackHandler.onGeometryTouched: " + geometry);
    }

    final class MetaioSDKCallbackHandler extends IMetaioSDKCallback
    {
        /**
         * Get path to Pictures directory if it exists
         *
         * @return Path to Pictures directory on the device if found, else <code>null</code>
         */
        private File getPicturesDirectory()
        {
            File picPath = null;

            try
            {
                picPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File path = new File(picPath, "Metaio Example");
                boolean success = path.mkdirs() || path.isDirectory();
                if (!success)
                {
                    path = new File(Environment.getExternalStorageDirectory(), "Pictures");
                }
                success = path.mkdirs() || path.isDirectory();
                if (!success)
                {
                    path = Environment.getDataDirectory();
                }

                return path.getAbsoluteFile();
            }
            catch (Exception e)
            {
                return null;
            }
        }

        @Override
        public void onSDKReady()
        {
            // show GUI
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mGUIView.setVisibility(View.VISIBLE);
                }
            });
        }

        // callback function for taking images using SDK
        @Override
        public void onCameraImageSaved(final File filePath)
        {
            // save the tracking values in case the application exits improperly
            mTrackingValues = metaioSDK.getTrackingValues(1);
            mImageTaken = true;

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (filePath.getPath().length() > 0)
                    {
                        metaioSDK.setImage(filePath);
                        mLayoutGeometries.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        @Override
        public void onScreenshotImage(ImageStruct image)
        {
            final File directory = getPicturesDirectory();
            if (directory == null)
            {
                image.release();
                image.delete();

                MetaioDebug.log(Log.ERROR, "Could not find pictures directory, not saving screenshot");
                return;
            }

            // Creating directory
            directory.mkdirs();

            try
            {
                // Creating file
                final File screenshotFile = new File(directory, "screenshot_" + System.currentTimeMillis() + ".jpg");
                screenshotFile.createNewFile();

                FileOutputStream stream = new FileOutputStream(screenshotFile);

                boolean result = false;
                Bitmap bitmap = image.getBitmap();
                try
                {
                    result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                }
                finally
                {
                    // release screenshot ImageStruct
                    image.release();
                    image.delete();

                    stream.close();
                }

                if (!result)
                {
                    MetaioDebug.log(Log.ERROR, "Failed to save screenshot to " + screenshotFile);
                    return;
                }

                final String url =
                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
                                "screenshot_" + System.currentTimeMillis(), "screenshot");


                // Recycle the bitmap
                bitmap.recycle();
                bitmap = null;

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String message = "The screenshot has been added to the gallery.";
                        if (url == null)
                        {
                            message = "Unable to add the screen shot to the gallery";
                        }
                        else
                        {
                            MediaScannerConnection.scanFile(getApplicationContext(),
                                    new String[]{screenshotFile.getAbsolutePath()}, new String[]{"image/jpg"},
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                            MetaioDebug.log("Screen saved at path " + path);
                                        }
                                    });
                        }

                        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
            }
            catch (IOException e)
            {
                MetaioDebug.printStackTrace(Log.ERROR, e);
            }
        }
    }
}
