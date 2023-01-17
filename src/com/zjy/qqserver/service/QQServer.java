package com.zjy.qqserver.service;

import com.zjy.qqcommon.Message;
import com.zjy.qqcommon.MessageType;
import com.zjy.qqcommon.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class QQServer {
    private ServerSocket ss = null;
//创建集合，存放用户
    private static HashMap<String,User> validUsers = new HashMap<>();
    static {
        validUsers.put("100",new User("100","123456"));
        validUsers.put("200",new User("200","123456"));
        validUsers.put("300",new User("300","123456"));
        validUsers.put("400",new User("400","123456"));
    }

    private boolean checkUser(String userId,String passwd){
        User user = validUsers.get(userId);
        if(user == null){
            return false;
        }
        if(!user.getPasswd().equals(passwd)){
            return false;
        }
        return true;
    }


    public QQServer() {


        try {
            System.out.println("服务器在9999端口监听。。。");

            new Thread(new SendNewsToAllService()).start();

            ss = new ServerSocket(9999);

            while (true) {

                Socket socket = ss.accept();
                //得到socket关联的对象输入流
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());


                User u = (User) ois.readObject();


                Message message = new Message();
                if (checkUser(u.getUserId(),u.getPasswd())) {
                    message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);
                    oos.writeObject(message);

                    //创建线程
                    ServerConnectClientThread serverConnectClientThread = new ServerConnectClientThread(socket, u.getUserId());

                    serverConnectClientThread.start();

                    //放入集合
                    ManageClientThreads.addClientThread(u.getUserId(), serverConnectClientThread);


                } else {

                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);
                    oos.writeObject(message);
                    socket.close();

                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
