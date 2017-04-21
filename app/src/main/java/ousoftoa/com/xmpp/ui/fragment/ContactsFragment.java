package ousoftoa.com.xmpp.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gjiazhe.wavesidebar.WaveSideBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cn.bingoogolapple.badgeview.BGABadgeView;
import immortalz.me.library.TransitionsHeleper;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseFragment;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.bean.MyContacts;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.presenter.ContactsPresenter;
import ousoftoa.com.xmpp.ui.activity.ChatActivity;
import ousoftoa.com.xmpp.ui.activity.FriendInfoActivity;
import ousoftoa.com.xmpp.ui.activity.GroupChatActivity;
import ousoftoa.com.xmpp.ui.activity.NewFriendActivity;
import ousoftoa.com.xmpp.ui.adapter.ContactsAdapter;
import ousoftoa.com.xmpp.ui.view.ContactsView;
import ousoftoa.com.xmpp.utils.PinyinUtils;
import ousoftoa.com.xmpp.utils.UIUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static ousoftoa.com.xmpp.R.id.rvContacts;

/**
 * Created by 韩莫熙 on 2017/4/6.
 */

public class ContactsFragment extends BaseFragment<ContactsPresenter> implements ContactsView {

    @Bind(rvContacts)
    RecyclerView mRvContacts;
    @Bind(R.id.side_bar)
    WaveSideBar mSideBar;
    @Bind(R.id.img_progress)
    ImageView mImgProgress;
    @Bind(R.id.ll_progress_bar)
    LinearLayout mLlProgressBar;

    //列表首尾布局
    private View mHeaderView;
    private BGABadgeView mBgheader;
    private TextView mFooterTv;
    private LinearLayout mLlNewFriend;
    private LinearLayout mLlGroupCheat;

    private AnimationDrawable mAnimationDrawable;
    private ContactsAdapter mAdapter;
    private String headname;

    private List<MyContacts> mData = new ArrayList<>();

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate( R.layout.fragment_contacts, null );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new ContactsPresenter( mContext, this );
    }

    @Override
    protected void init() {
        initAdapter();
        initListener();
        mPresenter.getFriend();
    }

    @Override
    public boolean isEventBus() {
        return true;
    }

    private void initListener() {
        mAdapter.setOnItemChildClickListener( (adapter, view, position) -> {
            switch (view.getId()){
                case R.id.llcontent:
                    Intent chatintent = new Intent( mContext, ChatActivity.class );
                    chatintent.putExtra( "chat",mData.get(position).t );
                    startActivity( new Intent( chatintent ) );
                    break;
                case R.id.ivHeader:
                    Intent headintent = new Intent( mContext, FriendInfoActivity.class );
                    headintent.putExtra( "friendinfo",mData.get(position).t );
                    TransitionsHeleper.startActivity( (Activity) mContext,headintent,view,mData.get( position ).t.getUserHead());
                    break;
            }
        } );
        mLlNewFriend.setOnClickListener( view -> startActivity( new Intent( mContext,NewFriendActivity.class ) ) );
        mLlGroupCheat.setOnClickListener( view -> startActivity( new Intent( mContext,GroupChatActivity.class ) ) );
        mBgheader.setDragDismissDelegage( badgeable -> NewMsgDbHelper.getInstance(mContext).delNewMsg( ""+0 ));
    }

    private void initSideBar() {
        mSideBar.setOnSelectIndexItemListener( index -> {
            for (int i=0; i < mData.size(); i++) {
                if (mData.get(i).isHeader && mData.get(i).header.equals(index)) {
                    ((LinearLayoutManager) mRvContacts.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                    return;
                }
            }
        } );
    }

    private void initAdapter() {
        mRvContacts.setLayoutManager( new LinearLayoutManager( mContext ) );
        mAdapter = new ContactsAdapter( R.layout.item_contacts_content, R.layout.item_contacts_head, mData );
        mRvContacts.setAdapter( mAdapter );
        mHeaderView = View.inflate(getActivity(), R.layout.header_contacts_rv, null);
        mLlNewFriend = (LinearLayout) mHeaderView.findViewById(R.id.llNewFriend);
        mLlGroupCheat = (LinearLayout) mHeaderView.findViewById(R.id.llGroupCheat);
        mBgheader = (BGABadgeView) mHeaderView.findViewById( R.id.bVheadview );
        mAdapter.addHeaderView(mHeaderView);

        mFooterTv = new TextView(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2Px(50));
        mFooterTv.setLayoutParams(params);
        mFooterTv.setGravity( Gravity.CENTER);
        mAdapter.addFooterView(mFooterTv);

    }

    @Override
    public void showLoadingDialog() {
        // 加载动画
        mLlProgressBar.setVisibility( View.VISIBLE );
        mAnimationDrawable = (AnimationDrawable) mImgProgress.getDrawable();
        mAnimationDrawable.start();
    }

    @Override
    public void closeLoadingDialog() {
        mLlProgressBar.setVisibility( View.GONE );
        mAnimationDrawable.stop();
    }

    @Override
    public void onNext(List<Friend> friendList) {
        headname = "";
        mData.clear();
        Observable.from( friendList )
                .map( friend -> {
                    if (!headname.equals( PinyinUtils.getPingYin( friend.getNickname() ).substring( 0, 1 ).toUpperCase() ) ){
                        headname = PinyinUtils.getPingYin( friend.getNickname() ).substring( 0, 1 ).toUpperCase();
                        mData.add( new MyContacts( true, headname ) );
                    }
                    mData.add( new MyContacts( friend ) );
                    return friend;
                } )
                .toList()
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( list -> {
                    mAdapter.setNewData( mData );
                    initSideBar();
                    mFooterTv.setText(friendList.size()+"个联系人");
                },throwable -> showTip( throwable.getMessage() ));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changefriend(MessageEvent event){
        if (event.getTag().equals( "friendchange" ))
            mPresenter.changeFriend();
        else if (event.getTag().equals( "ChatNewMsg" )){
            int count = NewMsgDbHelper.getInstance(mContext).getMsgCount(""+0);
            if (count > 0){
                mBgheader.showTextBadge( "" );
            }else {
                mBgheader.hiddenBadge();
            }
        }
    }
}
