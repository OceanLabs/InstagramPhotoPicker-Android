package ly.kite.instagramphotopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;


/**
 * Created by deon on 28/07/15.
 */
public class InstagramPhotoPicker {

    public static final String EXTRA_SELECTED_PHOTOS = "ly.kite.instagramphotopicker.EXTRA_SELECTED_PHOTOS";

    static final String PREFERENCE_FILE = "ly.kite.instagramphotopicker.PREFERENCE_FILE";
    static final String PREFERENCE_ACCESS_TOKEN = "ly.kite.instagramphotopicker.PREFERENCE_ACCESS_TOKEN";
    static final String PREFERENCE_CLIENT_ID = "ly.kite.instagramphotopicker.PREFERENCE_CLIENT_ID";
    static final String PREFERENCE_REDIRECT_URI = "ly.kite.instagramphotopicker.PREFERENCE_REDIRECT_URI";

    static String cachedAccessToken = null;
    static String cachedClientId = null;
    static String cachedRedirectUri = null;

    public static void startPhotoPickerForResult(Activity activity, String clientId, String redirectUri, int requestCode) {
        String accessToken = getAccessToken(activity);
        if (accessToken != null) {
            if (clientId.equals(cachedClientId)) {
                InstagramGalleryActivity.startForResult(activity, requestCode);
                return;
            } else {
                logout(activity); // clear cache & preferences if any.
            }
        }

        InstagramLoginActivity.startLoginForResult(activity, clientId, redirectUri, requestCode);
    }

    public static InstagramPhoto[] getResultPhotos(Intent data) {
        Parcelable[] photos = data.getParcelableArrayExtra(InstagramPhotoPicker.EXTRA_SELECTED_PHOTOS);
        InstagramPhoto[] instagramPhotos = new InstagramPhoto[photos.length];
        System.arraycopy(photos, 0, instagramPhotos, 0, photos.length);
        return instagramPhotos;
    }

    static String getAccessToken(Context context) {
        if (cachedAccessToken == null) {
            loadInstagramPreferences(context);
        }
        return cachedAccessToken;
    }

    static String getClientId(Context context) {
        if (cachedClientId == null) {
            loadInstagramPreferences(context);
        }
        return cachedClientId;
    }

    static String getRedirectUri(Context context) {
        if (cachedRedirectUri == null) {
            loadInstagramPreferences(context);
        }
        return cachedRedirectUri;
    }

    private static void loadInstagramPreferences(Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences preferences = applicationContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        cachedAccessToken = preferences.getString(PREFERENCE_ACCESS_TOKEN, null);
        cachedClientId = preferences.getString(PREFERENCE_CLIENT_ID, null);
        cachedRedirectUri = preferences.getString(PREFERENCE_REDIRECT_URI, null);
    }

    static void saveInstagramPreferences(Context context, String accessToken, String clientId, String redirectURI) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences preferences = applicationContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_ACCESS_TOKEN, accessToken);
        editor.putString(PREFERENCE_CLIENT_ID, clientId);
        editor.putString(PREFERENCE_REDIRECT_URI, redirectURI);
        editor.commit();
        cachedAccessToken = accessToken;
        cachedClientId = clientId;
        cachedRedirectUri = redirectURI;
    }

    static void logout(Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences preferences = applicationContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(PREFERENCE_ACCESS_TOKEN);
        editor.remove(PREFERENCE_CLIENT_ID);
        editor.remove(PREFERENCE_REDIRECT_URI);
        editor.commit();
        cachedAccessToken = null;
        cachedClientId = null;
        cachedRedirectUri = null;

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        new WebView(context).clearCache(true);
    }

}
