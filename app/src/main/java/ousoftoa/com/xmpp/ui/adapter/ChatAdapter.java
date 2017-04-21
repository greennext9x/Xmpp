package ousoftoa.com.xmpp.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.emoji.MoonUtils;

import java.io.IOException;
import java.util.List;

import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.utils.DateUtil;
import ousoftoa.com.xmpp.utils.ImageUtil;
import ousoftoa.com.xmpp.utils.UIUtils;

/**
 * Created by 韩莫熙 on 2017/4/20.
 */

public class ChatAdapter extends BaseMultiItemQuickAdapter<ChatItem, BaseViewHolder> {
    public ChatAdapter(List<ChatItem> data) {
        super( data );
        addItemType( ChatItem.CHAT_IN, R.layout.item_chat_in );
        addItemType( ChatItem.CHAT_OUT, R.layout.item_chat_out );
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatItem item) {
        setTime( helper, item, helper.getLayoutPosition() );
        setName( helper, item );
        setHead( helper, item );
        setMsg( helper, item );
    }

    private void setTime(BaseViewHolder helper, ChatItem item, int position) {
        long msgTime = item.sendDate;
        if (position > 0) {
            ChatItem chatItem = mData.get( position - 1 );
            long premsgTime = chatItem.sendDate;
            if (msgTime - premsgTime > (5 * 60 * 1000)) {
                helper.setVisible( R.id.tvTime, true ).setText( R.id.tvTime, DateUtil.getMsgFormatTime( msgTime ) );
            } else {
                helper.setVisible( R.id.tvTime, false );
            }
        } else {
            helper.setVisible( R.id.tvTime, true ).setText( R.id.tvTime, DateUtil.getMsgFormatTime( msgTime ) );
        }
    }

    private void setName(BaseViewHolder helper, ChatItem item) {
        if (item.chatType == ChatItem.GROUP_CHAT) {
            helper.setVisible( R.id.tvName, true )
                    .setText( R.id.tvName, item.nickName );
        } else {
            helper.setVisible( R.id.tvName, false );
        }
    }

    private void setHead(BaseViewHolder helper, ChatItem item) {
        Bitmap headimg = ImageUtil.getBitmapFromBase64String( item.head );
        if (headimg != null)
            helper.setImageBitmap( R.id.ivAvatar, headimg );
        else
            helper.setImageResource( R.id.ivAvatar, R.mipmap.default_tp );
    }

    private void setMsg(BaseViewHolder helper, ChatItem item) {
        if (item.msg.contains( Constants.SAVE_IMG_PATH )) {
            showBivPic( helper, item );
        } else if (item.msg.contains( Constants.SAVE_SOUND_PATH )) {
            showSound( helper, item );
        } else if (item.msg.contains( "[/a0" )) {
            showLocation( helper, item );
        } else {
            showTxtMsg( helper, item );
        }
    }

    private void showTxtMsg(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, false )
                .setVisible( R.id.tvText, true )
                .setVisible( R.id.bivPic, false );
        TextView tvContent = helper.getView( R.id.tvText );
        tvContent.setText( item.msg );
        MoonUtils.identifyFaceExpression( mContext, tvContent, item.msg, ImageSpan.ALIGN_BOTTOM );
    }

    private void showLocation(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, false )
                .setVisible( R.id.tvText, false )
                .setVisible( R.id.bivPic, false );
    }

    private void showSound(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, true )
                .setVisible( R.id.tvText, false )
                .setVisible( R.id.bivPic, false );
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource( item.msg );
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RelativeLayout rlAudio = helper.setText( R.id.tvDuration, mediaPlayer.getDuration() / 1000 + "''" ).getView( R.id.rlAudio );
        int increment = (int) (UIUtils.getDisplayWidth() / 240 * mediaPlayer.getDuration() / 1000);
        ViewGroup.LayoutParams params = rlAudio.getLayoutParams();
        params.width = UIUtils.dip2Px( 65 ) + UIUtils.dip2Px( increment );
        rlAudio.setLayoutParams( params );


        helper.setOnClickListener( R.id.rlAudio, view -> {
            String path = item.msg;
            final ImageView ivAudio = helper.getView( R.id.ivAudio );
            Uri uri = Uri.parse( path );
            AudioPlayManager.getInstance().startPlay( mContext, uri, new IAudioPlayListener() {
                @Override
                public void onStart(Uri var1) {
                    if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                        AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                        animation.start();
                    }
                }

                @Override
                public void onStop(Uri var1) {
                    if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                        AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                        animation.stop();
                        animation.selectDrawable( 0 );
                    }
                }

                @Override
                public void onComplete(Uri var1) {
                    if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                        AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                        animation.stop();
                        animation.selectDrawable( 0 );
                    }
                }
            } );
        } );
    }

    private void showBivPic(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, false )
                .setVisible( R.id.tvText, false )
                .setVisible( R.id.bivPic, true );
        Bitmap headimg = ImageUtil.getBitmapFromBase64String( item.msg );
        if (headimg != null) {
            helper.setImageBitmap( R.id.bivPic, headimg );
        } else {
            helper.setImageResource( R.id.bivPic, R.mipmap.default_tp );
        }
    }
}
