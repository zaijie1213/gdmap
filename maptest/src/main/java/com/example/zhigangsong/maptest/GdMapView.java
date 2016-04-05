package com.example.zhigangsong.maptest;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

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

/**
 * Created by zhigang.song on 2016/4/5.
 */
public class GdMapView extends FrameLayout implements View.OnClickListener, AMap.OnMapClickListener, AMap.OnCameraChangeListener {
    private static final String TAG = "huli";
    private MapView mMapView;
    private Button mBtnPlayControl;
    private AMap mMap;
    private TextView mTimeLine;

    AMapLocationListener mLocationListener;
    private Context ctx;
    private CameraPosition mCameraPosition;
    private float mZoomLevel = 5;
    private Marker mLastMarker;
    private AMapLocationClient mLocationClient;
    private GeocodeSearch mGeocodeSearch;
    private RadarPlayer mPlayer;
    private GroundOverlay mLastGroundOverlay;


    public GdMapView(Context context) {
        this(context, null);
    }

    public GdMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GdMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctx = context;
    }

    private void initData() {
        PlayHandler playHandler = new PlayHandler(this);
        mLocationClient = new AMapLocationClient(ctx);
        mPlayer = new RadarPlayer(playHandler);
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                mCameraPosition = CameraPosition.fromLatLngZoom(latLng, mZoomLevel);
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(mCameraPosition);
                mMap.moveCamera(update);
                Log.e(TAG, "my location is: " + latLng.toString());
                updateLocation(latLng);
            }
        };
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(true).setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(mLocationListener);
        mGeocodeSearch = new GeocodeSearch(ctx);
        mGeocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                Log.d(TAG, regeocodeResult.getRegeocodeAddress().getFormatAddress());
                LatLonPoint point = regeocodeResult.getRegeocodeQuery().getPoint();
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                List<PoiItem> poiItems = regeocodeResult.getRegeocodeAddress().getPois();
                StringBuilder loc = new StringBuilder();
                loc.append(regeocodeResult.getRegeocodeAddress().getDistrict()).append(" ");
                if (!poiItems.isEmpty()) {
                    loc.append(poiItems.get(0).getTitle());
                } else {
                    loc.append(regeocodeResult.getRegeocodeAddress().getFormatAddress());
                }
                updateMarker(latLng, loc.toString());
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
    }

    private void updateMarker(LatLng latLng, String title) {
        removeMarkers();
        mLastMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title(title));
        mLastMarker.showInfoWindow();
        notifyPlayerPositionChanged(latLng, mZoomLevel);
    }

    private void updateLocation(LatLng latLng) {
        LatLonPoint point = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 0, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
    }

    private void notifyPlayerPositionChanged(LatLng latLng, float zoomLevel) {
        mPlayer.changPosition(latLng, zoomLevel);
    }

    private void removeMarkers() {
        if (null != mLastMarker) {
            mLastMarker.remove();
        }
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.map_layout, this);
        mMapView = (MapView) findViewById(R.id.map_view);
        mMap = mMapView.getMap();
        mBtnPlayControl = (Button) findViewById(R.id.play_control);
        mBtnPlayControl.setOnClickListener(this);
        mTimeLine = (TextView) findViewById(R.id.time_line);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraChangeListener(this);
    }

    private void requestMyLocation() {
        Log.i(TAG, "start request location");
        mLocationClient.startLocation();
    }


    public void onCreate(Bundle bundle) {
        initView(ctx);
        initData();
        mMapView.onCreate(bundle);
    }

    public void onSaveInstanceState(Bundle bundle) {
        Log.i(TAG, "map onsave");
        mMapView.onSaveInstanceState(bundle);
    }

    public void onPause() {
        Log.i(TAG, "map onpause");
        mMapView.onPause();
        mPlayer.pause();
    }

    public void onResume() {
        mMapView.onResume();
        Log.i(TAG, "map onResume");
        requestMyLocation();
        mPlayer.resume();
    }

    public void onDestory() {
        Log.i(TAG, "map onDestory");
        mMapView.onDestroy();
        mPlayer.destory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_control:
                if (mPlayer.isPlay()) {
                    pausePlay();
                } else {
                    resumePlay();
                }
                break;
            default:
                break;
        }
    }

    private void resumePlay() {
        mBtnPlayControl.setText("pause");
        mPlayer.resume();
    }

    private void pausePlay() {
        mBtnPlayControl.setText("play");
        mPlayer.pause();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        updateLocation(latLng);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        mZoomLevel = cameraPosition.zoom;
        notifyPlayerPositionChanged(cameraPosition.target, mZoomLevel);
    }

    static class PlayHandler extends NoLeakHandler<GdMapView> {
        public PlayHandler(GdMapView outClass) {
            super(outClass);
        }

        @Override
        public void handleMessage(Message msg, GdMapView gdMapView) {
            if (null != gdMapView) {
                switch (msg.what) {
                    case RadarPlayer.MSG_PLAY:
                        RadarImageEntity image = (RadarImageEntity) msg.obj;
                        gdMapView.playWav(image);
                        break;
                    case RadarPlayer.MSG_STOP:
                        gdMapView.clearRadioImg();
                        break;
                    default:
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


    private void playWav(RadarImageEntity image) {
        if (mLastGroundOverlay != null) {
            mLastGroundOverlay.remove();
        }
        mLastGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .anchor(0.5f, 0.5f).transparency(0.1f)
                .image(BitmapDescriptorFactory.fromPath(image.getCachePath()))
                .positionFromBounds(image.getArea()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) (image.getTime() * 1000));
        mTimeLine.setText(calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE));
    }
}
