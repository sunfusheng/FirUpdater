package com.sunfusheng;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

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
    private static final int DEFAULT_THREAD_COUNT = 3;

    private static final String STATE_INIT = "init";
    private static final String STATE_DOWNLOADING = "downloading";
    private static final String STATE_PAUSE = "pause";
    private static final int STATE_SUCCESS = 1001;
    private static final int STATE_ERROR = 1002;

    private Context context;
    private String apkUrl;
    private String apkPath;
    private int fileLength;
    private volatile int currLength;

    private DownloadThread[] threadArr;
    private int threadCount = DEFAULT_THREAD_COUNT;
    private volatile int threadCountRunning = 0;
    private String downloadState = STATE_INIT;

    private int lastProgress = 0;
    private OnDownLoadListener onDownLoadListener;

    public FirDownloader(Context context, String apkUrl, String apkPath) {
        this(context, apkUrl, apkPath, null);
    }

    public FirDownloader(Context context, String apkUrl, String apkPath, OnDownLoadListener onDownLoadListener) {
        this.context = context;
        this.apkUrl = apkUrl;
        this.apkPath = apkPath;
        this.onDownLoadListener = onDownLoadListener;
    }

    public void downLoad() {
        new Thread(() -> {
            try {
                if (threadArr == null) {
                    threadArr = new DownloadThread[threadCount];
                }

                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    fileLength = conn.getContentLength();
                    RandomAccessFile raf = new RandomAccessFile(apkPath, "rwd");
                    raf.setLength(fileLength);
                    raf.close();
                    int blockLength = fileLength / threadCount;

                    SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                    currLength = sp.getInt(CURR_LENGTH, 0);
                    for (int i = 0; i < threadCount; i++) {
                        int threadId = i + 1;
                        int startPosition = sp.getInt(SP_NAME + threadId, i * blockLength);
                        int endPosition = (i + 1) * blockLength - 1;
                        if (threadId == threadCount) {
                            endPosition = fileLength;
                        }
                        threadArr[i] = new DownloadThread(threadId, startPosition, endPosition);
                        threadArr[i].start();
                    }
                } else {
                    handler.sendMessage(handler.obtainMessage(STATE_ERROR, "下载受限啦，明日早来哦^_^"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(STATE_ERROR);
            }
        }).start();
    }

    public void start() {
        if (threadArr != null)
            synchronized (STATE_PAUSE) {
                downloadState = STATE_DOWNLOADING;
                STATE_PAUSE.notifyAll();
            }
    }

    public void pause() {
        if (threadArr != null) {
            downloadState = STATE_PAUSE;
        }
    }

    public void cancel() {
        if (threadArr != null) {
            if (downloadState.equals(STATE_PAUSE)) {
                start();
            }

            for (DownloadThread thread : threadArr) {
                thread.cancel();
            }
        }
    }

    public void destroy() {
        if (threadArr != null) {
            threadArr = null;
        }
    }

    private class DownloadThread extends Thread {

        private int threadId;
        private boolean isGoOn = true;
        private int currPosition;
        private int startPosition;
        private int endPosition;

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
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
                conn.setConnectTimeout(5000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    InputStream is = conn.getInputStream();
                    RandomAccessFile raf = new RandomAccessFile(apkPath, "rwd");
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
                            if (lastProgress != progress) {
                                lastProgress = progress;
                                handler.sendEmptyMessage(progress);
                            }
                        }

                        raf.write(buffer, 0, len);
                        currPosition += len;
                        synchronized (STATE_PAUSE) {
                            if (downloadState.equals(STATE_PAUSE)) {
                                STATE_PAUSE.wait();
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
                        handler.sendEmptyMessage(100);
                        handler.sendEmptyMessage(STATE_SUCCESS);
                        threadArr = null;
                    }
                } else {
                    sp.edit().clear().apply();
                    handler.sendEmptyMessage(STATE_ERROR);
                }
            } catch (Exception e) {
                sp.edit().clear().apply();
                e.printStackTrace();
                handler.sendEmptyMessage(STATE_ERROR);
            }
        }

        public void cancel() {
            isGoOn = false;
        }
    }

    public void setOnDownLoadListener(OnDownLoadListener onDownLoadListener) {
        this.onDownLoadListener = onDownLoadListener;
    }

    public interface OnDownLoadListener {
        void onProgress(int progress);

        void onSuccess();

        void onError();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (onDownLoadListener != null) {
                switch (msg.what) {
                    case STATE_SUCCESS:
                        onDownLoadListener.onSuccess();
                        break;
                    case STATE_ERROR:
                        onDownLoadListener.onError();
                        Object obj = msg.obj;
                        if (obj != null && obj instanceof String) {
                            Toast.makeText(context, (String) obj, Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        int progress = msg.what;
                        if (lastProgress != progress) {
                            lastProgress = progress;
                            onDownLoadListener.onProgress(progress);
                        }
                        break;
                }
            }
        }
    };
}
