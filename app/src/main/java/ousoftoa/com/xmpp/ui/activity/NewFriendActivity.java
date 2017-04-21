package ousoftoa.com.xmpp.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import immortalz.me.library.TransitionsHeleper;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.dao.NewFriendDbHelper;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.presenter.NewFriendPresenter;
import ousoftoa.com.xmpp.ui.adapter.NewFriendAdapter;
import ousoftoa.com.xmpp.ui.view.NewFriendView;

public class NewFriendActivity extends BaseActivity<NewFriendPresenter> implements NewFriendView{
    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.tv_right)
    TextView mTvRight;
    @Bind(R.id.etContent)
    EditText mEtContent;
    @Bind(R.id.rvNewfriend)
    RecyclerView mRvNewfriend;

    private List<Friend> mData = new ArrayList<>(  );
    private NewFriendAdapter mAdapter;
    private View emtyView;
    private int position = 0;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_new_friend );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new NewFriendPresenter( this,this );
    }

    @Override
    protected void init() {
        initToobar();
        initEmtyView();
        initAdapter();
        initListener();
        mPresenter.getNewFriend();
    }

    private void initEmtyView() {
        emtyView = getLayoutInflater().inflate( R.layout.item_emtyview,(ViewGroup)mRvNewfriend.getParent(),false );
        TextView emtyTv = (TextView) emtyView.findViewById( R.id.tv_emty );
        emtyTv.setText( "暂时无好友申请信息" );
    }

    private void initListener() {
        RxView.clicks( mTvRight )
                .throttleFirst( 1,TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    NewFriendDbHelper.getInstance(getApplicationContext()).clear();
                    mAdapter.setNewData( null );
                    mAdapter.setEmptyView( emtyView );
                } );
        RxView.clicks( mEtContent )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> startActivity( new Intent( this, SearchUserActivity.class ) ) );
        mAdapter.setOnItemChildClickListener( (adapter, view, position) -> {
                switch (view.getId()){
                    case R.id.ivHeader:
                        Intent intent = new Intent( this,FriendInfoActivity.class );
                        intent.putExtra( "friendinfo",mData.get( position ));
                        TransitionsHeleper.startActivity( (Activity) mContext,intent,view,mData.get( position ).getUserHead());
                        break;
                    case R.id.btnAck:
                        this.position = position;
                        mPresenter.addFriend( mData.get( position ).getUsername() );
                }
        } );
    }

    private void initAdapter() {
        mRvNewfriend.setHasFixedSize(true);
        mRvNewfriend.setLayoutManager( new LinearLayoutManager( this ) );
        mAdapter = new NewFriendAdapter( R.layout.item_newfriend,mData );
        mRvNewfriend.setAdapter( mAdapter );

        View headview = getLayoutInflater().inflate( R.layout.item_searchuser_head,(ViewGroup)mRvNewfriend.getParent(),false );
        TextView headviewTV = (TextView) headview.findViewById( R.id.tv_search_head );
        headviewTV.setText( "新的朋友" );
        mAdapter.addHeaderView( headview );
    }

    private void initToobar() {
        RxView.clicks( mImBack )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> finish() );
    }

    @Override
    public void onNext(List<Friend> mlist) {
        mData = mlist;
        if (mData.size() == 0){
            mAdapter.setNewData( null );
            mAdapter.setEmptyView( emtyView );
        } else {
            mAdapter.setNewData( mData );
        }
    }

    @Override
    public void onAddNext() {
        EventBus.getDefault().post( new MessageEvent( "friendchange","" ) );
        NewFriendDbHelper.getInstance(this).delFriend(mData.get( position ).getUsername());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NewMsgDbHelper.getInstance(this).delNewMsg(""+0);
        EventBus.getDefault().post( new MessageEvent( "ChatNewMsg","" ) );
    }
}
