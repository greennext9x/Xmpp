package ousoftoa.com.xmpp.ui.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseFragment;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.presenter.MessagePresenter;
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
        mPresenter.getMessge();
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
