package ousoftoa.com.xmpp.scoket;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.util.Timer;
import java.util.TimerTask;


public class XmppConnecionListener implements ConnectionListener {
    private Timer tExit;
    private String username;
    private String password;
    private int logintime = 5000;

    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean b) {

    }

    @Override
    public void connectionClosed() {
        // 重连服务器
        tExit = new Timer();
        tExit.schedule(new timetask(), logintime);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        if (e.getMessage().contains("conflict")) {
           //异地登录
        } else {
            // 重连服务器
            tExit = new Timer();
            tExit.schedule(new timetask(), logintime);
        }
    }

    class timetask extends TimerTask {
        @Override
        public void run() {
//            XmppConnection.getInstance().closeConnection();
//            username = Constants.USER_NAME;
//            password = Constants.PWD;
//            if (username != null && password != null) {
//                // 连接服务器
//                boolean isConnect = XmppConnection.getInstance().login(username, password);
//                if (!isConnect) {
//                    tExit.schedule(new timetask(), logintime);
//                } else {
//                    new Handler().postDelayed( () -> {
//                        XmppConnection.getInstance().closeConnection();
//                        boolean isSuees = XmppConnection.getInstance().login(username, password);
//                        if (!isSuees) {
//                            tExit.schedule(new timetask(), logintime);
//                        }else {
////                            XmppConnection.getInstance().loadFriendAndJoinRoom();
//                        }
//                    },10000);
//                }
//            }
        }
    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int i) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }
}
