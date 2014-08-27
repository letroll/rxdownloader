package fr.letroll.rxdownloader.manager;

import android.app.Fragment;
import android.view.View;

import java.util.ArrayList;

import fr.letroll.rxdownloader.viewcustom.DownloadView;

/**
 * Created by letroll on 03/08/2014.
 */
public class DownloadManager {

    /**
     * VARIABLES *
     */
    public final static String TAG = "DownloadManager";

    /**
     * OBJECTS *
     */
    private ArrayList<DownloadView> downloadViewArrayList = new ArrayList<DownloadView>();
    private Fragment container;
    private static DownloadManager INSTANCE;

    private DownloadManager() {
    }

    public synchronized static DownloadManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        }
        return INSTANCE;
    }


    public void unsubscribe() {
        for (DownloadView downloadView :downloadViewArrayList) {
          downloadView.unsubscribe();
        }
    }

    public void setDownloadViewContainer(Fragment container) {
        this.container=container;
    }

    public void add(DownloadView dlv) throws Exception {
        if(container==null) throw new Exception("setDownloadViewContainer must be called before add method");
        if(dlv.getId()== View.NO_ID)throw new Exception("DownloadView must have it's Id set");

        int pos = posInList(dlv);

        if(pos==-1){
            downloadViewArrayList.add(dlv);
        }else{
            dlv.setDownloadPresenter(downloadViewArrayList.get(pos).getDownloadPresenter());
        }
        dlv.onCreateView(container);
    }

    private int posInList(DownloadView dlv) throws Exception {
        if(dlv.getId()== View.NO_ID)throw new Exception("DownloadView must have it's Id set (posInList)");
        int result=-1;
        int len = downloadViewArrayList.size();
        for (int i = 0; i<len; i++) {
            if(dlv.getId()==downloadViewArrayList.get(i).getId()){
                result=i;
                break;
            }
        }
        return result;
    }

    public void clearAllDl() {
        for (DownloadView downloadView :downloadViewArrayList) {
            downloadView.clearDl();
        }
    }
}
