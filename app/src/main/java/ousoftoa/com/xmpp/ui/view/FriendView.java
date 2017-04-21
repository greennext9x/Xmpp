package ousoftoa.com.xmpp.ui.view;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.IBaseView;

/**
 * Created by 韩莫熙 on 2017/4/11.
 */

public interface FriendView extends IBaseView {
    void onNext(VCard vCard);
    void onAddNext();
    void onDeleteNext();
}
