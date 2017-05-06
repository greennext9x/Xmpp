package ousoftoa.com.xmpp.model.bean;

import java.io.Serializable;

/**
 * Created by 韩莫熙 on 2017/5/5.
 */

public class UserInfoData implements Serializable{
    private String type;
    private String title;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
