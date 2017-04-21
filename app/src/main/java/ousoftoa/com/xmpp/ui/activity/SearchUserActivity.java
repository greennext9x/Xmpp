package ousoftoa.com.xmpp.ui.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.presenter.SearchUserPersenter;
import ousoftoa.com.xmpp.ui.adapter.SearchUserAdapter;
import ousoftoa.com.xmpp.ui.view.SearchUserView;
import rx.android.schedulers.AndroidSchedulers;

public class SearchUserActivity extends BaseActivity<SearchUserPersenter> implements SearchUserView {

    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.etSearch)
    EditText mEtSearch;
    @Bind(R.id.rlNoResultTip)
    RelativeLayout mRlNoResultTip;
    @Bind(R.id.tvMsg)
    TextView mTvMsg;
    @Bind(R.id.llSearch)
    LinearLayout mLlSearch;
    @Bind(R.id.rvSearch)
    RecyclerView mRvSearch;

    private SearchUserAdapter mAdapter;
    private List<Friend> mData = new ArrayList<>();
    private View headview;
    private TextView headviewTV;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_search_user );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new SearchUserPersenter( this, this );
    }

    @Override
    protected void init() {
        initToolbar();
        initAdapter();
        initListener();
    }

    private void initAdapter() {
        mRvSearch.setLayoutManager( new LinearLayoutManager( this ) );
        mAdapter = new SearchUserAdapter( R.layout.item_contacts_content, mData );
        mRvSearch.setAdapter( mAdapter );
        headview = getLayoutInflater().inflate( R.layout.item_searchuser_head, (ViewGroup) mRvSearch.getParent(), false );
        headviewTV = (TextView) headview.findViewById( R.id.tv_search_head );
        mAdapter.addHeaderView( headview );
    }

    private void initListener() {
        RxView.clicks( mLlSearch )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    mPresenter.getUser( mEtSearch.getText().toString() );
                } );
    }

    private void initToolbar() {
        RxView.clicks( mImBack )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> finish() );
        RxTextView.textChangeEvents( mEtSearch )
                .debounce( 400, TimeUnit.MILLISECONDS )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( event -> {
                    if (!TextUtils.isEmpty( event.text() )){
                        showSearchlayout();
                        mTvMsg.setText( event.text() );
                    }else {
                        showNotextlayout();
                    }
                } );
    }

    @Override
    public void onNext(List<Friend> mlist) {
        if (mlist.size() == 0) {
            showEmtylayout();
        } else {
            showRvlayout();
            mData = mlist;
            mAdapter.setNewData( mData );
            headviewTV.setText( "搜索到以下" + mlist.size() + "个用户" );
        }
    }

    public void showRvlayout() {
        mLlSearch.setVisibility( View.GONE );
        mRlNoResultTip.setVisibility( View.GONE );
        mRvSearch.setVisibility( View.VISIBLE );
    }

    public void showEmtylayout() {
        mLlSearch.setVisibility( View.GONE );
        mRlNoResultTip.setVisibility( View.VISIBLE );
        mRvSearch.setVisibility( View.GONE );
    }

    public void showSearchlayout() {
        mLlSearch.setVisibility( View.VISIBLE );
        mRlNoResultTip.setVisibility( View.GONE );
        mRvSearch.setVisibility( View.GONE );
    }

    public void showNotextlayout(){
        mLlSearch.setVisibility( View.GONE );
        mRlNoResultTip.setVisibility( View.GONE );
        mRvSearch.setVisibility( View.GONE );
    }
}
