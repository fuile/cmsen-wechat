/**
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.wechat;

import com.cmsen.common.http.ClientHttpResponse;
import com.cmsen.common.http.ClientHttps;
import com.cmsen.common.util.FileUtil;
import com.cmsen.common.util.JsonUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.util.Base64;

public abstract class WeChatService {
    private static String ACCESS_TOKEN_FILENAME = "wx_access_token.tmp";
    private static String JS_API_TICKET_FILENAME = "wx_js_api_ticket.tmp";
    private static String ACCESS_TOKEN_PATH;
    private static String JS_API_TICKET_PATH;
    private static long EXPIRES_IN;

    public static void setAccessTokenPath(String accessTokenPath) {
        ACCESS_TOKEN_PATH = accessTokenPath + ACCESS_TOKEN_FILENAME;
    }

    public static void setJsApiTicketPath(String jsApiTicketPath) {
        JS_API_TICKET_PATH = jsApiTicketPath + JS_API_TICKET_FILENAME;
    }

    public static void setExpiresIn(long expiresIn) {
        WeChatService.EXPIRES_IN = expiresIn;
    }

    public static AccessTokenResult getAccessToken(String url) {
        File file = new File(ACCESS_TOKEN_PATH);
        if (file.exists() && System.currentTimeMillis() - file.lastModified() <= EXPIRES_IN && file.length() > 0) {
            return JsonUtil.toClass(new String(FileUtil.getBytes(file)), AccessTokenResult.class);
        } else {
            ClientHttpResponse result = ClientHttps.get(url);
            AccessTokenResult accessTokenResult = JsonUtil.toClass(result.toString(), AccessTokenResult.class);
            if (null != accessTokenResult && null != accessTokenResult.getAccessToken()) {
                try {
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(result.toString());
                    bw.close();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return accessTokenResult;
        }
    }

    public static boolean clearAccessToken() {
        try {
            FileWriter fw = new FileWriter(ACCESS_TOKEN_PATH);
            fw.write("");
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    public static JsTicketResult getJsApiTicket(String url) {
        File file = new File(JS_API_TICKET_PATH);
        if (file.exists() && System.currentTimeMillis() - file.lastModified() <= EXPIRES_IN && file.length() > 0) {
            return JsonUtil.toClass(new String(FileUtil.getBytes(file)), JsTicketResult.class);
        } else {
            ClientHttpResponse result = ClientHttps.get(url);
            JsTicketResult accessTokenResult = JsonUtil.toClass(result.toString(), JsTicketResult.class);
            if (null != accessTokenResult && null != accessTokenResult.getTicket()) {
                try {
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(result.toString());
                    bw.close();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return accessTokenResult;
        }
    }


    public static boolean clearJsApiTicket() {
        try {
            FileWriter fw = new FileWriter(JS_API_TICKET_PATH);
            fw.write("");
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    public static JsCode2sessionResult getJsCode2session(String url) {
        ClientHttpResponse result = ClientHttps.get(url);
        return JsonUtil.toClass(result.toString(), JsCode2sessionResult.class);
    }

    /**
     * 检验数据的真实性，并且获取解密后的明文
     *
     * @param encryptedData  //密文，被加密的数据
     * @param rawData        //源密文，未加密的数据
     * @param session_key    //秘钥
     * @param iv             //偏移量
     * @param signature      //签名
     * @param encodingFormat //解密后的结果需要进行的编码
     * @return String
     */
    public static String decryptUserInfo(String encryptedData, String rawData, String session_key, String iv, String signature, String encodingFormat) {
        // Security.addProvider(new BouncyCastleProvider());
        if (signature.equals(SHA1Util.encode(rawData + session_key))) {
            //被加密的数据
            byte[] dataByte = Base64.getDecoder().decode(encryptedData);
            //加密秘钥
            byte[] keyByte = Base64.getDecoder().decode(session_key);
            //偏移量
            byte[] ivByte = Base64.getDecoder().decode(iv);
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
                AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
                parameters.init(new IvParameterSpec(ivByte));
                // 初始化
                cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
                byte[] resultByte = cipher.doFinal(dataByte);
                if (null != resultByte && resultByte.length > 0) {
                    return new String(resultByte, encodingFormat);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static {
        ACCESS_TOKEN_PATH = FileUtil.getTmpDir() + ACCESS_TOKEN_FILENAME;
        JS_API_TICKET_PATH = FileUtil.getTmpDir() + JS_API_TICKET_FILENAME;
        EXPIRES_IN = 7200000L;
    }
}
