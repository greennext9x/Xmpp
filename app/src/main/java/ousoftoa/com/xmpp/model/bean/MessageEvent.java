package ousoftoa.com.xmpp.model.bean;

/**
 * Created by 韩莫熙 on 2017/4/11.
 */

public class MessageEvent {
    private String tag;
    private String message;

    public MessageEvent(String tag, String message) {
        this.tag = tag;
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }

}
