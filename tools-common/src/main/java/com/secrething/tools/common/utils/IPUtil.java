package com.secrething.tools.common.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liuzz on 2018/4/14.
 */
public class IPUtil {
    private static final String WEB_IP;
    static {
        WEB_IP = getV4IP();
    }
    private static String getV4IP() {
        String ip = null;
        String chinaz = "http://ip.chinaz.com/getip.aspx";

        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url;
        url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read + "\r\n");
            }
            String json = inputLine.toString();
            int end = json.indexOf(',')-1;
            int begin = json.indexOf('\'')+1;
            ip = json.substring(begin,end);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return ip;
    }

    public static String getIPV4(){
        return WEB_IP;
    }
}