package com.toucha.analytics.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.model.AccessToken;

/**
 * Build access token
 *
 * @author senhui.li
 */
@SuppressWarnings("deprecation")
public class AuthServerOAuth2Util {

    private static final String KEY_STORE = "JKS";

    private static final String KEY_STORE_NAME = "ZhaBeiPlatformServiceAccount.keystore";

    private static final String ALIAS = "26e02a34-eaab-4b21-9b97-c3f706336a19";

    private static final String KEY_STORE_PASS = "ZhaBeiPlatform101";

    private static final String ALIAS_PASS = "ZhaBeiPlatform101";

    private static final String ACCESS_TOKEN_PATH = ApplicationConfig.AuthServerGetAccessToken;

    private static final String PLATFORM_CLIENT_USERNAME = ApplicationConfig.PLATFORMClientUserName;

    // Ty HengDa ServiceAccount
    private static final String PLATFORM_CLIENT_PASSWORD = ApplicationConfig.PLATFORMClientPassWord;

    // 设置连接超时
    private static final int CONNECTION_TIMEOUT = ApplicationConfig.AUTH_SERVER_CONNECTION_TIMEOUT * 1000;

    // Socket超时
    private static final int SOCKET_TIMEOUT = ApplicationConfig.AUTH_SERVER_SOCKET_TIMEOUT * 1000;

    private final static Map<String, AccessToken> _tokens = new ConcurrentHashMap<>();

    public static String getAccessToken() throws Exception {
        AccessToken at = _tokens.get(PLATFORM_CLIENT_USERNAME);
        synchronized (AuthServerOAuth2Util.class){
            if(at == null){
                at = getAccessTokenWithSignature();
                _tokens.put(PLATFORM_CLIENT_USERNAME, at);
            } else if(!at.isAvailable()){
                at = refreshAccessTokenWithSignature(at.getRefreshToken());
                _tokens.put(PLATFORM_CLIENT_USERNAME, at);
            }
        }
        return at.getAccessToken();
    }

    private static AccessToken getAccessTokenWithSignature() throws Exception {

        AccessToken accessToken = null;
        String HDOilBasicHeader = Base64.encodeBase64String((PLATFORM_CLIENT_USERNAME + ":" + PLATFORM_CLIENT_PASSWORD)
                .getBytes("UTF-8"));
        JSONObject getAccessTokenResponse = null;

        // 增加服务端完整性校验的获取Token请求
        String postParams = "username=" + PLATFORM_CLIENT_USERNAME + "&password=" + PLATFORM_CLIENT_PASSWORD
                + "&grant_type=password";
        // String sig = getSignature(postParams);
        String sig = sign(postParams, KEY_STORE_NAME, ALIAS, KEY_STORE_PASS, ALIAS_PASS);
        if (sig != null) {
            postParams += sig;
            getAccessTokenResponse = sendRequest(ACCESS_TOKEN_PATH, postParams, HDOilBasicHeader);

            accessToken = new AccessToken(getAccessTokenResponse.get("access_token").toString(),
                    getAccessTokenResponse.get("refresh_token").toString(),
                    getAccessTokenResponse.getIntValue("expires_in"));
        }
        return accessToken;
    }

    private static AccessToken refreshAccessTokenWithSignature(String refreshAccessToken) throws Exception {

        AccessToken newAccessToken = null;

        // 增加服务端完整性校验的刷新Token请求
        String HDOilBasicHeader = Base64.encodeBase64String((PLATFORM_CLIENT_USERNAME + ":" + PLATFORM_CLIENT_PASSWORD).getBytes("UTF-8"));
        String postParams = "refresh_token=" + refreshAccessToken + "&grant_type=refresh_token";

        String sig = sign(postParams, KEY_STORE_NAME, ALIAS, KEY_STORE_PASS, ALIAS_PASS);
        if (sig != null) {
            postParams += sig;
            JSONObject refreshResp = sendRequest(ACCESS_TOKEN_PATH, postParams, HDOilBasicHeader);

            newAccessToken = new AccessToken(refreshResp.getString("access_token"),
                    refreshResp.getString("refresh_token"),
                    refreshResp.getIntValue("expires_in"));
        }

        return newAccessToken;
    }

