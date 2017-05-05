package ousoftoa.com.xmpp.ui.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.IntentData;
import ousoftoa.com.xmpp.presenter.MainPresenter;
import ousoftoa.com.xmpp.ui.adapter.MyPagerAdapter;
import ousoftoa.com.xmpp.ui.adapter.TabEntity;
import ousoftoa.com.xmpp.ui.fragment.ContactsFragment;
import ousoftoa.com.xmpp.ui.fragment.FriendsFragment;
import ousoftoa.com.xmpp.ui.fragment.MeFragment;
import ousoftoa.com.xmpp.ui.fragment.MessageFragment;
import ousoftoa.com.xmpp.ui.view.MainView;
import ousoftoa.com.xmpp.utils.DataHelper;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {
    @Bind(R.id.vp)
    ViewPager mVp;
    @Bind(R.id.tl_common)
    CommonTabLayout mTlCommon;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.probar_loading)
    ProgressBar mProbarLoading;
    @Bind(R.id.tv_title)
    TextView mTvTitle;

    private List<Fragment> mFragmentList = new ArrayList<>();
    private String[] mTitles = {"消息", "联系人", "朋友圈", "我"};
    private int[] mIconUnselectIds = {R.mipmap.aio, R.mipmap.aim, R.mipmap.friend, R.mipmap.wo};
    private int[] mIconSelectIds = {R.mipmap.aio1, R.mipmap.aim1, R.mipmap.friend1, R.mipmap.wo1};
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private MyPagerAdapter mPagerAdapter;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_main );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new MainPresenter( this,this );
    }

    @Override
    public boolean supportSlideBack() {
        return false;
    }

    @Override
    protected void init() {
        initToIntent();
        initToolbar();
        initCommonTablayout();
        initLogin();
    }

    private void initToIntent() {
        if (!DataHelper.getBooleanSF( this, Constants.LOGIN_CHECK )) {
            startActivity( new Intent( this, LoginActivity.class ) );
            finish();
        }
        Constants.USER_NAME = DataHelper.getStringSF( this,Constants.LOGIN_ACCOUNT );
        IntentData intentData = (IntentData) getIntent().getSerializableExtra( "noti" );
        if (intentData != null){
            if (intentData.getType() == ChatItem.CHAT || intentData.getType() == ChatItem.GROUP_CHAT){
                ChatItem friend = new ChatItem();
                friend.setNickName( intentData.getNickname() );
                friend.setChatName( intentData.getChatname() );
                Intent intent = new Intent( this,ChatActivity.class );
                intent.putExtra( "chat", friend );
                startActivity( intent );
            }else if (intentData.getType() == ChatItem.FRIEND){
                startActivity( new Intent( this,NewFriendActivity.class ) );
            }
        }
    }

    private void initToolbar() {
        mToolbar.inflateMenu( R.menu.menu_basetoolbar );
        mToolbar.setOnMenuItemClickListener( item -> {
            switch (item.getItemId()){
                case R.id.action_more:
                    startActivity( new Intent( this,SearchUserActivity.class ) );
                    break;
            }
            return true;
        } );
    }

    private void initLogin() {
        if (!Constants.USER_MODE.equals( "available" )) {
            mPresenter.toLogin();
        }
    }

    @Override
    public void showLoadingDialog() {
        mProbarLoading.setVisibility( View.VISIBLE );
        mTvTitle.setText( R.string.loading );
    }

    @Override
    public void closeLoadingDialog() {
        mProbarLoading.setVisibility( View.GONE );
        mTvTitle.setText( R.string.app_name );
    }

    private void initCommonTablayout() {
        mFragmentList.add( new MessageFragment() );
        mFragmentList.add( new ContactsFragment() );
        mFragmentList.add( new FriendsFragment() );
        mFragmentList.add( new MeFragment() );
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add( new TabEntity( mTitles[i], mIconSelectIds[i], mIconUnselectIds[i] ) );
        }
        mPagerAdapter = new MyPagerAdapter( getSupportFragmentManager(), mFragmentList, mTitles );
        mVp.setAdapter( mPagerAdapter );
        mVp.setOffscreenPageLimit( 3 );
        mTlCommon.setTabData( mTabEntities );
        mTlCommon.setOnTabSelectListener( new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mVp.setCurrentItem( position );
            }

            @Override
            public void onTabReselect(int position) {

            }
        } );
        mVp.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTlCommon.setCurrentTab( position );
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        } );
    }
}
