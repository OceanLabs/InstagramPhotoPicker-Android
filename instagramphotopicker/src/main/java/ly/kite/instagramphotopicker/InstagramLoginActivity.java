package ly.kite.instagramphotopicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class InstagramLoginActivity extends Activity {

  static private final String   LOG_TAG           = "InstagramLoginActivity";
  static private final boolean  DEBUGGING_ENABLED = false;

    private static final String GENERIC_LOGIN_ERROR_MESSAGE = "You need to authorise the application to allow photo picking. Please try again.";

    private static final String EXTRA_CLIENT_ID = "ly.kite.instagramimagepicker.EXTRA_CLIENT_ID";
    private static final String EXTRA_REDIRECT_URI = "ly.kite.instagramimagepicker.EXTRA_REDIRECT_URI";

    private static final int REQUEST_CODE_GALLERY = 99;

    public static void startLoginForResult(Activity activity, String clientId, String redirectUri, int requestCode) {
        Intent i = new Intent(activity, InstagramLoginActivity.class);
        i.putExtra(EXTRA_CLIENT_ID, clientId);
        i.putExtra(EXTRA_REDIRECT_URI, redirectUri);
        activity.startActivityForResult(i, requestCode);
    }

    public static void startLoginForResult(Fragment fragment, String clientId, String redirectUri, int requestCode) {
        Intent i = new Intent(fragment.getActivity(), InstagramLoginActivity.class);
        i.putExtra(EXTRA_CLIENT_ID, clientId);
        i.putExtra(EXTRA_REDIRECT_URI, redirectUri);
        fragment.startActivityForResult(i, requestCode);
    }

    private WebView webview;
    private String clientId;
    private String redirectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_login);

        this.clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        this.redirectUri = getIntent().getStringExtra(EXTRA_REDIRECT_URI);

        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(webViewClient);
        loadLoginPage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadLoginPage() {
        String instagramAuthURL = "https://api.instagram.com/oauth/authorize/?client_id=" + this.clientId + "&redirect_uri=" + this.redirectUri + "&response_type=token";
        webview.loadUrl(instagramAuthURL);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_CLIENT_ID, clientId);
        outState.putString(EXTRA_REDIRECT_URI, redirectUri);
        webview.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.clientId = savedInstanceState.getString(EXTRA_CLIENT_ID);
        this.redirectUri = savedInstanceState.getString(EXTRA_REDIRECT_URI);
        webview.restoreState(savedInstanceState);
    }

    private void gotAccessToken(final String instagramAccessToken) {
        InstagramPhotoPicker.saveInstagramPreferences(this, instagramAccessToken, clientId, redirectUri);
        InstagramGalleryActivity.startForResult(this, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // bubble result up to calling/starting activity
        setResult(resultCode, data);
        finish();
    }

    private final String getLoginErrorMessage(Uri uri) {
        String errorReason = uri.getQueryParameter("error_reason");
        String errorMessage = "An unknown error occurred. Please try again.";
        if (errorReason != null) {
            if (errorReason.equalsIgnoreCase("user_denied")) {
                errorMessage = GENERIC_LOGIN_ERROR_MESSAGE;
            } else {
                errorMessage = uri.getQueryParameter("error_description");
                if (errorMessage == null) {
                    errorMessage = GENERIC_LOGIN_ERROR_MESSAGE;
                } else {
                    try {
                        errorMessage = URLDecoder.decode(errorMessage, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        errorMessage = GENERIC_LOGIN_ERROR_MESSAGE;
                    }
                }
            }
        }

        return errorMessage;
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private final WebViewClient webViewClient =  new WebViewClient() {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "shouldOverrideUrlLoading( view, url = " + url.toString() + " )" );

            if (url != null && url.startsWith(redirectUri)) {
                Uri uri = Uri.parse(url);
                String error = uri.getQueryParameter("error");
                if (error != null) {
                    String errorMessage = getLoginErrorMessage(uri);
                    webview.stopLoading();
                    loadLoginPage();
                    showErrorDialog(errorMessage);
                } else {
                    String fragment = uri.getFragment();
                    String accessToken = fragment.substring("access_token=".length());
                    gotAccessToken(accessToken);
                }

                return true;
            }

            return false;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "onPageStarted( view, url = " + url.toString() + ", favicon )" );

        }

        public void onPageFinished(WebView view, String url) {

        if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "onPageFinished( view, url = " + url.toString() + " )" );

        }

        public void onLoadResource(WebView view, String url) {

        if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "onLoadResources( view, url = " + url.toString() + " )" );

        }
    };

}
