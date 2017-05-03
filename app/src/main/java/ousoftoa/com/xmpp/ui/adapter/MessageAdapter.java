package ousoftoa.com.xmpp.ui.adapter;

import android.graphics.Bitmap;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lqr.emoji.MoonUtils;

import java.util.List;

import cn.bingoogolapple.badgeview.BGABadgeView;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.utils.DateUtil;
import ousoftoa.com.xmpp.utils.ImageUtil;

/**
 * Created by 韩莫熙 on 2017/4/14.
 */

public class MessageAdapter extends BaseQuickAdapter<ChatItem, BaseViewHolder> {
    private NewMsgDbHelper newMsgDbHelper;

    public MessageAdapter(int layoutResId, List<ChatItem> data) {
        super( layoutResId, data );
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatItem item) {
        newMsgDbHelper = NewMsgDbHelper.getInstance( mContext );
        if (item.chatType == ChatItem.CHAT) {
            helper.setText( R.id.tvName, item.nickName )
                    .setText( R.id.tvTime, DateUtil.getMsgFormatTime( item.sendDate ) );
            Bitmap headimg = ImageUtil.getBitmapFromBase64String( item.head );
            if (headimg != null)
                helper.setImageBitmap( R.id.ivHeader, headimg );
            else
                helper.setImageResource( R.id.ivHeader, R.mipmap.default_tp );
            if (item.msg != null) {
                if (item.subject == null || item.subject.equals( Constants.SEND_TXT )) {
                    TextView tvContent = helper.getView( R.id.tvMsg );
                    MoonUtils.identifyFaceExpression( mContext, tvContent, item.msg, ImageSpan.ALIGN_BOTTOM );
                } else if (item.subject.equals( Constants.SEND_IMG ))
                    helper.setText( R.id.tvMsg, "[图片]" );
                else if (item.subject.equals( Constants.SEND_SOUND ))
                    helper.setText( R.id.tvMsg, "[语音]" );
                else if (item.subject.equals( Constants.SEND_LOCATION ))
                    helper.setText( R.id.tvMsg, "[位置]" );
            }
        }
        //是否显示有消息
        int newCount = newMsgDbHelper.getMsgCount( item.chatName );
        BGABadgeView header = helper.getView( R.id.bVheadview );
        if (newCount > 0) {
            header.showTextBadge( newCount + "" );
            header.setDragDismissDelegage( badgeable -> newMsgDbHelper.delNewMsg( item.chatName ) );
        }else {
            header.hiddenBadge();
        }
    }
}
