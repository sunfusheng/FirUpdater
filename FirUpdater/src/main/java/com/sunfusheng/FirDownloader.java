package com.sunfusheng;

import android.content.Context;
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

    private static final int STATE_SUCCESS = -1;
    private static final int STATE_ERROR = -2;

    private Context context;
    private String fileName;
    private String filePath;
    private String fileUrl;
    private int fileLength;
    private int currLength;

    private boolean isGoOn = true;
    private int lastProgress = 0;
    private OnDownLoadListener onDownLoadListener;

    public FirDownloader(Context context, String fileName, String filePath, String fileUrl, int fileLength) {
        this.context = context;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.fileLength = fileLength;
    }

    public void downloadApk() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(fileUrl).openConnection();
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                currLength = FirUpdaterUtils.getCurrLengthValue(context, fileName);
                conn.setRequestProperty("Range", "bytes=" + currLength + "-" + fileLength);
                FirUpdaterUtils.logger("currLength: " + currLength + " fileLength: " + fileLength);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    InputStream is = conn.getInputStream();
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
                    raf.setLength(fileLength);
                    raf.seek(currLength);

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
                    }

                    FirUpdaterUtils.closeQuietly(is, raf);

                    if (!isGoOn && currLength < fileLength) {
                        FirUpdaterUtils.putCurrLengthValue(context, fileName, currLength);
                    } else {
                        FirUpdaterUtils.putCurrLengthValue(context, fileName, 0);
                        handler.sendEmptyMessage(100);
                        handler.sendEmptyMessage(STATE_SUCCESS);
                    }
                } else {
                    handler.sendMessage(handler.obtainMessage(STATE_ERROR, "下载受限啦，明日早来哦^_^"));
                }
            } catch (Exception e) {
                FirUpdaterUtils.loggerError(e);
                FirUpdaterUtils.putCurrLengthValue(context, fileName, 0);
                handler.sendEmptyMessage(STATE_ERROR);
            }
        }).start();
    }

    public void cancel() {
        isGoOn = false;
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
                        if (msg.obj != null && msg.obj instanceof String) {
                            Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        if (lastProgress != msg.what) {
                            lastProgress = msg.what;
                            onDownLoadListener.onProgress(msg.what);
                        }
                        break;
                }
            }
        }
    };
}
