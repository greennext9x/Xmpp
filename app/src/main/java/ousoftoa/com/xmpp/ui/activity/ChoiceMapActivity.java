package ousoftoa.com.xmpp.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jakewharton.rxbinding.view.RxView;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.Circle;
import com.tencent.mapsdk.raster.model.CircleOptions;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.LocationData;
import ousoftoa.com.xmpp.ui.adapter.ChoiceMapAdapter;

public class ChoiceMapActivity extends BaseActivity implements TencentLocationListener, SensorEventListener {

    @Bind(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @Bind(R.id.map)
    MapView mMap;
    @Bind(R.id.ibShowLocation)
    ImageButton mIbShowLocation;
    @Bind(R.id.rvPOI)
    RecyclerView mRvPOI;
    @Bind(R.id.pb)
    ProgressBar mPb;
    @Bind(R.id.im_back)
    ImageView mImBack;

    private SensorManager mSensorManager;
    private Sensor mOritationSensor;
    private TencentLocationManager mLocationManager;
    private TencentLocationRequest mLocationRequest;
    private TencentMap mTencentMap;
    private Marker myLocation;
    private Circle accuracy;
    private TencentSearch mTencentSearch;
    private List<Geo2AddressResultObject.ReverseAddressResult.Poi> mData = new ArrayList<>();
    private int mSelectedPosi = 0;
    private ChoiceMapAdapter mAdapter;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_choice_map );
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    protected void init() {
        initAdapter();
        mBtnToolbarSend.setVisibility( View.VISIBLE );
        mSensorManager = (SensorManager) getSystemService( SENSOR_SERVICE );
        mOritationSensor = mSensorManager.getDefaultSensor( Sensor.TYPE_ORIENTATION );
        mLocationManager = TencentLocationManager.getInstance( this );
        mLocationRequest = TencentLocationRequest.create();
        mTencentMap = mMap.getMap();
        mTencentSearch = new TencentSearch( this );
        requestLocationUpdate();
        initListener();
    }

    private void initAdapter() {
        mRvPOI.setLayoutManager( new LinearLayoutManager( mContext ) );
        mAdapter = new ChoiceMapAdapter( mData );
        mRvPOI.setAdapter( mAdapter );
    }

    private void initListener() {
        RxView.clicks( mImBack )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> finish() );
        mAdapter.setOnItemClickListener( (adapter, view, position) -> {
            mSelectedPosi = position;
            mAdapter.setSelectedPosi( mSelectedPosi );
        } );
        mBtnToolbarSend.setOnClickListener( v -> {
            if (mData != null && mData.size() > mSelectedPosi) {
                Geo2AddressResultObject.ReverseAddressResult.Poi poi = mData.get( mSelectedPosi );
                Intent data = new Intent();
                LocationData locationData = new LocationData();
                locationData.setLat( String.valueOf( poi.location.lat ) );
                locationData.setLng( String.valueOf( poi.location.lng ) );
                locationData.setPoi( poi.title );
                locationData.setImgUrl( getMapUrl( poi.location.lat, poi.location.lng ) );
                data.putExtra( "location", locationData );
                this.setResult( Activity.RESULT_OK, data );
                finish();
            }
        } );
        mIbShowLocation.setOnClickListener( v -> requestLocationUpdate() );
        mTencentMap.setOnMapCameraChangeListener( new TencentMap.OnMapCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (myLocation != null)
                    myLocation.setPosition( mTencentMap.getMapCenter() );
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (accuracy != null) {
                    accuracy.setCenter( mTencentMap.getMapCenter() );
                }
                search( mTencentMap.getMapCenter() );
            }
        } );
    }

    private void requestLocationUpdate() {
        //开启定位
        int error = mLocationManager.requestLocationUpdates( mLocationRequest, this );
        switch (error) {
            case 0:
                Log.i( "debug", "requestLocationUpdate: " + "成功注册监听器" );
                break;
            case 1:
                Log.i( "debug", "requestLocationUpdate: " + "设备缺少使用腾讯定位服务需要的基本条件" );
                break;
            case 2:
                Log.i( "debug", "requestLocationUpdate: " + "manifest 中配置的 key 不正确" );
                break;
            case 3:
                Log.i( "debug", "requestLocationUpdate: " + "自动加载libtencentloc.so失败" );
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void search(LatLng latLng) {
        mPb.setVisibility( View.VISIBLE );
        mRvPOI.setVisibility( View.GONE );
        Location location = new Location().lat( (float) latLng.getLatitude() ).lng( (float) latLng.getLongitude() );
        //还可以传入其他坐标系的坐标，不过需要用coord_type()指明所用类型
        //这里设置返回周边poi列表，可以在一定程度上满足用户获取指定坐标周边poi的需求
        Geo2AddressParam geo2AddressParam = new Geo2AddressParam().
                location( location ).get_poi( true );
        mTencentSearch.geo2address( geo2AddressParam, new HttpResponseListener() {

            @Override
            public void onSuccess(int arg0, BaseObject arg1) {
                mPb.setVisibility( View.GONE );
                mRvPOI.setVisibility( View.VISIBLE );
                if (arg1 == null) {
                    return;
                }
                mData = ((Geo2AddressResultObject) arg1).result.pois;
                mAdapter.setNewData( mData );
            }

            @Override
            public void onFailure(int arg0, String arg1, Throwable arg2) {
                mPb.setVisibility( View.GONE );
                mRvPOI.setVisibility( View.VISIBLE );
            }
        } );
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (i == tencentLocation.ERROR_OK) {
            LatLng latLng = new LatLng( tencentLocation.getLatitude(), tencentLocation.getLongitude() );
            if (myLocation == null) {
                myLocation = mTencentMap.addMarker( new MarkerOptions().position( latLng ).icon( BitmapDescriptorFactory.fromResource( R.mipmap.arm ) ).anchor( 0.5f, 0.8f ) );
            }
            if (accuracy == null) {
                accuracy = mTencentMap.addCircle( new CircleOptions().center( latLng ).radius( tencentLocation.getAccuracy() ).fillColor( 0x440000ff ).strokeWidth( 0f ) );
            }
            myLocation.setPosition( latLng );
            accuracy.setCenter( latLng );
            accuracy.setRadius( tencentLocation.getAccuracy() );
            mTencentMap.animateTo( latLng );
            mTencentMap.setZoom( 16 );
            search( latLng );
            //取消定位
            mLocationManager.removeUpdates( this );
        } else {
            Log.i( "debug", "onLocationChanged: " + i );
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        String desc = "";
        switch (i) {
            case STATUS_DENIED:
                desc = "权限被禁止";
                break;
            case STATUS_DISABLED:
                desc = "模块关闭";
                break;
            case STATUS_ENABLED:
                desc = "模块开启";
                break;
            case STATUS_GPS_AVAILABLE:
                desc = "GPS可用，代表GPS开关打开，且搜星定位成功";
                break;
            case STATUS_GPS_UNAVAILABLE:
                desc = "GPS不可用，可能 gps 权限被禁止或无法成功搜星";
                break;
            case STATUS_LOCATION_SWITCH_OFF:
                desc = "位置信息开关关闭，在android M系统中，此时禁止进行wifi扫描";
                break;
            case STATUS_UNKNOWN:
                break;
        }
        Log.i( "debug", "onStatusUpdate: " + s + ", " + s1 + " " + desc );
    }

    private String getMapUrl(double x, double y) {
        String url = "http://st.map.qq.com/api?size=708*270&center=" + y + "," + x + "&zoom=17&referer=xmpp";
        return url;
    }
}
