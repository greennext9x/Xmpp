package ousoftoa.com.xmpp.ui.view;

import java.util.List;

import ousoftoa.com.xmpp.base.IBaseView;
import ousoftoa.com.xmpp.model.bean.ChatItem;

/**
 * Created by 韩莫熙 on 2017/4/14.
 */

public interface MessageView extends IBaseView{
    void onNext(List<ChatItem> itemList);
}
