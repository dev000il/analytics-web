package com.toucha.analytics.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * a kpl client tool
 *
 * @author senhui.li
 */
public class HttpClientUtil {


    public static final String APPLICATION_JSON = "application/json";

    public static String signPost(String url,
                              String contentType,
                              String body) throws Exception {

        String accessToken = AuthServerOAuth2Util.getAccessToken();
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost post = new HttpPost(url);
            post.setHeader("authorization", "Bearer "+accessToken);
            // next release will remove it also use access token.
            post.setHeader("AuthInfo", "sao.so");
            post.setHeader("Content-Type", contentType);
            post.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));

            HttpResponse resp = client.execute(post);
            int status = resp.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = resp.getEntity();
                String content = (null != entity) ? EntityUtils.toString(entity, "UTF-8") : "";
                return content;
            } else {
                throw new ClientProtocolException("Server response failed. The status code: " + status);
            }
        } finally {
            if(client != null){
                client.close();
            }
        }
    }
}
