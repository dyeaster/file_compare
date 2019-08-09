package com.ztesoft.config.compare.utils;

import ch.ethz.ssh2.Connection;
import com.ztesoft.config.compare.entity.HostDetail;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class HostUtil {
    private static final String KEY = "iwhalecloud";
    private static final String SALT = "00000000";
    private static byte[] iv = SALT.getBytes();

    private HostUtil() {

    }

    public static Map<String, Object> hostDetailList2Map(List<HostDetail> hostDetails) {
        Map<String, Object> map = new HashMap<>();
        for (HostDetail hostDetail : hostDetails) {
//            if(hostDetail.getKey() != null && hostDetail.getKey().equalsIgnoreCase("password")) {
//                hostDetail.setValue(HostUtil.decryptDES(hostDetail.getValue()));
//            }
            map.put(hostDetail.getKey(), hostDetail.getValue());
        }
        return map;
    }


    public static List<HostDetail> map2HostDetailList(Map<String, String> param, Long hostId) {
        List<HostDetail> hostDetails = new ArrayList<>();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            HostDetail hostDetail = new HostDetail();
            hostDetail.setHostId(hostId);
            hostDetail.setKey(mapKey);
            hostDetail.setValue(mapValue);
            hostDetails.add(hostDetail);
        }
        return hostDetails;
    }

    /**
     * 验证host是否能成功登录
     *
     * @param param
     * @return
     */
    public static boolean checkHost(Map<String, String> param) {
        String hostIp = param.get("hostIp");
        String user = param.get("user");
        String password = param.get("password");
        String port = param.get("port");
        Connection connection = new Connection(hostIp);
        try {
            connection.connect();
            System.out.println("开始登录");
            System.out.println("ip: " + hostIp);
            System.out.println("user: " + user);
            System.out.println("password: " + password);
            return connection.authenticateWithPassword(user, password);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("登录服务器失败！");
            return false;
        } finally {
            connection.close();
        }
    }

    //加密
    public static String encryptDES(String encryptString) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        DESKeySpec keySpec = null;
        byte[] encryptedData = null;
        try {
            keySpec = new DESKeySpec(KEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            encryptedData = cipher.doFinal(encryptString.getBytes());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encryptedData);
    }


    //解密
    public static String decryptDES(String decryptString) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] byteMi = decoder.decode(decryptString);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        try {
            DESKeySpec keySpec = new DESKeySpec(KEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            // 真正开始解密操作
            String result = new String(cipher.doFinal(byteMi));
//            加密后的密码
//            System.out.println("加密后的密码： " + result);
            return result;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptString;
    }

    public static void main(String[] args) throws Exception {
        String a = "gdgdg";
        String b = encryptDES(a);
        System.out.println(b);
        String c = decryptDES(b);
        System.out.println(c);

    }
}
