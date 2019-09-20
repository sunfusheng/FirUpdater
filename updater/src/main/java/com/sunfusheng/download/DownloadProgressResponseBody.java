package com.sunfusheng.download;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public class DownloadProgressResponseBody extends ResponseBody {
    private ResponseBody mResponseBody;
    private IDownloadListener mDownloadListener;
    private BufferedSource mBufferedSource;

    public DownloadProgressResponseBody(ResponseBody responseBody, IDownloadListener downloadListener) {
        this.mResponseBody = responseBody;
        this.mDownloadListener = downloadListener;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (mDownloadListener != null && contentLength() > 0) {
                    int percentage = (int) (totalBytesRead * 100 / contentLength());
                    if (bytesRead != -1) {
                        AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                            mDownloadListener.onProgress(totalBytesRead, contentLength(), percentage);
                        });
                    }

                }
                return bytesRead;
            }
        };
    }
}
