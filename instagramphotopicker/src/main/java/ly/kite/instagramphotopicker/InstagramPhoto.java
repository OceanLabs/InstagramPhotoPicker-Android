package ly.kite.instagramphotopicker;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;

/**
 * Created by deon on 03/08/15.
 */
public class InstagramPhoto implements Parcelable {

    private final URL thumbURL;
    private final URL fullURL;

    public InstagramPhoto(URL thumbURL, URL fullURL) {
        this.thumbURL = thumbURL;
        this.fullURL = fullURL;
    }

    public URL getThumbnailURL() {
        return thumbURL;
    }

    public URL getFullURL() {
        return fullURL;
    }

    public InstagramPhoto(Parcel in){
        thumbURL = (URL) in.readValue(URL.class.getClassLoader());
        fullURL = (URL) in.readValue(URL.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(thumbURL);
        dest.writeValue(fullURL);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public InstagramPhoto createFromParcel(Parcel in) {
            return new InstagramPhoto(in);
        }

        public InstagramPhoto[] newArray(int size) {
            return new InstagramPhoto[size];
        }
    };

    @Override
    public int hashCode() {
        int v = 17;
        v = v * 31 + thumbURL.hashCode();
        v = v * 31 + fullURL.hashCode();
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InstagramPhoto)) {
            return false;
        }

        InstagramPhoto photo = (InstagramPhoto) o;
        return photo.thumbURL.equals(thumbURL) && photo.fullURL.equals(fullURL);
    }
}
