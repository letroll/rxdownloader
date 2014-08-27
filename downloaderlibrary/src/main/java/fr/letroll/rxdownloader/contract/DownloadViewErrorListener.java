package fr.letroll.rxdownloader.contract;


import fr.letroll.rxdownloader.viewcustom.DownloadView;

/**
 * Created by letroll on 19/07/2014.
 */
public interface DownloadViewErrorListener {
    void onDownloadViewError(Exception e, DownloadView view);
}
