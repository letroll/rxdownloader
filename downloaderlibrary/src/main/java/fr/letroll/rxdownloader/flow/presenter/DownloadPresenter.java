package fr.letroll.rxdownloader.flow.presenter;

import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import fr.letroll.rxdownloader.contract.IDownloadView;
import fr.letroll.rxdownloader.events.DownloadProgressEvent;
import fr.letroll.rxdownloader.exception.DownloadDataInvalidException;
import fr.letroll.rxdownloader.flow.model.Downloader;
import rx.android.concurrency.AndroidSchedulers;
import rx.util.functions.Action1;

/**
 * Created by letroll on 06/07/2014.
 */
public class DownloadPresenter {

    /**
     * VARIABLES *
     */
    private static final String TAG = "DownloadPresenter";

    private String filename;
    private String url;

    /**
     * OBJECTS *
     */
    private Downloader downloadThread;

    private IDownloadView iDownloadView;

    public DownloadPresenter(IDownloadView iDownloadView) {
        this.iDownloadView = iDownloadView;
    }

    public void restoreState() {
        /**
         * Restore state of the views based on the fragment instance state
         * If not done, the center button stays in "download" state that
         * the view is initialized with
         */
        if (downloadThread != null) {
            if (!downloadThread.isKilled()) {
                if (downloadThread.isRunning()) {
//                    DownloadFragment.report("downloadThread isRunning");
                    iDownloadView.switchToPauseView();
                } else {
//                    DownloadFragment.report("downloadThread isRunning not");
                    iDownloadView.switchToResumeView();
                }
            }//else DownloadFragment.report("downloadThread isKilled");
        }//else DownloadFragment.report("downloadThread null");
    }

    public void startDownload() throws DownloadDataInvalidException {
        if (TextUtils.isEmpty(filename) || TextUtils.isEmpty(url))
            throw new DownloadDataInvalidException("filename and/or url empty");

        downloadThread = Downloader.newInstance(url, filename);
        downloadThread.loadInBackground();

        // Subscribe to the progress observable
        // Sample the stream every 30 milliseconds (ignore events in between)
        // Upon receiving an event, update the views
        downloadThread.getProgressObservable()
                .sample(30, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DownloadProgressEvent>() {
                    @Override
                    public void call(DownloadProgressEvent event) {
                        if (downloadThread.isKilled()) {
                            iDownloadView.switchToDownloadView();
                        } else {
                            iDownloadView.showProgress(event);
                        }
                    }
                });
        iDownloadView.switchToPauseView();
    }

    public void pauseDownload() {
        downloadThread.setInPause(true);
        iDownloadView.switchToResumeView();
    }

    public void resumeDownload() {
        downloadThread.setInPause(false);
        iDownloadView.switchToPauseView();
    }

    public void resetDownload() {
        if (downloadThread != null) {
            downloadThread.kill();
        }
        iDownloadView.switchToDownloadView();
    }

    public boolean clearDl() {
        resetDownload();
        if (downloadThread == null) {
            return true;
        }else return downloadThread.clearDl();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    public void setiDownloadView(IDownloadView iDownloadView) {
        this.iDownloadView = iDownloadView;
    }


}