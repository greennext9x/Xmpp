package ousoftoa.com.xmpp.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioRecordListener;
import com.lqr.emoji.EmotionKeyboard;
import com.lqr.emoji.EmotionLayout;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImageGridActivity;
import com.nanchen.compresshelper.CompressHelper;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.zhy.autolayout.AutoFrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.LocationData;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.presenter.ChatPresenter;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.adapter.ChatAdapter;
import ousoftoa.com.xmpp.ui.view.ChatView;
import ousoftoa.com.xmpp.utils.ImageUtil;
import ousoftoa.com.xmpp.utils.RxUtils;
import ousoftoa.com.xmpp.utils.UIUtils;
import rx.Observable;

import static ousoftoa.com.xmpp.utils.ImageUtil.getBase64StringFromFile;

public class ChatActivity extends BaseActivity<ChatPresenter> implements ChatView {
    public final static int REQUEST_IMAGE_PICKER = 1000;
    public final static int REQUEST_TAKE_PHOTO = 1001;
    public final static int REQUEST_MY_LOCATION = 1002;

    @Bind(R.id.llRoot)
    LinearLayout mLlRoot;
    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.im_right)
    ImageView mImRight;
    @Bind(R.id.llContent)
    LinearLayout mLlContent;
    @Bind(R.id.rvChat)
    RecyclerView mRvChat;
    @Bind(R.id.ivAudio)
    ImageView mIvAudio;
    @Bind(R.id.etContent)
    EditText mEtContent;
    @Bind(R.id.btnAudio)
    Button mBtnAudio;
    @Bind(R.id.ivEmo)
    ImageView mIvEmo;
    @Bind(R.id.ivMore)
    ImageView mIvMore;
    @Bind(R.id.btnSend)
    Button mBtnSend;
    @Bind(R.id.elEmotion)
    EmotionLayout mElEmotion;
    @Bind(R.id.ivAlbum)
    ImageView mIvAlbum;
    @Bind(R.id.ivShot)
    ImageView mIvShot;
    @Bind(R.id.ivLocation)
    ImageView mIvLocation;
    @Bind(R.id.flEmotionView)
    AutoFrameLayout mFlEmotionView;
    @Bind(R.id.llMore)
    LinearLayout mLlMore;
    @Bind(R.id.refreshLayout)
    TwinklingRefreshLayout mRefreshLayout;

    private ChatAdapter mAdapter;
    private List<ChatItem> mData = new ArrayList<>();
    private ChatItem mfriend;
    private boolean mIsFirst = false;
    private EmotionKeyboard mEmotionKeyboard;
    private RxPermissions rxPermissions;
    private String chatname, title;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_chat );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new ChatPresenter( this, this );
    }

    @Override
    public boolean isEventBus() {
        return true;
    }

    @Override
    protected void init() {
        ImagePicker.getInstance().setSelectLimit( 1 );
        rxPermissions = new RxPermissions( this );
        Intent intent = getIntent();
        mfriend = (ChatItem) intent.getSerializableExtra( "chat" );
        if (mfriend.chatType == ChatItem.CHAT) {
            chatname = mfriend.username;
            title = mfriend.nickName;
        } else if (mfriend.chatType == ChatItem.GROUP_CHAT) {
            chatname = mfriend.chatName;
            title = mfriend.chatName;
        }
        initToobar();
        mElEmotion.attachEditText( mEtContent );
        initEmotionKeyboard();
        initRefreshLayout();
        initAudioRecordManager();
        initAdapter();
        initListener();
        XmppConnection.getInstance().setRecevier( chatname, mfriend.chatType );
        mPresenter.getChat( chatname, mfriend.chatType );
    }

    private void initRefreshLayout() {
        mRefreshLayout.setEnableRefresh( true );
        mRefreshLayout.setEnableLoadmore( false );
        mRefreshLayout.setOnRefreshListener( new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                mPresenter.loadMore( mAdapter.getData().size() );
            }
        } );
    }

    private void initAdapter() {
        mRvChat.setLayoutManager( new LinearLayoutManager( this ) );
        mAdapter = new ChatAdapter( mData );
        mRvChat.setAdapter( mAdapter );
    }

    private void initListener() {
        mLlContent.setOnTouchListener( (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    closeBottomAndKeyboard();
                    break;
            }
            return false;
        } );
        mRvChat.setOnTouchListener( (v, event) -> {
            closeBottomAndKeyboard();
            return false;
        } );
        mIvAudio.setOnClickListener( v -> {
            if (mBtnAudio.isShown()) {
                hideAudioButton();
                mEmotionKeyboard.showSoftInput();
            } else {
                rxPermissions.request( Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE )
                        .subscribe( granted -> {
                            if (granted) {
                                mEmotionKeyboard.hideSoftInput();
                                showAudioButton();
                                hideEmotionLayout();
                                hideMoreLayout();
                            } else {
                                showTip( "请您开启录音权限" );
                            }
                        } );
            }
        } );
        mBtnAudio.setOnTouchListener( (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    AudioRecordManager.getInstance( ChatActivity.this ).startRecord();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCancelled( v, event )) {
                        AudioRecordManager.getInstance( ChatActivity.this ).willCancelRecord();
                    } else {
                        AudioRecordManager.getInstance( ChatActivity.this ).continueRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    AudioRecordManager.getInstance( ChatActivity.this ).stopRecord();
                    AudioRecordManager.getInstance( ChatActivity.this ).destroyRecord();
                    break;
            }
            return false;
        } );
        mEtContent.setOnFocusChangeListener( (v, hasFocus) -> {
            if (hasFocus) {
                UIUtils.postTaskDelay( () -> mRvChat.smoothScrollToPosition( mAdapter.getData().size() ), 50 );
            }
        } );
        mEtContent.setOnClickListener( view -> {
            mIvEmo.setImageResource( R.mipmap.ic_cheat_emo );
            UIUtils.postTaskDelay( () -> mRvChat.smoothScrollToPosition( mAdapter.getData().size() ), 50 );
        } );
        RxTextView.textChangeEvents( mEtContent ).subscribe( event -> {
            if (!TextUtils.isEmpty( event.text() )) {
                mBtnSend.setVisibility( View.VISIBLE );
                mIvMore.setVisibility( View.GONE );
            } else {
                mBtnSend.setVisibility( View.GONE );
                mIvMore.setVisibility( View.VISIBLE );
            }
        } );
        //发送消息
        RxView.clicks( mBtnSend )
                .subscribe( aVoid -> {
                    String msg = mEtContent.getText().toString();
                    mEtContent.setText( null );
                    mPresenter.sendMsg( Constants.SEND_TXT, msg );
                } );

        RxView.clicks( mIvAlbum )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .compose( rxPermissions.ensure( Manifest.permission.WRITE_EXTERNAL_STORAGE ) )
                .subscribe( granted -> {
                    if (granted) {
                        Intent intent = new Intent( this, ImageGridActivity.class );
                        startActivityForResult( intent, REQUEST_IMAGE_PICKER );
                    } else {
                        showTip( "请您开启读写权限" );
                    }
                } );
        RxView.clicks( mIvShot )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .compose( rxPermissions.ensure( Manifest.permission.CAMERA
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.RECORD_AUDIO ) )
                .subscribe( granted -> {
                    if (granted) {
                        Intent intent = new Intent( this, TakePhotoActivity.class );
                        startActivityForResult( intent, REQUEST_TAKE_PHOTO );
                    } else {
                        showTip( "请您开启相机权限" );
                    }
                } );
        RxView.clicks( mIvLocation )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .compose( rxPermissions.ensure( Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION ) )
                .subscribe( granted -> {
                    if (granted) {
                        Intent intent = new Intent( this, ChoiceMapActivity.class );
                        startActivityForResult( intent, REQUEST_MY_LOCATION );
                    } else {
                        showTip( "请您开启定位权限" );
                    }
                } );
        mAdapter.setOnItemClickListener( (adapter, view, position) -> {
            closeBottomAndKeyboard();
        } );
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen( location );

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    private void initToobar() {
        mTvTitle.setText( title );
        mImBack.setOnClickListener( view -> {
            if (mElEmotion.isShown() || mLlMore.isShown()) {
                mEmotionKeyboard.interceptBackPress();
                mIvEmo.setImageResource( R.mipmap.ic_cheat_emo );
            } else {
                finish();
            }
        } );
        mImRight.setOnClickListener( view -> showTip( "friend" ) );
    }

    @Override
    public void onNext(List<ChatItem> itemList) {
        mData = itemList;
        mAdapter.setNewData( mData );
        UIUtils.postTaskDelay( () -> mRvChat.smoothScrollToPosition( mAdapter.getData().size() ), 50 );
    }

    @Override
    public void onSendFail() {
        Log.i( "debug", "onSendFail: " );
    }

    @Override
    public void onRefreshNext(List<ChatItem> itemList) {
        mRefreshLayout.finishRefreshing();
        mAdapter.addData( 0, itemList );
        UIUtils.postTaskDelay( () -> mRvChat.smoothScrollToPosition( itemList.size() ), 50 );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notify(MessageEvent event) {
        if (event.getTag().equals( "ChatNewMsg" )) {
            mPresenter.getChat( chatname, mfriend.chatType );
        }
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with( this );
        mEmotionKeyboard.bindToEditText( mEtContent );
        mEmotionKeyboard.bindToContent( mLlContent );
        mEmotionKeyboard.setEmotionLayout( mFlEmotionView );
        mEmotionKeyboard.bindToEmotionButton( mIvEmo, mIvMore );
        mEmotionKeyboard.setOnEmotionButtonOnClickListener( view -> {
            switch (view.getId()) {
                case R.id.ivEmo:
                    UIUtils.postTaskDelay( () -> mRvChat.smoothScrollToPosition( mAdapter.getData().size() ), 50 );
                    mEtContent.clearFocus();
                    if (!mElEmotion.isShown()) {
                        if (mLlMore.isShown()) {
                            showEmotionLayout();
                            hideMoreLayout();
                            hideAudioButton();
                            return true;
                        }
                    } else if (mElEmotion.isShown() && !mLlMore.isShown()) {
                        mIvEmo.setImageResource( R.mipmap.ic_cheat_emo );
                        return false;
                    }
                    showEmotionLayout();
                    hideMoreLayout();
                    hideAudioButton();
                    break;
                case R.id.ivMore:
                    UIUtils.postTaskDelay( () -> mRvChat.smoothScrollToPosition( mAdapter.getData().size() ), 50 );
                    mEtContent.clearFocus();
                    if (!mLlMore.isShown()) {
                        if (mElEmotion.isShown()) {
                            showMoreLayout();
                            hideEmotionLayout();
                            hideAudioButton();
                            return true;
                        }
                    }
                    showMoreLayout();
                    hideEmotionLayout();
                    hideAudioButton();
                    break;
            }
            return false;
        } );
    }

    private void showAudioButton() {
        mBtnAudio.setVisibility( View.VISIBLE );
        mEtContent.setVisibility( View.GONE );
        mIvAudio.setImageResource( R.mipmap.ic_cheat_keyboard );

        if (mFlEmotionView.isShown()) {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.interceptBackPress();
            }
        } else {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.hideSoftInput();
            }
        }
    }

    private void hideAudioButton() {
        mBtnAudio.setVisibility( View.GONE );
        mEtContent.setVisibility( View.VISIBLE );
        mIvAudio.setImageResource( R.mipmap.ic_cheat_voice );
    }

    private void showEmotionLayout() {
        mElEmotion.setVisibility( View.VISIBLE );
        mIvEmo.setImageResource( R.mipmap.ic_cheat_keyboard );
    }

    private void hideEmotionLayout() {
        mElEmotion.setVisibility( View.GONE );
        mIvEmo.setImageResource( R.mipmap.ic_cheat_emo );
    }

    private void showMoreLayout() {
        mLlMore.setVisibility( View.VISIBLE );
    }

    private void hideMoreLayout() {
        mLlMore.setVisibility( View.GONE );
    }

    private void closeBottomAndKeyboard() {
        mEtContent.clearFocus();
        mElEmotion.setVisibility( View.GONE );
        mLlMore.setVisibility( View.GONE );
        if (mEmotionKeyboard != null) {
            mEmotionKeyboard.hideSoftInput();
            mEmotionKeyboard.interceptBackPress();
            mIvEmo.setImageResource( R.mipmap.ic_cheat_emo );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirst) {
            mEtContent.clearFocus();
        } else {
            mIsFirst = false;
        }
    }

    private void initAudioRecordManager() {
        AudioRecordManager.getInstance( this ).setMaxVoiceDuration( 150 );
        File audioDir = new File( Constants.SAVE_SOUND_PATH );
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        AudioRecordManager.getInstance( this ).setAudioSavePath( audioDir.getAbsolutePath() );
        AudioRecordManager.getInstance( this ).setAudioRecordListener( new IAudioRecordListener() {

            private TextView mTimerTV;
            private TextView mStateTV;
            private ImageView mStateIV;
            private PopupWindow mRecordWindow;

            @Override
            public void initTipView() {
                View view = View.inflate( ChatActivity.this, R.layout.popup_audio_wi_vo, null );
                mStateIV = (ImageView) view.findViewById( R.id.rc_audio_state_image );
                mStateTV = (TextView) view.findViewById( R.id.rc_audio_state_text );
                mTimerTV = (TextView) view.findViewById( R.id.rc_audio_timer );
                mRecordWindow = new PopupWindow( view, -1, -1 );
                mRecordWindow.showAtLocation( mLlRoot, 17, 0, 0 );
                mRecordWindow.setFocusable( true );
                mRecordWindow.setOutsideTouchable( false );
                mRecordWindow.setTouchable( false );
            }

            @Override
            public void setTimeoutTipView(int counter) {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility( View.GONE );
                    this.mStateTV.setVisibility( View.VISIBLE );
                    this.mStateTV.setText( R.string.voice_rec );
                    this.mStateTV.setBackgroundResource( R.drawable.bg_voice_popup );
                    this.mTimerTV.setText( String.format( "%s", new Object[]{Integer.valueOf( counter )} ) );
                    this.mTimerTV.setVisibility( View.VISIBLE );
                }
            }

            @Override
            public void setRecordingTipView() {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility( View.VISIBLE );
                    this.mStateIV.setImageResource( R.mipmap.ic_volume_1 );
                    this.mStateTV.setVisibility( View.VISIBLE );
                    this.mStateTV.setText( R.string.voice_rec );
                    this.mStateTV.setBackgroundResource( R.drawable.bg_voice_popup );
                    this.mTimerTV.setVisibility( View.GONE );
                }
            }

            @Override
            public void setAudioShortTipView() {
                if (this.mRecordWindow != null) {
                    mStateIV.setImageResource( R.mipmap.ic_volume_wraning );
                    mStateTV.setText( R.string.voice_short );
                }
            }

            @Override
            public void setCancelTipView() {
                if (this.mRecordWindow != null) {
                    this.mTimerTV.setVisibility( View.GONE );
                    this.mStateIV.setVisibility( View.VISIBLE );
                    this.mStateIV.setImageResource( R.mipmap.ic_volume_cancel );
                    this.mStateTV.setVisibility( View.VISIBLE );
                    this.mStateTV.setText( R.string.voice_cancel );
                    this.mStateTV.setBackgroundResource( R.drawable.corner_voice_style );
                }
            }

            @Override
            public void destroyTipView() {
                if (this.mRecordWindow != null) {
                    this.mRecordWindow.dismiss();
                    this.mRecordWindow = null;
                    this.mStateIV = null;
                    this.mStateTV = null;
                    this.mTimerTV = null;
                }
            }

            @Override
            public void onStartRecord() {

            }

            @Override
            public void onFinish(Uri audioPath, int duration) {
                //发送文件
                File file = new File( audioPath.getPath() );
                if (file.exists()) {
                    String datas = getBase64StringFromFile( audioPath.getPath() );
                    mPresenter.sendMsg( Constants.SEND_SOUND, datas );
                }
            }

            @Override
            public void onAudioDBChanged(int db) {
                switch (db / 5) {
                    case 0:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_1 );
                        break;
                    case 1:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_2 );
                        break;
                    case 2:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_3 );
                        break;
                    case 3:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_4 );
                        break;
                    case 4:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_5 );
                        break;
                    case 5:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_6 );
                        break;
                    case 6:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_7 );
                        break;
                    default:
                        this.mStateIV.setImageResource( R.mipmap.ic_volume_8 );
                }
            }
        } );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mElEmotion.isShown() || mLlMore.isShown()) {
                mEmotionKeyboard.interceptBackPress();
                mIvEmo.setImageResource( R.mipmap.ic_cheat_emo );
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        switch (requestCode) {
            case REQUEST_IMAGE_PICKER:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                    if (data != null) {
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra( ImagePicker.EXTRA_RESULT_ITEMS );
                        if (images != null && images.size() > 0) {
                            Observable.create( (Observable.OnSubscribe<String>) subscriber -> {
                                ImageItem imageItem = images.get( 0 );
                                File file = new File( imageItem.path );
                                File newFile = CompressHelper.getDefault( this ).compressToFile( file );
                                String s = ImageUtil.getBase64StringFromFile( newFile.getPath() );
                                subscriber.onNext( s );
                                subscriber.onCompleted();
                            } ).compose( RxUtils.bindToSchedulers( this ) )
                                    .subscribe( s -> {
                                        mPresenter.sendMsg( Constants.SEND_IMG, s );
                                    } );
                        }
                    }
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra( "path" );
                    if (data.getBooleanExtra( "take_photo", true )) {
                        //照片
                        Observable.just( getBase64StringFromFile( path ) )
                                .compose( RxUtils.bindToSchedulers( this ) )
                                .subscribe( s -> mPresenter.sendMsg( Constants.SEND_IMG, s ) );
                    } else {
                        //小视频
//                        mPresenter.sendFileMsg(new File(path));
                    }
                }
                break;
            case REQUEST_MY_LOCATION:
                if (resultCode == RESULT_OK) {
                    LocationData locationData = (LocationData) data.getSerializableExtra( "location" );
                    mPresenter.sendMsg( Constants.SEND_LOCATION, locationData.toString() );
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NewMsgDbHelper.getInstance( this ).delNewMsg( mfriend.chatName );
        EventBus.getDefault().post( new MessageEvent( "ChatNewMsg", "" ) );
    }
}
