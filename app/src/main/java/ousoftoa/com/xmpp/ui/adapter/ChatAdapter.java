package ousoftoa.com.xmpp.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.emoji.MoonUtils;

import java.util.List;

import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.LocationData;
import ousoftoa.com.xmpp.model.bean.SoundData;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.utils.DateUtil;
import ousoftoa.com.xmpp.utils.ImageUtil;
import ousoftoa.com.xmpp.utils.JsonUtil;
import ousoftoa.com.xmpp.utils.UIUtils;
import ousoftoa.com.xmpp.wight.bubble.BubbleImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by 韩莫熙 on 2017/4/20.
 */

public class ChatAdapter extends BaseMultiItemQuickAdapter<ChatItem, BaseViewHolder> {
    private int position = -1;
    private boolean isPlaying = false;

    public ChatAdapter(List<ChatItem> data) {
        super( data );
        addItemType( ChatItem.CHAT_IN, R.layout.item_chat_in );
        addItemType( ChatItem.CHAT_OUT, R.layout.item_chat_out );
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatItem item) {
        if (item.inOrOut == 0)
            setHead( helper, item.getUsername() );
        else if (item.inOrOut == 1)
            setHead( helper, null );
        setTime( helper, item, helper.getLayoutPosition() );
        setName( helper, item );
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

    private void setHead(BaseViewHolder helper, String username) {
        XmppConnection.getInstance().getUserImage( username )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( bitmap -> {
                    if (bitmap != null)
                        helper.setImageBitmap( R.id.ivAvatar, bitmap );
                } );
    }

    private void setMsg(BaseViewHolder helper, ChatItem item) {
        if (item.subject == null || item.subject.equals( Constants.SEND_TXT )) {
            showTxtMsg( helper, item );
        } else if (item.subject.equals( Constants.SEND_IMG )) {
            showBivPic( helper, item );
        } else if (item.subject.equals( Constants.SEND_SOUND )) {
            showSound( helper, item );
        } else if (item.subject.equals( Constants.SEND_LOCATION )) {
            showLocation( helper, item );
        }
    }

    private void showTxtMsg(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, false )
                .setVisible( R.id.tvText, true )
                .setVisible( R.id.llLocation, false )
                .setVisible( R.id.bivPic, false );
        TextView tvContent = helper.getView( R.id.tvText );
        tvContent.setText( item.msg );
        MoonUtils.identifyFaceExpression( mContext, tvContent, item.msg, ImageSpan.ALIGN_BOTTOM );
    }

    private void showLocation(BaseViewHolder helper, ChatItem item) {
        LocationData mData = JsonUtil.jsonToObject( item.msg, LocationData.class );
        helper.setVisible( R.id.llSound, false )
                .setVisible( R.id.tvText, false )
                .setVisible( R.id.bivPic, false )
                .setVisible( R.id.llLocation, true )
                .setText( R.id.tvTitle, mData.getPoi() );
        Glide.with( mContext ).load( mData.getImgUrl() ).into( (ImageView) helper.getView( R.id.ivLocation ) );
    }

    private void showSound(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, true )
                .setVisible( R.id.tvText, false )
                .setVisible( R.id.llLocation, false )
                .setVisible( R.id.bivPic, false );
        SoundData soundData = JsonUtil.jsonToObject( item.msg, SoundData.class );
        LinearLayout rlAudio = helper.setText( R.id.tvDuration, soundData.getDuration() + "''" ).getView( R.id.rlAudio );
        int increment = (int) (UIUtils.getDisplayWidth() / 300 * soundData.getDuration());
        ViewGroup.LayoutParams params = rlAudio.getLayoutParams();
        params.width = UIUtils.dip2Px( 30 ) + UIUtils.dip2Px( increment );
        rlAudio.setLayoutParams( params );

        helper.setOnClickListener( R.id.rlAudio, view -> {
            String path = soundData.getPathname();
            final ImageView ivAudio = helper.getView( R.id.ivAudio );
            Uri uri = Uri.parse( path );
            if (helper.getLayoutPosition() != position){
                position = helper.getLayoutPosition();
                playSound( uri,ivAudio );
            }else {
                if (isPlaying)
                    AudioPlayManager.getInstance().stopPlay();
                else
                    playSound( uri,ivAudio );
            }
        } );
    }

    private void showBivPic(BaseViewHolder helper, ChatItem item) {
        helper.setVisible( R.id.llSound, false )
                .setVisible( R.id.tvText, false )
                .setVisible( R.id.llLocation, false )
                .setVisible( R.id.bivPic, true );
        BubbleImageView imageView = helper.getView( R.id.bivPic );
        Observable.create( (Observable.OnSubscribe<Bitmap>) subscriber -> {
            Bitmap headimg = ImageUtil.getBitmapFromBase64String( item.msg );
            if (headimg != null) {
                subscriber.onNext( headimg );
                subscriber.onCompleted();
            } else {
                subscriber.onError( new Throwable( "没有头像" ) );
            }
        } ).subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( bitmap -> imageView.setImageBitmap( bitmap )
                        , throwable -> imageView.setImageResource( R.mipmap.default_tp ) );
    }

    private void playSound(Uri uri, View view){
        AudioPlayManager.getInstance().startPlay( mContext, uri, new IAudioPlayListener() {
            @Override
            public void onStart(Uri var1) {
                if (view != null && view.getBackground() instanceof AnimationDrawable) {
                    AnimationDrawable animation = (AnimationDrawable) view.getBackground();
                    animation.start();
                    isPlaying = true;
                }
            }

            @Override
            public void onStop(Uri var1) {
                if (view != null && view.getBackground() instanceof AnimationDrawable) {
                    AnimationDrawable animation = (AnimationDrawable) view.getBackground();
                    animation.stop();
                    animation.selectDrawable( 0 );
                    isPlaying = false;
                }
            }

            @Override
            public void onComplete(Uri var1) {
                if (view != null && view.getBackground() instanceof AnimationDrawable) {
                    AnimationDrawable animation = (AnimationDrawable) view.getBackground();
                    animation.stop();
                    animation.selectDrawable( 0 );
                }
            }
        } );
    }
}
