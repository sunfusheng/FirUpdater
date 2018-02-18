package com.sunfusheng;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirDownloader {

    private static final String SP_NAME = "download_file";
    private static final String CURR_LENGTH = "curr_length";
    private static final int DEFAULT_THREAD_COUNT = 3;//默认下载线程数
    //以下为线程状态
    private static final String DOWNLOAD_INIT = "1";
    private static final String DOWNLOAD_ING = "2";
    private static final String DOWNLOAD_PAUSE = "3";

    private Context context;

    private String loadUrl;//网络获取的url
    private String filePath;//下载到本地的path
    private int threadCount = DEFAULT_THREAD_COUNT;//下载线程数

    private int fileLength;//文件总大小
    private volatile int currLength;//当前总共下载的大小
    private volatile int threadCountRunning;//正在运行的线程数
    private String stateDownload = DOWNLOAD_INIT;//当前线程状态
    private FirNotification firNotification;

    private DownloadThread[] threadArr;

    private OnDownLoadListener onDownLoadListener;

    public void setOnDownLoadListener(OnDownLoadListener onDownLoadListener) {
        this.onDownLoadListener = onDownLoadListener;
    }

    interface OnDownLoadListener {
        void onProgress(int totalLength, int currLength, int progress);

        void onSuccess();

        void onError();
    }

    public FirDownloader(Context context, String loadUrl, String filePath) {
        this(context, loadUrl, filePath, null);
    }

    public FirDownloader(Context context, String loadUrl, String filePath, OnDownLoadListener onDownLoadListener) {
        this.context = context;
        this.loadUrl = loadUrl;
        this.filePath = filePath;
        this.threadCountRunning = 0;
        this.onDownLoadListener = onDownLoadListener;
    }

    protected void downLoad() {
        new Thread(() -> {
            try {
                if (threadArr == null) {
                    threadArr = new DownloadThread[threadCount];
                }

                URL url = new URL(loadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    fileLength = conn.getContentLength();
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
                    raf.setLength(fileLength);
                    raf.close();
                    int blockLength = fileLength / threadCount;

                    SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                    currLength = sp.getInt(CURR_LENGTH, 0);
                    for (int i = 0; i < threadCount; i++) {
                        int startPosition = sp.getInt(SP_NAME + (i + 1), i * blockLength);
                        int endPosition = (i + 1) * blockLength - 1;
                        if ((i + 1) == threadCount)
                            endPosition = endPosition * 2;

                        threadArr[i] = new DownloadThread(i + 1, startPosition, endPosition);
                        threadArr[i].start();
                    }
                } else {
                    handler.sendEmptyMessage(ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(ERROR);
            }
        }).start();
    }

    protected void start() {
        if (threadArr != null)
            synchronized (DOWNLOAD_PAUSE) {
                stateDownload = DOWNLOAD_ING;
                DOWNLOAD_PAUSE.notifyAll();
            }
    }

    protected void pause() {
        if (threadArr != null) {
            stateDownload = DOWNLOAD_PAUSE;
        }
    }

    protected void cancel() {
        if (threadArr != null) {
            if (stateDownload.equals(DOWNLOAD_PAUSE)) {
                start();
            }

            for (DownloadThread thread : threadArr) {
                thread.cancel();
            }
        }
    }

    protected void destroy() {
        if (threadArr != null) {
            threadArr = null;
        }
    }

    private class DownloadThread extends Thread {

        private boolean isGoOn = true;//是否继续下载
        private int threadId;
        private int currPosition;//当前线程的下载进度
        private int startPosition;//开始下载点
        private int endPosition;//结束下载点

        private DownloadThread(int threadId, int startPosition, int endPosition) {
            this.threadId = threadId;
            this.currPosition = startPosition;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            threadCountRunning++;
        }

        @Override
        public void run() {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            try {
                URL url = new URL(loadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
                conn.setConnectTimeout(5000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    InputStream is = conn.getInputStream();
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
                    raf.seek(startPosition);
                    int len;
                    int lastProgress = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = is.read(buffer)) != -1) {
                        if (!isGoOn) {
                            break;
                        }
                        if (onDownLoadListener != null) {
                            currLength += len;
                            int progress = (int) ((float) currLength / (float) fileLength * 100);
                            if (progress != lastProgress) {
                                lastProgress = progress;
                                handler.sendMessage(handler.obtainMessage(progress, fileLength, currLength));
                            }
                        }

                        raf.write(buffer, 0, len);
                        currPosition += len;
                        synchronized (DOWNLOAD_PAUSE) {
                            if (stateDownload.equals(DOWNLOAD_PAUSE)) {
                                DOWNLOAD_PAUSE.wait();
                            }
                        }
                    }
                    is.close();
                    raf.close();
                    threadCountRunning--;
                    if (!isGoOn) {
                        if (currPosition < endPosition) {
                            sp.edit().putInt(SP_NAME + threadId, currPosition).apply();
                            sp.edit().putInt(CURR_LENGTH, currLength).apply();
                        }
                        return;
                    }
                    if (threadCountRunning == 0) {
                        sp.edit().clear().apply();
                        handler.sendMessage(handler.obtainMessage(100, fileLength, currLength));
                        handler.sendEmptyMessage(SUCCESS);
                        threadArr = null;
                    }
                } else {
                    sp.edit().clear().apply();
                    handler.sendEmptyMessage(ERROR);
                }
            } catch (Exception e) {
                sp.edit().clear().apply();
                e.printStackTrace();
                handler.sendEmptyMessage(ERROR);
            }
        }

        public void cancel() {
            isGoOn = false;
        }
    }

    private final int SUCCESS = 1000;
    private final int ERROR = 1001;
    private int lastProgress = 0;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (onDownLoadListener != null) {
                switch (msg.what) {
                    case SUCCESS:
                        onDownLoadListener.onSuccess();
                        break;
                    case ERROR:
                        onDownLoadListener.onError();
                        break;
                    default:
                        if (msg.what != lastProgress) {
                            lastProgress = msg.what;
                            onDownLoadListener.onProgress(msg.arg1, msg.arg2, msg.what);
                        }
                        break;
                }
            }
        }
    };
}
