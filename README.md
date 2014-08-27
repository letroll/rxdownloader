Rxdownloader
============

a download management library for Android with [RxJava](https://github.com/ReactiveX/RxJava) as core

Features
========
* support when possible download resume
* device orientation change

Install
=======

**Android Studio:**
* add Rxdownloader like a project library 
* make of Rxdownloader a dependency of your project

If you have problems with the support-v4 (multiple dex files) then exclude the support-v4 in the dependency section:
```xml
dependencies {
    compile 'com.android.support:support-v4:19.0.0'
    compile(project(':downloaderlibrary')) {
        exclude module: 'support-v4'
    }
}
```

Code
====

a fragment:
```java
public class DownloadFragment extends Fragment {

    private DownloadManager dlm;

    public DownloadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_download, container, false);

        DownloadView dl_0 = (DownloadView) rootView.findViewById(R.id.dl_0);

        dlm = DownloadManager.getInstance();
        dlm.setDownloadViewContainer(this);
        try {
            dlm.add(dl_0);
            dl_0.setFilename("adt-bundle-mac-x86_64-20131030.zip");
            dl_0.setUrl("http://dl.google.com/android/adt/adt-bundle-mac-x86_64-20131030.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        dlm.unsubscribe();
        dlm.clearAllDl();
        super.onDestroy();
    }
}
```

it's layout:
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    >

    <fr.letroll.rxdownloader.viewcustom.DownloadView
        android:id="@+id/dl_0"
        android:layout_marginTop="20dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
    />

</RelativeLayout>
```

Authors and contributors
========================
* Julien Quiévreux <<julien.quievreux@gmail.com>>
* Dan Osipov <<http://danosipov.com/>> (Data Engineer at Shazam)
his project [rxdownloader](https://github.com/danosipov/rxdownloader) is the source of this project


License
=======

This BitTorrent library is distributed under the terms of the Apache Software License version 2.0. See COPYING file for more details.