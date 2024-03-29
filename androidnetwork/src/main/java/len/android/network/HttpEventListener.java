package len.android.network;

import android.support.annotation.Nullable;
import len.tools.android.Log;
import okhttp3.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpEventListener extends EventListener {
    private long startNs;

    public HttpEventListener() {
    }

    @Override
    public void callStart(Call call) {
        startNs = System.nanoTime();

        logWithTime("callStart: " + call.request());
    }

    @Override
    public void dnsStart(Call call, String domainName) {
        logWithTime("dnsStart: " + domainName);
    }

    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        logWithTime("dnsEnd: " + inetAddressList);
    }

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        logWithTime("connectStart: " + inetSocketAddress + " " + proxy);
    }

    @Override
    public void secureConnectStart(Call call) {
        logWithTime("secureConnectStart");
    }

    @Override
    public void secureConnectEnd(Call call, @Nullable Handshake handshake) {
        logWithTime("secureConnectEnd: " + handshake);
    }

    @Override
    public void connectEnd(
            Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {
        logWithTime("connectEnd: " + protocol);
    }

    @Override
    public void connectFailed(
            Call call,
            InetSocketAddress inetSocketAddress,
            Proxy proxy,
            @Nullable Protocol protocol,
            IOException ioe) {
        logWithTime("connectFailed: " + protocol + " " + ioe);
    }

    @Override
    public void connectionAcquired(Call call, Connection connection) {
        logWithTime("connectionAcquired: " + connection);
    }

    @Override
    public void connectionReleased(Call call, Connection connection) {
        logWithTime("connectionReleased");
    }

    @Override
    public void requestHeadersStart(Call call) {
        logWithTime("requestHeadersStart");
    }

    @Override
    public void requestHeadersEnd(Call call, Request request) {
        logWithTime("requestHeadersEnd");
    }

    @Override
    public void requestBodyStart(Call call) {
        logWithTime("requestBodyStart");
    }

    @Override
    public void requestBodyEnd(Call call, long byteCount) {
        logWithTime("requestBodyEnd: byteCount=" + byteCount);
    }

    @Override
    public void responseHeadersStart(Call call) {
        logWithTime("responseHeadersStart");
    }

    @Override
    public void responseHeadersEnd(Call call, Response response) {
        logWithTime("responseHeadersEnd: " + response);
    }

    @Override
    public void responseBodyStart(Call call) {
        logWithTime("responseBodyStart");
    }

    @Override
    public void responseBodyEnd(Call call, long byteCount) {
        logWithTime("responseBodyEnd: byteCount=" + byteCount);
    }

    @Override
    public void callEnd(Call call) {
        logWithTime("callEnd");
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        logWithTime("callFailed: " + ioe);
    }

    private void logWithTime(String message) {
        long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        Log.d("[" + timeMs + " ms] " + message);
    }
}
