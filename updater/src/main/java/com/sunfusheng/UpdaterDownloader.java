package com.sunfusheng;

import android.Manifest;

import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;
import com.sunfusheng.download.DownloadProgressObserver;
import com.sunfusheng.download.IDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
class UpdaterDownloader {
    private static final Permissions permissions = Permissions.build(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    private DownloadProgressObserver mDownloadProgressObserver;

    void download(String url, String filePathName, IDownloadListener downloadListener) {
        SoulPermission.getInstance().checkAndRequestPermissions(permissions, new CheckRequestPermissionsListener() {
            @Override
            public void onAllPermissionOk(Permission[] allPermissions) {
                downloadInternal(url, filePathName, downloadListener);
            }

            @Override
            public void onPermissionDenied(Permission[] refusedPermissions) {

            }
        });
    }

    private void downloadInternal(String url, String filePathName, IDownloadListener downloadListener) {
        mDownloadProgressObserver = new DownloadProgressObserver(filePathName, downloadListener);
        UpdaterApi.getDownloadService(downloadListener).download(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    if (downloadListener != null) {
                        AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                            downloadListener.onError(throwable);
                        });
                    }
                })
                .map(ResponseBody::byteStream)
                .doOnNext(inputStream -> writeFile(inputStream, filePathName))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mDownloadProgressObserver);
    }

    void cancel() {
        if (mDownloadProgressObserver != null) {
            mDownloadProgressObserver.dispose();
        }
    }

    private void writeFile(InputStream inputStream, String filePathName) throws Exception {
        File file = new File(filePathName);
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[2048];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        inputStream.close();
        fos.close();
    }
}
