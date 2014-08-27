package fr.letroll.rxdownloader.exception;

/**
 * Created by letroll on 19/07/2014.
 */
public class DownloadDataInvalidException extends Exception {

    /**
     * VARIABLES *
     */
    public final static String TAG = "DownloadDataInvalidException";
    private final String message;

    /**
     * OBJECTS *
     */

    public DownloadDataInvalidException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
