package ousoftoa.com.xmpp.ui.fragment;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.dialog.widget.NormalListDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseFragment;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.presenter.MessagePresenter;
import ousoftoa.com.xmpp.ui.activity.ChatActivity;
import ousoftoa.com.xmpp.ui.adapter.MessageAdapter;
import ousoftoa.com.xmpp.ui.view.MessageView;

/**
 * Created by 韩莫熙 on 2017/4/6.
 */

public class MessageFragment extends BaseFragment<MessagePresenter> implements MessageView{

    @Bind(R.id.rv_message)
    RecyclerView mRvMessage;

    private MessageAdapter mAdapter;
    private List<ChatItem> mData = new ArrayList<>();
    private NormalListDialog dialog;
    private int position;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate( R.layout.fragment_message, null );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new MessagePresenter( mContext,this );
    }

    @Override
    protected void init() {
        initAdapter();
        initDialog();
        initListener();
        mPresenter.getMessge();
    }

    private void initDialog() {
        dialog = new NormalListDialog( mContext,new String[]{"删除该聊天"} );
        dialog.isTitleShow( false );
        dialog.setOnOperItemClickL( (parent, view, position, id) -> {
            MsgDbHelper.getInstance( mContext ).delChatMsg( mData.get( this.position ).chatName );
            dialog.dismiss();
            mPresenter.getMessge();
        } );
    }

    private void initListener() {
        mAdapter.setOnItemClickListener( (adapter, view, position) -> {
            ChatItem item = mData.get( position );
            item.msg  = "";
            Intent intent = new Intent( mContext, ChatActivity.class );
            intent.putExtra( "chat",item );
            startActivity( intent );
        } );
        mAdapter.setOnItemLongClickListener( (adapter, view, position) -> {
            this.position = position;
            dialog.show();
            return false;
        } );
    }

    private void initAdapter() {
        mRvMessage.setLayoutManager( new LinearLayoutManager( mContext ) );
        mAdapter = new MessageAdapter( R.layout.item_message_contant,mData );
        mRvMessage.setAdapter( mAdapter );
    }

    @Override
    public void onNext(List<ChatItem> itemList) {
        mData = itemList;
        mAdapter.setNewData( mData );
    }

    @Override
    public boolean isEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void chatnewmsg(MessageEvent event){
        if (event.getTag().equals( "ChatNewMsg" ))
            mPresenter.getMessge();
    }
}
