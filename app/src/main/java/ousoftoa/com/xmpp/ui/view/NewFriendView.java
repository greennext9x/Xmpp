package ousoftoa.com.xmpp.ui.view;

import java.util.List;

import ousoftoa.com.xmpp.base.IBaseView;
import ousoftoa.com.xmpp.model.bean.Friend;

/**
 * Created by 韩莫熙 on 2017/4/12.
 */

public interface NewFriendView extends IBaseView{
    void onNext(List<Friend> mlist);
    void onAddNext();
}
