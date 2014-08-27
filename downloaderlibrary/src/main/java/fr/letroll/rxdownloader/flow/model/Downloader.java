package fr.letroll.rxdownloader.flow.model;

import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.letroll.rxdownloader.events.DownloadProgressEvent;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class Downloader {
    public static final String TAG = "Downloader";

    private String url;
    private String filename = "";
    private long totalSize;
    private long loadedSize;
    private BehaviorSubject<DownloadProgressEvent> progressSubject;
    private OkHttpClient client = new OkHttpClient();
    private Request request;
    private volatile boolean running = true;
    private volatile boolean killed = false;
    private boolean logHeader = false;
    private File outFile;

    private Downloader(String url, String filename) {
        this.url = url;
        this.filename = filename;
        // Seed the event stream with the first event.
        progressSubject = BehaviorSubject.create(new DownloadProgressEvent(0, 0));
    }

    public static Downloader newInstance(String url, String filename) {
        return new Downloader(url, filename);
    }

    @SuppressWarnings("unused")
    public Downloader logHeader() {
        logHeader = true;
        return this;
    }

    public void loadInBackground() {
        request = new Request.Builder().url(url).build();

        final Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                progressSubject.onError(e);
            }

            @Override
            public void onResponse(Response response) {
                if (!response.isSuccessful()) {
                    progressSubject.onError(new IOException("Unexpected code " + response));
                }

                if (logHeader) {
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.v(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }
                }

                totalSize = response.body().contentLength();
                InputStream inputStream = response.body().byteStream();
                loadedSize = 0;

                byte[] buffer = new byte[1024];
                int bufferLength; //used to store a temporary size of the buffer

                try {
                    File sdCardRoot = Environment.getExternalStorageDirectory();
                    outFile = new File(sdCardRoot, filename);
                    FileOutputStream fileOutput = new FileOutputStream(outFile);

                    try {
                        while (!killed && (bufferLength = inputStream.read(buffer)) > 0) {
                            while (!running && !killed) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    progressSubject.onError(e);
                                }
                            }
                            if (!killed) {
                                fileOutput.write(buffer, 0, bufferLength);
                                loadedSize += bufferLength;
                                reportProgress();
                            }
                        }

                        fileOutput.close();
                    } catch (IOException e) {
                        progressSubject.onError(e);
                    }
                    if (killed && outFile.exists()) {
                        if (!outFile.delete())
                            progressSubject.onError(new IOException("file delete failed"));
                        call.cancel();
                    }
                    progressSubject.onCompleted();
                } catch (FileNotFoundException e) {
                    progressSubject.onError(e);
                }
            }
        });
    }

    private void reportProgress() {
        progressSubject.onNext(new DownloadProgressEvent(loadedSize, totalSize));
    }

    public void setInPause(boolean pause) {
        running = !pause;
    }

    public void kill() {
        killed = true;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isKilled() {
        return killed;
    }

    public Subject<DownloadProgressEvent, DownloadProgressEvent> getProgressObservable() {
        return progressSubject;
    }

    public boolean clearDl() {
        if(outFile == null)return true;
        else {
            if(!outFile.exists())return true;
            else
            return outFile.delete();
        }
    }
}