    /**
     * Get KeyStore
     *
     * @param keyStoreResourceName
     * @param password
     * @return
     * @throws Exception
     */
    private static KeyStore getKeyStore(String keyStoreResourceName, String password) throws GeneralSecurityException,
            IOException {

        String path = "/" + keyStoreResourceName;
        try (InputStream is = AuthServerOAuth2Util.class.getResourceAsStream(path)) {
            KeyStore ks = KeyStore.getInstance(KEY_STORE);
            ks.load(is, password.toCharArray());
            return ks;
        }
    }

    /**
     * Get private key by KeyStore
     *
     * @param keyStoreResourceName
     * @param keyStorePassword
     * @param alias
     * @param aliasPassword
     * @return
     * @throws Exception
     */
    private static PrivateKey getPrivateKey(String keyStoreResourceName, String keyStorePassword, String alias,
                                            String aliasPassword) throws Exception {
        KeyStore ks = getKeyStore(keyStoreResourceName, keyStorePassword);
        PrivateKey key = (PrivateKey) ks.getKey(alias, aliasPassword.toCharArray());
        return key;
    }

    /**
     * signature
     *
     * @param keyStoreResourceName
     * @param alias
     * @param keyStorePassword
     * @param aliasPassword
     * @return
     * @throws Exception
     */
    private static String sign(String content, String keyStoreResourceName, String alias, String keyStorePassword,
                              String aliasPassword) throws Exception {
        byte[] sign = content.getBytes("utf-8");

        // 取得私钥
        PrivateKey privateKey = getPrivateKey(keyStoreResourceName, keyStorePassword, alias, aliasPassword);

        // 构建签名
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(sign);
        byte[] signatureBytes = signature.sign();
        StringBuffer res = new StringBuffer();
        res.append("&assertion=").append(new String(Base64.encodeBase64URLSafeString(signatureBytes)));
        return res.toString();
    }

    /**
     *
     *
     * @param url
     *            请求access_token的地址
     * @param postBody
     *            请求的内容
     * @param authHeader
     *            请求的头信息
     * @return access_token
     * @throws Exception
     */
    private static JSONObject sendRequest(String url, String postBody, final String authHeader) throws Exception {
        CloseableHttpClient client = getSSLHttpClient();
        try {
            HttpEntity bodyEntity = new StringEntity(postBody, Consts.UTF_8);
            Header[] headers = new Header[6];
            headers[0] = new BasicHeader("Cache-Control", "no-store,no-cache");
            headers[1] = new BasicHeader("Connection", "Keep-Alive");
            headers[2] = new BasicHeader("Pragma", "no-cache");
            headers[3] = new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            headers[4] = new BasicHeader("Authorization", "Basic " + authHeader);
            headers[5] = new BasicHeader("Expect", "100-continue");

            HttpPost post = new HttpPost(url);
            post.setHeaders(headers);
            post.setEntity(bodyEntity);
            HttpResponse resp = client.execute(post);
            int statusCode = resp.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK) {
                String jsonObj = EntityUtils.toString(resp.getEntity(), "utf-8");
                JSONObject root = JSONObject.parseObject(jsonObj);
                if (root.containsKey("access_token")) {
                    return root;
                } else {
                    throw new RuntimeException("请求成功，但没有access_token！");
                }
            }

            throw new RuntimeException("请求失败！");
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }


    public static CloseableHttpClient getSSLHttpClient() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // 不应该信任所有证书，应该验证SSL证书
        trustStore.load(null, null);
        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", sf, 443));

        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
        CloseableHttpClient client = new DefaultHttpClient(ccm, params);

        return client;
    }


    private static class MySSLSocketFactory extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException
                 {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
