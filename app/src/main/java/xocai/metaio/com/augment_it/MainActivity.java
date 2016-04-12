package xocai.metaio.com.augment_it;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.ListActivity;
import android.widget.Toast;

import com.metaio.sdk.ARELActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {
    View mProgress;
    WebView mWebView;
    boolean mLaunching;
   AssetsExtracter mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.webview);
        super.onCreate(savedInstanceState);
        MetaioDebug.enableLogging(BuildConfig.DEBUG);

        mProgress = findViewById(R.id.progress);
        mWebView = (WebView) findViewById(R.id.webview);

        // extract all the assets
       mTask = new AssetsExtracter();
        mTask.execute(0);

        /*String[] values = new String[]{"SCAN", "INTERACTIVE FURNITURE"};
        // use your custom layout

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.panel, R.id.label, values);
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch( position ) {
                    case 0:  Intent newActivity = new Intent(getApplicationContext(), Scan.class);
                        startActivity(newActivity);
                        break;
                    case 1:  Intent newActivity2 = new Intent(getApplicationContext(), InteractiveFurniture.class);
                        startActivity(newActivity2);
                        break;

                }
            }
        });*/
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mWebView.resumeTimers();
        mLaunching = false;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mWebView.pauseTimers();
    }

    @Override
    public void onBackPressed()
    {
        // if web view can go back, go back
        if (mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        

        return super.onOptionsItemSelected(item);
    }
   // @Override
    /*protected void onListItemClick(ListView l, View v, int position, long id) {

        switch (position) {
            case 0:
                Intent newActivity = new Intent(this, Scan.class);
                startActivity(newActivity);
                break;
            case 1:
                Intent newActivity2 = new Intent(this, InteractiveFurniture.class);
                startActivity(newActivity2);
                break;
        }
    }*/
   private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
   {

       @Override
       protected void onPreExecute()
       {
           mProgress.setVisibility(View.VISIBLE);
       }

       @Override
       protected Boolean doInBackground(Integer... params)
       {
           try
           {
               // Extract all assets except Menu. Overwrite existing files for debug build only.
               final String[] ignoreList = {"Menu", "webkit", "sounds", "images", "webkitsec"};
               AssetsManager.extractAllAssets(getApplicationContext(), "", ignoreList, BuildConfig.DEBUG);
           }
           catch (IOException e)
           {
               MetaioDebug.printStackTrace(Log.ERROR, e);
               return false;
           }

           return true;
       }

      @Override
      protected void onPostExecute(Boolean result)
       {
           mProgress.setVisibility(View.GONE);

           if (result)
           {
               WebSettings settings = mWebView.getSettings();

              settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
               settings.setJavaScriptEnabled(true);

               mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
               mWebView.setWebViewClient(new WebViewHandler());
              mWebView.loadUrl("file:///android_asset/Menu/index.html");
               mWebView.setVisibility(View.VISIBLE);
               MetaioDebug.log(Log.DEBUG, "Successful extracting assets");

           }
           else
           {
               MetaioDebug.log(Log.ERROR, "Error extracting assets, closing the application...");
               finish();
           }
           }
      }

    private void showToast(final String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    class WebViewHandler extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            mProgress.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if (!url.toLowerCase(Locale.US).startsWith("app"))
            {
                if (url.contains("metaio.com"))
                {
                    // Open external browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
            else if (mLaunching)
            {
                return true;
            }

            startOption(url);
            return true;
        }

        /**
         * Start a Native or AREL tutorial from local URL
         * @param url URL with prefix metaiosdkexample:// or metaiosdkexamplearel://
         */
        private void startOption(final String url)
        {
            final String Id = url.substring(url.lastIndexOf("=") + 1);
            if (Id == null || Id.length() == 0)
            {
                MetaioDebug.log(Log.ERROR, "Invalid  URL: "+url);
                showToast("Invalid  URL: "+url);
                return;
            }

            MetaioDebug.log(" ID detected: "+Id);

            if (url.toLowerCase(Locale.US).startsWith("app://"))
            {
                try
                {
                    final Class<?> activity = Class.forName(getPackageName()+"."+Id);
                    mLaunching = true;
                    startActivity(new Intent(getApplicationContext(), activity));
                }
                catch (ClassNotFoundException e)
                {
                    MetaioDebug.log(Log.ERROR, "Invalid  id, class not found: "+Id);
                    showToast("Invalid id, class not found: "+Id);
                }
            }
           /* else if (url.toLowerCase(Locale.US).startsWith("metaiosdkexamplearel://"))
            {
                final String arelConfigFile = "index.xml";
                final File arelConfigFilePath = AssetsManager.getAssetPathAsFile(getApplicationContext(), "Tutorial"+tutorialId+"/"+arelConfigFile);
                if (arelConfigFilePath != null)
                {
                    MetaioDebug.log("AREL configuration to be passed to intent: "+arelConfigFilePath.getPath());
                    Intent intent = new Intent(getApplicationContext(), ARELViewActivity.class);
                    intent.putExtra(getPackageName()+ ARELActivity.INTENT_EXTRA_AREL_SCENE, arelConfigFilePath);
                    mLaunchingTutorial = true;
                    startActivity(intent);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Invalid tutorial id, AREL configuration file not found: "+tutorialId);
                    showToast("Invalid tutorial id, AREL configuration file not found: "+tutorialId);
                }
            }*/
        }
    }
   }

