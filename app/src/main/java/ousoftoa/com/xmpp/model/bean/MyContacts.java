package ousoftoa.com.xmpp.model.bean;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * Created by 韩莫熙 on 2017/4/10.
 */

public class MyContacts extends SectionEntity<Friend>{

    public MyContacts(boolean isHeader, String header) {
        super( isHeader, header );
    }

    public MyContacts(Friend friend) {
        super( friend );
    }

}
