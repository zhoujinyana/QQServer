package com.zjy.qqserver.service;

import com.zjy.qqcommon.Message;
import com.zjy.qqcommon.MessageType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

public class ServerConnectClientThread extends Thread {
    private Socket socket;
    private String userId;//连接到服务端的用户

    public ServerConnectClientThread(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        while (true) {

            try {
                System.out.println(userId+"保持通信，读取数据");
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

                if(message.getMesType().equals(MessageType.MESSAGE_GET_ONLINE_FRIEND)){
                    System.out.println(message.getSender()+"要在线列表");

                    String onlineUser = ManageClientThreads.getOnlineUser();

                    Message message2 = new Message();
                    message2.setMesType(MessageType.MESSAGE_RET_ONLINE_FRIEND);
                    message2.setContent(onlineUser);
                    message2.setGetter(message.getSender());

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message2);


                }else if(message.getMesType().equals(MessageType.MESSAGE_COMM_MES)){

                    ServerConnectClientThread serverConnectClientThread = ManageClientThreads.getServerConnectClientThread(message.getGetter());

                    ObjectOutputStream oos =
                            new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());
                    oos.writeObject(message);//转发，如果客户不在线，可以保存到数据库，实现离线留言

                }else if(message.getMesType().equals(MessageType.MESSAGE_TO_ALL_MES)){
                    HashMap<String, ServerConnectClientThread> hm = ManageClientThreads.getHm();
                    Iterator<String> iterator = hm.keySet().iterator();

                    while(iterator.hasNext()){
                        String onLineUserId = iterator.next();

                        if(!onLineUserId.equals(message.getSender())){

                            ObjectOutputStream oos = new ObjectOutputStream(hm.get(onLineUserId).getSocket().getOutputStream());
                            oos.writeObject(message);
                        }
                    }


                }else if(message.getMesType().equals(MessageType.MESSAGE_FILE_MES)){
                    ServerConnectClientThread serverConnectClientThread = ManageClientThreads.getServerConnectClientThread(message.getGetter());
                    ObjectOutputStream oos = new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());
                    oos.writeObject(message);

                }
                else if(message.getMesType().equals(MessageType.MESSAGE_CLIENT)){
                    System.out.println(message.getSender()+"退出系统");
                    ManageClientThreads.removeServerConnectClientThread(message.getSender());

                    socket.close();
                    break;
                }

                else{
                    System.out.println("暂时不处理");

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
