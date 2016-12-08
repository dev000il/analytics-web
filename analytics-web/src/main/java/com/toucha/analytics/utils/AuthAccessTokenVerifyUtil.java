package com.toucha.analytics.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.util.DateHelper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;

/**
 * Verify access token valid.
 *
 * @author senhui.li
 */
public class AuthAccessTokenVerifyUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuthAccessTokenVerifyUtil.class);

    private static Certificate _signatureCert;

    static {
        try {
            InputStream certStream = ApplicationConfig.class
                    .getResourceAsStream(ApplicationConfig.JWT_RSA_SIGN_VERIFY_KEY);
            BufferedInputStream bis = new BufferedInputStream(certStream);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            _signatureCert = cf.generateCertificate(bis);

        } catch (CertificateException e) {
            logger.error("", e);
        }
    }

    /**
     * If token is right,set the phone number,else set null.
     *
     * @param token
     * @return 0 success 1 error_token 2 expiration
     */
    public static int validateToken(String token, String[] phoneNum) {
        phoneNum[0] = null;
        String[] sections = token.split("\\.");
        if (sections.length != 3) {
            // illegal format
            return 1;
        }

        if (!containsValidSignature(sections)) {
            return 1;
        }

        try {
            // verify the expiration date
            byte[] jwtPayloadByte = Base64UrlDecode(sections[1]);
            String jwtPayload = new String(jwtPayloadByte, "utf-8");

            /*
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jwtPayload);
            */

            JSONObject jobj = JSON.parseObject(jwtPayload);
            String uniqueName = jobj.getString("unique_name");
            String expirationTime = jobj.getString("urn:oauth:exp");

            /*
            // String issue = node.get("iss").asText();
            // String audience = node.get("aud").asText();
            String uniqueName = node.get("unique_name").asText();
            // String scope = node.get("urn:oauth:scope").asText();
            String expirationTime = node.get("urn:oauth:exp").asText();
            // String issueTime = node.get("urn:oauth:iat").asText();
             **/

            Date now = DateHelper.getGMTadd8();
            Date expireDay = new Date(Long.parseLong(expirationTime) * 1000);
            if (now.before(expireDay)) {
                phoneNum[0] = uniqueName;
                return 0;
            } else {
                return 2;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    private static boolean containsValidSignature(String[] tokens) {
        String data = tokens[0] + "." + tokens[1];
        byte[] dataAsBytes = null;

        try {
            dataAsBytes = data.getBytes("utf-8");

            // The token could be RSA signed
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(_signatureCert);
            signature.update(dataAsBytes);
            if (signature.verify(Base64.decodeBase64(tokens[2]))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static byte[] Base64UrlDecode(String base64UrlString) {
        base64UrlString = base64UrlString.replace('-', '+'); // 62nd char of
        // encoding
        base64UrlString = base64UrlString.replace('_', '/'); // 63rd char of
        // encoding

        switch (base64UrlString.length() % 4) // Pad with trailing '='s
        {
            case 0:
                break; // No pad chars in this case
            case 2:
                base64UrlString += "==";
                break; // Two pad chars
            case 3:
                base64UrlString += "=";
                break; // One pad char
            default:
                return null; // Invalid base64url string!
        }

        return Base64.decodeBase64(base64UrlString); // Standard base64 decoder
    }
}
