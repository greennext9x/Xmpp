package ousoftoa.com.xmpp.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import immortalz.me.library.TransitionsHeleper;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.ui.activity.FriendInfoActivity;
import ousoftoa.com.xmpp.utils.ImageUtil;

/**
 * Created by 韩莫熙 on 2017/4/12.
 */

public class SearchUserAdapter extends BaseQuickAdapter<Friend,BaseViewHolder>{


    public SearchUserAdapter(int layoutResId, List<Friend> data) {
        super( layoutResId, data );
    }

    @Override
    protected void convert(BaseViewHolder helper, Friend item) {
        helper.setText( R.id.tvName,item.getNickname());
        Bitmap headimg = ImageUtil.getBitmapFromBase64String(item.getUserHead());
        if (headimg != null){
            helper.setImageBitmap(R.id.ivHeader,headimg);
        }else {
            helper.setImageResource(R.id.ivHeader,R.mipmap.default_tp);
        }
        helper.setOnClickListener( R.id.llcontent, view -> {
            Intent intent = new Intent( mContext, FriendInfoActivity.class );
            intent.putExtra( "friendinfo", item );
            TransitionsHeleper.startActivity( (Activity) mContext,intent,helper.getView( R.id.ivHeader ),item.getUserHead());
        } );
    }
}
