package com.example.zhigangsong.maptest;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.GroundOverlay;
import com.amap.api.maps2d.model.GroundOverlayOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {



    private Marker mLastMarker;
    CameraPosition mCameraPosition;
    private float mZoomLevel = 5;
    public AMapLocationClient mLocationClient = null;
    GeocodeSearch mGeocodeSearch;
    GroundOverlay mLastGroundOverlay;
    PlayHandler mPlayHandler = new PlayHandler(this);
    RadarPlayer mPlayer;
    Button mControlBtn;


    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            mCameraPosition = CameraPosition.fromLatLngZoom(latLng, mZoomLevel);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(mCameraPosition);
            mAMap.moveCamera(update);
            updateMarker(latLng);
        }
    };


    private void notifyPlayerPositionChanged() {
        CameraPosition position = mAMap.getCameraPosition();
        mPlayer.changPosition(position.target, position.zoom);
    }

    private static final String TAG = "huli";
    MapView mMapView;
    AMap mAMap;

    UiSettings mUiSettings;
    TextView mTimeLine;
    TextView mLoc;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mMapView.onCreate(savedInstanceState);
        mPlayer = new RadarPlayer(mPlayHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        requestLocation();
        if (null != mPlayer) {
            mPlayer.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mPlayer.destory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.onDestroy();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void updateMarker(LatLng latLng) {
        removeMarkers();
        LatLonPoint point = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 0, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
        mLastMarker = mAMap.addMarker(new MarkerOptions().position(latLng).icon(
                BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        notifyPlayerPositionChanged();
    }


    private void initView() {
        mMapView = (MapView) findViewById(R.id.map_view);
        mLoc = (TextView) findViewById(R.id.loc_info);
        mControlBtn = (Button) findViewById(R.id.play);
        mGeocodeSearch = new GeocodeSearch(this);
        mGeocodeSearch.setOnGeocodeSearchListener(this);
        mImageView = (ImageView) findViewById(R.id.img);
        mTimeLine = (TextView) findViewById(R.id.time_line);
        initMap();
    }

    private void initMap() {
        mAMap = mMapView.getMap();
        mUiSettings = mAMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mAMap.setOnMapClickListener(this);
        mAMap.setOnCameraChangeListener(this);
    }

    private void requestLocation() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(true);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.startLocation();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        updateMarker(latLng);
    }

    private void removeMarkers() {
        if (null != mLastMarker) {
            mLastMarker.remove();
        }
    }

    public void getMyLoc(View view) {
        requestLocation();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        mZoomLevel = cameraPosition.zoom;
        notifyPlayerPositionChanged();
        Log.d(TAG,mAMap.getProjection().getVisibleRegion().latLngBounds.toString());
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        List<PoiItem> poiItems = regeocodeResult.getRegeocodeAddress().getPois();
        StringBuilder loc = new StringBuilder();
        loc.append(regeocodeResult.getRegeocodeAddress().getDistrict()).append(" ");
        if (!poiItems.isEmpty()) {
            loc.append(poiItems.get(0).getTitle());
        } else {
            loc.append(regeocodeResult.getRegeocodeAddress().getFormatAddress());
        }
        Log.d(TAG,loc.toString());
        mLoc.setText(loc);
    }

    private void playWav(RadarImageEntity image) {
        if (mLastGroundOverlay != null) {
            mLastGroundOverlay.remove();
        }
        mLastGroundOverlay = mAMap.addGroundOverlay(new GroundOverlayOptions()
                .anchor(0.5f, 0.5f).transparency(0.1f)
                .image(BitmapDescriptorFactory.fromPath(image.getCachePath()))
                .positionFromBounds(image.getArea()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) (image.getTime() * 1000));
        mTimeLine.setText(calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE));
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    public void user(View view) {
        Toast.makeText(this, "用户反馈", Toast.LENGTH_SHORT).show();
        mPlayer.stop();
    }

    public void radio(View view) {
        Toast.makeText(this, "雷达", Toast.LENGTH_SHORT).show();
        mPlayer.start();
    }

    public void pause(View view) {
        if (null == mPlayer) {
            return;
        }
        if (mPlayer.isPlay()) {
            mPlayer.pause();
            mControlBtn.setText("start");
        } else {
            mPlayer.resume();
            mControlBtn.setText("pause");
        }
    }

    static class PlayHandler extends NoLeakHandler<MainActivity> {
        public PlayHandler(MainActivity outClass) {
            super(outClass);
        }

        @Override
        public void handleMessage(Message msg, MainActivity mainActivity) {
            if (null != mainActivity) {
                switch (msg.what) {
                    case RadarPlayer.MSG_PLAY:
                        RadarImageEntity image = (RadarImageEntity) msg.obj;
                        mainActivity.playWav(image);
                        break;
                    case RadarPlayer.MSG_STOP:
                        mainActivity.clearRadioImg();
                        break;
                }
            }
        }
    }

    private void clearRadioImg() {
        if (mLastGroundOverlay != null) {
            mLastGroundOverlay.setVisible(false);
            mLastGroundOverlay.remove();
        }
    }

}
