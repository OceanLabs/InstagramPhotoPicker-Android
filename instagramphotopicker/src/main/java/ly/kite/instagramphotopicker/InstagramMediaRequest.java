package ly.kite.instagramphotopicker;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by deon on 03/08/15.
 */
public class InstagramMediaRequest implements Parcelable
  {

  private static final String GENERIC_NETWORK_EXCEPTION_MESSAGE = "Failed to reach Instagram. Please check your internet connectivity and try again";
  private static final String MEDIA_URL_ENDPOINT = "https://api.instagram.com/v1/users/self/media/recent";

  private AsyncTask<Void, Void, MediaResponse> requestTask;
  private final String baseURL;

  public InstagramMediaRequest()
    {
    baseURL = MEDIA_URL_ENDPOINT;
    }

  private InstagramMediaRequest( String url )
    {
    baseURL = url;
    }

  public void getMedia( final String accessToken, final InstagramMediaRequestListener listener )
    {
    requestTask = new AsyncTask<Void, Void, MediaResponse>()
      {
      @Override
      protected MediaResponse doInBackground( Void... voids )
        {
        MediaResponse mediaResponse = new MediaResponse();

        String urlString = baseURL;
        if ( !urlString.contains( "access_token" ) )
          {
          urlString += "?access_token=" + accessToken;
          }

        if ( !urlString.contains( "&count=" ) )
          {
          urlString += "&count=33";
          }

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = request = new HttpGet( urlString );

        try
          {
          HttpResponse response = httpclient.execute( request );
          BufferedReader reader = new BufferedReader( new InputStreamReader( response.getEntity().getContent(), "UTF-8" ) );
          StringBuilder builder = new StringBuilder();
          for ( String line = null; ( line = reader.readLine() ) != null; )
            {
            builder.append( line ).append( "\n" );
            }

          JSONTokener t = new JSONTokener( builder.toString() );
          JSONObject json = new JSONObject( t );
          int httpStatusCode = response.getStatusLine().getStatusCode();
          mediaResponse.httpStatusCode = httpStatusCode;

          if ( httpStatusCode == 400 || httpStatusCode == 401 )
            {
            // access token has expired
            mediaResponse.error = new InstagramPhotoPickerException( InstagramPhotoPickerException.CODE_INVALID_ACCESS_TOKEN, null );
            }
          else if ( httpStatusCode != 200 )
            {
            mediaResponse.error = new InstagramPhotoPickerException( InstagramPhotoPickerException.CODE_GENERIC_NETWORK_EXCEPTION, GENERIC_NETWORK_EXCEPTION_MESSAGE );
            }
          else
            {
            mediaResponse.photos = parsePhotosFromResponseJSON( json );
            mediaResponse.nextPageRequest = parseNextPageRequestFromResponseJSON( json );
            }

          }
        catch ( Exception e )
          {
          if ( e instanceof UnknownHostException )
            {
            e = new InstagramPhotoPickerException( InstagramPhotoPickerException.CODE_GENERIC_NETWORK_EXCEPTION, GENERIC_NETWORK_EXCEPTION_MESSAGE );
            }
          mediaResponse.error = e;
          }

        return mediaResponse;
        }

      @Override
      protected void onPostExecute( MediaResponse mediaResponse )
        {
        if ( mediaResponse.error != null )
          {
          listener.onError( mediaResponse.error );
          }
        else
          {
          listener.onMedia( mediaResponse.photos, mediaResponse.nextPageRequest );
          }
        }
      };

    requestTask.execute();
    }

  private static List<InstagramPhoto> parsePhotosFromResponseJSON( JSONObject json ) throws JSONException
    {
    final ArrayList<InstagramPhoto> photos = new ArrayList<>();

    JSONArray data = json.getJSONArray( "data" );
    for ( int i = 0; i < data.length(); ++i )
      {
      try
        {
        JSONObject photoJSON = data.getJSONObject( i );

        JSONObject images = photoJSON.getJSONObject( "images" );

        JSONObject thumbnail     = images.getJSONObject( "thumbnail" );
        JSONObject lowResolution = images.getJSONObject( "low_resolution" );
        JSONObject standard      = images.getJSONObject( "standard_resolution" );

        String thumbnailURL     = adjustedURL( thumbnail.getString( "url" ) );
        String lowResolutionURL = adjustedURL( lowResolution.getString( "url" ) );
        String standardURL      = adjustedURL( standard.getString( "url" ) );

        // We use the low resolution image for the picking; the thumbnail image is too
        // low resolution for larger devices.
        InstagramPhoto photo = new InstagramPhoto( new URL( lowResolutionURL ), new URL( standardURL ) );

        photos.add( photo );
        }
      catch ( Exception ex )
        { /* ignore */ }
      }

    return photos;
    }

  static private String adjustedURL( String originalURL )
    {
    if ( originalURL.startsWith( "http://" ) ) return ( originalURL.replace( "http://", "https://" ) );

    return ( originalURL );
    }

  private static InstagramMediaRequest parseNextPageRequestFromResponseJSON( JSONObject json ) throws JSONException
    {
    JSONObject pagination = json.getJSONObject( "pagination" );
    String nextPageURL = pagination.optString( "next_url", null );
    return nextPageURL != null ? new InstagramMediaRequest( nextPageURL ) : null;
    }

  public void cancel()
    {
    if ( requestTask != null )
      {
      requestTask.cancel( true );
      requestTask = null;
      }
    }

  public static interface InstagramMediaRequestListener
    {
    void onMedia( List<InstagramPhoto> media, InstagramMediaRequest nextPageRequest );

    void onError( Exception error );
    }

  private static class MediaResponse
    {
    private Exception error;
    private int httpStatusCode;
    private List<InstagramPhoto> photos;
    private InstagramMediaRequest nextPageRequest;
    }

  public InstagramMediaRequest( Parcel in )
    {
    baseURL = in.readString();
    }

  @Override
  public int describeContents()
    {
    return 0;
    }

  @Override
  public void writeToParcel( Parcel dest, int flags )
    {
    dest.writeString( baseURL );
    }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
    public InstagramMediaRequest createFromParcel( Parcel in )
      {
      return new InstagramMediaRequest( in );
      }

    public InstagramMediaRequest[] newArray( int size )
      {
      return new InstagramMediaRequest[ size ];
      }
    };

  }
