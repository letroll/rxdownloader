package fr.letroll.rxdownloader.contract;

import fr.letroll.rxdownloader.events.DownloadProgressEvent;

/**
 * Created by jquievreux on 26/08/2014.
 */
public interface IDownloadView {

    void switchToPauseView();

    void switchToResumeView();

    void switchToDownloadView();

    void showProgress(DownloadProgressEvent event);

    void clearDl();

    void setUrl(String url);

    String getUrl();

    void setFilename(String filename);

    String getFilename();

    int getId();

}
