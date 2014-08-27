package fr.letroll.rxdownloader.viewcustom;

import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.letroll.rxdownloader.R;
import fr.letroll.rxdownloader.contract.DownloadViewErrorListener;
import fr.letroll.rxdownloader.contract.IDownloadView;
import fr.letroll.rxdownloader.events.ClickEvent;
import fr.letroll.rxdownloader.events.DownloadProgressEvent;
import fr.letroll.rxdownloader.exception.DownloadDataInvalidException;
import fr.letroll.rxdownloader.flow.presenter.DownloadPresenter;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.Subscriptions;

import static fr.letroll.rxdownloader.events.ClickEvent.DownloadEventType.pause;
import static fr.letroll.rxdownloader.events.ClickEvent.DownloadEventType.reset;
import static fr.letroll.rxdownloader.events.ClickEvent.DownloadEventType.resume;
import static fr.letroll.rxdownloader.events.ClickEvent.DownloadEventType.start;


/**
 * Created by letroll on 19/07/2014.
 */
public class DownloadView extends RelativeLayout implements Observer<ClickEvent>,IDownloadView {

    /**
     * VARIABLES *
     */
    public final static String TAG = "DownloadView";

    /**
     * VIEWS *
     */
    ImageView downloadButton;
    ImageView resetButton;
    ProgressBar downloadProgressBar;
    TextView downloadProgressTextView;

    /**
     * OBJECTS *
     */
    private DownloadViewErrorListener downloadViewErrorListener;
    private ClickEvent.DownloadEventType downloadEventType = start;
    private Subscription clickSubscription;
    private Observer<? super ClickEvent> observer;
    private DownloadPresenter downloadPresenter;

    public DownloadView(Context context) {
        super(context);
        init(context, null);
    }

    public DownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DownloadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        View view=inflate(context, R.layout.download_view, this);

        downloadButton = (ImageView) view.findViewById(R.id.downloadButton);
        resetButton = (ImageView) view.findViewById(R.id.resetButton);
        downloadProgressBar = (ProgressBar) view.findViewById(R.id.downloadProgressBar);
        downloadProgressTextView = (TextView) view.findViewById(R.id.downloadProgressTextView);

        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (observer != null)
                    observer.onNext(new ClickEvent(reset));
            }
        });

        downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (observer != null)
                    observer.onNext(new ClickEvent(getState()));
            }
        });

        initFromAttruteSet(context, attrs);

        if (!isInEditMode()) {

        }
    }

    private void initFromAttruteSet(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
                    R.styleable.DownloadView);

            final int N = styledAttrs.getIndexCount();
            for (int i = 0; i < N; ++i) {
                int attr = styledAttrs.getIndex(i);
                switch (attr) {
                    case R.styleable.DownloadView_downloadView_typeface:
                        String fontName = styledAttrs.getString(attr);
                        break;
                    default:
                        break;
                }
            }

            styledAttrs.recycle();
        }
    }

    public ClickEvent.DownloadEventType getState() {
        return downloadEventType;
    }

    public void onCreateView(Fragment container) {
        if(downloadPresenter==null){
            downloadPresenter = new DownloadPresenter(this);
        } else {
            downloadPresenter.setiDownloadView(this);
        }

        subscribeToClickStream(container, createClickObservable());
    }

    private void subscribeToClickStream(Fragment fragment, Observable<ClickEvent> clickObservable) {
        clickSubscription = AndroidObservable.fromFragment(fragment, clickObservable)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    private Observable<ClickEvent> createClickObservable() {
        return Observable.create(
                new Observable.OnSubscribeFunc<ClickEvent>() {
                    @Override
                    public Subscription onSubscribe(final Observer<? super ClickEvent> obs) {
                        observer = obs;
                        downloadPresenter.restoreState();
                        return Subscriptions.empty();
                    }
                }
        );
    }

    public void setDownloadViewErrorListener(DownloadViewErrorListener downloadViewErrorListener) {
        this.downloadViewErrorListener = downloadViewErrorListener;
    }

    public void unsubscribe() {

        clickSubscription.unsubscribe();
    }

    /**
     * Rxjava methods *
     */

    @Override
    public void onCompleted() {
        switchToCompletedView();
    }

    @Override
    public void onError(Throwable throwable) {
        Log.e(TAG, "Got an error from the Observable", throwable);
        downloadViewErrorListener.onDownloadViewError(new Exception(throwable.getMessage()), this);
    }

    @Override
    public void onNext(ClickEvent clickEvent) {
        switch (clickEvent.getEventType()) {
            case start:
                try {
                    downloadPresenter.startDownload();
                } catch (DownloadDataInvalidException e) {
                    e.printStackTrace();
                    if (downloadViewErrorListener != null)
                        downloadViewErrorListener.onDownloadViewError(e, this);
                }
                break;
            case pause:
                downloadPresenter.pauseDownload();
                break;
            case resume:
                downloadPresenter.resumeDownload();
                break;
            case reset:
                downloadPresenter.resetDownload();
                break;
            default:
                break;
        }
    }

    @Override
    public void switchToPauseView() {
        downloadButton.setImageResource(android.R.drawable.ic_media_pause);
        downloadEventType = pause;
    }

    @Override
    public void switchToResumeView() {
        downloadButton.setImageResource(android.R.drawable.ic_media_play);
        downloadEventType = resume;
    }

    public void switchToCompletedView() {
        downloadProgressTextView.setText(R.string.download_complete);
        resetButton.setImageResource(android.R.drawable.ic_notification_clear_all);
        downloadButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void switchToDownloadView() {
        downloadButton.setImageResource(android.R.drawable.ic_media_play);
        downloadEventType = start;
        downloadButton.setVisibility(View.VISIBLE);
        downloadProgressTextView.setText(R.string.zero_bytes);
        downloadProgressBar.setProgress(0);
    }

    @Override
    public void showProgress(DownloadProgressEvent event) {
        downloadProgressBar.setProgress((int) event.getProgress());
        downloadProgressTextView.setText(String.format("%s / %s", event.getLoadedBytes(), event.getTotalBytes()));
    }

    @Override
    public void clearDl() {
        downloadPresenter.clearDl();
    }

    @Override
    public void setUrl(String url) {
        if(downloadPresenter==null){
            Log.e(TAG,"setUrl call must be after DownloadView.add method");
            return;
        }
        downloadPresenter.setUrl(url);
    }

    @Override
    public String getUrl() {
        if(downloadPresenter==null){
            Log.e(TAG,"getUrl call must be after DownloadView.add method");
            return "";
        }
        return downloadPresenter.getUrl();
    }

    @Override
    public void setFilename(String filename) {
        if(downloadPresenter==null){
            Log.e(TAG,"setFilename call must be after DownloadView.add method");
            return;
        }
        downloadPresenter.setFilename(filename);
    }

    @Override
    public String getFilename() {
        if(downloadPresenter==null){
            Log.e(TAG,"getFilename call must be after DownloadView.add method");
            return "";
        }
        return downloadPresenter.getFilename();
    }

    public DownloadPresenter getDownloadPresenter() {
        return downloadPresenter;
    }

    public void setDownloadPresenter(DownloadPresenter downloadPresenter) {
        this.downloadPresenter = downloadPresenter;
    }

}
