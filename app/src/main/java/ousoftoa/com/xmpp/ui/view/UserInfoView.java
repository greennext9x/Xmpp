package ousoftoa.com.xmpp.ui.view;

import android.graphics.Bitmap;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.IBaseView;

/**
 * Created by 韩莫熙 on 2017/4/18.
 */

public interface UserInfoView extends IBaseView{
    void onNext(VCard vCard);
    void onSetHeard(Bitmap bitmap);
}
