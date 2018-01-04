package com.pictureair.hkdlphotopass.widget.wheelview;

import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pictureair.hkdlphotopass.widget.CustomWebView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by tech-beyondren on 18/1/2.
 */

public class CustomWebClient extends WebViewClient {
    private CustomWebView.MyWebviewImp myWebviewImp;

    public CustomWebClient(CustomWebView.MyWebviewImp myWebviewImp) {
        this.myWebviewImp = myWebviewImp;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (null != myWebviewImp) {
            myWebviewImp.webViewFailedToLoad();
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            HttpURLConnection urlConnection = createConnection(url);
            InputStream is = urlConnection.getInputStream();
            String contentType = urlConnection.getContentType();
            String encoding = urlConnection.getContentEncoding();
            if (contentType != null) {
                String mimeType = contentType;
                if (contentType.contains(";")) {
                    mimeType = contentType.split(";")[0].trim();
                }
                return new WebResourceResponse(mimeType, encoding, is);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.shouldInterceptRequest(view, url);
    }

    public static final SSLContext getSSLContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String s) throws CertificateException {

            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        }};

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }

    public HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = createConnection(url, "");
        SSLContext sslContext = null;
        if (connection instanceof HttpsURLConnection) {
            try {
                sslContext = getSSLContext();//不验证证书
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sslContext != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                ((HttpsURLConnection) connection).setHostnameVerifier((new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                }));
            }
        }
        return connection;
    }

    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~\'%");
        HttpURLConnection conn = (HttpURLConnection) (new URL(encodedUrl)).openConnection();
        conn.setConnectTimeout(30);
        conn.setReadTimeout(30);
        return conn;
    }
}
