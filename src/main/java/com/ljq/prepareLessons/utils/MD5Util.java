package com.ljq.prepareLessons.utils;

import com.alibaba.fastjson.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author 李佳琪
 * 2023-04-25
 */
public class MD5Util {
    public static void main(String[] args) throws Exception {
        String mmm = "come in ‘";
        mmm = mmm.replaceAll("‘", "").replaceAll(" ", "%20");
        System.out.println(mmm);

        JSONObject json = new JSONObject(new TreeMap<>());
        json.put("client", 6);
        json.put("key", 1000006);
        json.put("timestamp", 1682420578352L);
        //json.put("word", URLEncoder.encode("come%20out", "UTF-8"));
        json.put("word", "remove");
        ///dictionary/word/query/web610000061682418451836come%2520out7ece94d9f9c202b0d2ec557dg4r9bc
        //235f48d212b59f293f497aca38e342df


        String t = json.values().stream().map(Object::toString).collect(Collectors.joining());
        String str = "/dictionary/word/query/web" + t + "7ece94d9f9c202b0d2ec557dg4r9bc";

         str = "/dictionary/word/query/web610000061682420578352remove7ece94d9f9c202b0d2ec557dg4r9bc";
         //944ea3756df607f65c42709c39e9c3bb
        System.out.println(str);
        System.out.println(getMD5(str));
        //http://dict.iciba.com/dictionary/word/query/web?client=6&key=1000006&timestamp=1682417680310&word=come%20ouT&signature=afba42ea372644f8f437e871dce5e3b6
        //http://dict.iciba.com/dictionary/word/query/web?client=6&key=1000006&timestamp=1682418451836&word=come%2520ouT&signature=9dd0bce10517a9891a2616c239b0f946 正确的
             //                     /dictionary/word/query/   web61000006                1682418451836      come%2520ouT   7ece94d9f9c202b0d2ec557dg4r9bc
    }

    public static synchronized String getMD5(String strs) throws Exception {
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] bs = digest.digest(strs.getBytes());

            /*
             *  加密后的数据是-128 到 127 之间的数字，这个数字也不安全。
             *   取出每个数组的某些二进制位进行某些运算，得到一个具体的加密结果
             *
             *   0000 0011 0000 0100 0010 0000 0110 0001
             *  &0000 0000 0000 0000 0000 0000 1111 1111
             *  ---------------------------------------------
             *   0000 0000 0000 0000 0000 0000 0110 0001
             *   把取出的数据转成十六进制数
             */

            for (byte b : bs) {
                int x = b & 255;
                String s = Integer.toHexString(x);
                if (x > 0 && x < 16) {
                    sb.append("0");
                    sb.append(s);
                } else {
                    sb.append(s);
                }
            }

        } catch (Exception e) {
            System.out.println("加密失败");
        }
        return sb.toString();
    }
}
