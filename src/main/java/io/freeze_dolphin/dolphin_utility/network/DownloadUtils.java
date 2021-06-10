package io.freeze_dolphin.dolphin_utility.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DownloadUtils {

	public static InputStream download_from(String url, String ua, long time_out)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
		if (url.startsWith("https://")) {
			return download_from_https(url, ua, time_out);
		} else {
			return download_from_http(url, ua, time_out);
		}
	}

	private static InputStream download_from_http(String url, String ua, long time_out) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setReadTimeout((int) time_out);
		conn.setConnectTimeout((int) time_out);
		conn.setRequestProperty("User-Agent", ua);
		conn.connect();
		InputStream inputStream = conn.getInputStream();
		return inputStream;
	}

	private static InputStream download_from_https(String url, String ua, long time_out)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		URL u = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
		conn.setReadTimeout((int) time_out);
		conn.setConnectTimeout((int) time_out);
		conn.setRequestProperty("User-Agent", ua);
		conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
		conn.setSSLSocketFactory(ssf);
		conn.connect();
		InputStream inputStream = conn.getInputStream();
		return inputStream;
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	private static class TrustAnyTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}

	}

	public static long get_file_length(String url, String ua) throws IOException {
		if (url == null || "".equals(url)) {
			return 0L;
		}
		URL u = new URL(url);
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("HEAD");
			conn.setRequestProperty("User-Agent", ua);
			return (long) conn.getContentLength();
		} catch (IOException e) {
			return 0L;
		} finally {
			conn.disconnect();
		}
	}

}
