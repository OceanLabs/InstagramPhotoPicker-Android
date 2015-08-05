package ly.kite.instagramphotopicker;

/**
 * Created by deon on 03/08/15.
 */
public class InstagramPhotoPickerException extends Exception {

    public static final int CODE_GENERIC_NETWORK_EXCEPTION = 0;
    public static final int CODE_INVALID_ACCESS_TOKEN = 1;

    private final int code;

    public InstagramPhotoPickerException(int code, String detailsMessage) {
        super(detailsMessage);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
