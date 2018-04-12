package com.secrething.tools.local;

import com.secrething.tools.local.server.Server;

/**
 * Created by liuzz on 2018/4/12.
 */
public class Demo {

    private static void startLocalServer() throws Exception {
        //LocalServer server = new LocalServer("hello");
        Server server = new Server("hello");
        server.run();
    }

    public static void main(String[] args) throws Exception {
        Thread serverThread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    startLocalServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();

        Thread.sleep(1000);
        /*LocalClient client = new LocalClient("hello");
        client.sendRequest("hello");*/
        String url = "http://59.110.6.12:8080/tuniuhm/search";
        String request = "{\"type\":\"0\",\"cid\":\"tuniu\",\"tripType\":\"1\",\"fromCity\":\"BKK\",\"toCity\":\"HKT\",\"fromDate\":\"20180421\",\"all\":\"\",\"adultNum\":\"1\",\"childNum\":\"0\",\"infantNumber\":\"0\",\"retDate\":\"\"}";
        int waitTime = 60000;
        String res = ProxyHttpPoolManage.sendJsonPostRequest(url, request, waitTime);
        System.out.println(res);
       /* Thread clientThread = new Thread(new Runnable() {
            @Override public void run() {
                startLocalClient();
            }
        });
        clientThread.start();*/
    }
}
