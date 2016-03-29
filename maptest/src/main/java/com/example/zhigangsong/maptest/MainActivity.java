package com.example.zhigangsong.maptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {

    public static final String URL = "http://caiyunapp.com/fcgi-bin/v1/img.py?token=Y2FpeXVuIGFuZHJpb2QgYXBp";
    List<ImgBean> mImgBeans;
    AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
    private Marker myLocMarker;
    CameraPosition mCameraPosition;
    private float mZoomLevel = 5;
    public AMapLocationClient mLocationClient = null;
    GeocodeSearch mGeocodeSearch;

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            Log.d(TAG, aMapLocation.toString());
            LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            mCameraPosition = CameraPosition.fromLatLngZoom(latLng, mZoomLevel);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(mCameraPosition);
            mAMap.moveCamera(update);
            Log.d(TAG,aMapLocation.toString());
            updateMarker(latLng);
        }
    };


    private void getImgs() {
        mAsyncHttpClient.get(URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mImgBeans = ImgParseUtil.parse(response);
                Log.d(TAG, String.valueOf(mImgBeans.size()));
            }
        });
    }

    private static final String TAG = "huli";
    MapView mMapView;
    AMap mAMap;

    UiSettings mUiSettings;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mMapView.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        requestLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.onDestroy();
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
        myLocMarker = mAMap.addMarker(new MarkerOptions().position(latLng).icon(
                BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        getImgs();
    }


    private void init() {
        mMapView = (MapView) findViewById(R.id.map_view);
        if (mMapView != null) {
            mAMap = mMapView.getMap();
            mUiSettings = mAMap.getUiSettings();
            mUiSettings.setZoomControlsEnabled(false);
            mAMap.setOnMapClickListener(this);
            mAMap.setOnCameraChangeListener(this);
        }
        mTextView = (TextView) findViewById(R.id.loc_info);
        mGeocodeSearch = new GeocodeSearch(this);
        mGeocodeSearch.setOnGeocodeSearchListener(this);
    }

    private void requestLocation() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(true);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.startLocation();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        updateMarker(latLng);
    }

    private void removeMarkers() {
        if (null != myLocMarker) {
            myLocMarker.remove();
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
        Log.d(TAG, cameraPosition.toString());
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.d(TAG, ":dis " + regeocodeResult.getRegeocodeAddress().getDistrict());
        Log.d(TAG, ":pois " + regeocodeResult.getRegeocodeAddress().getPois().toString());
        Log.d(TAG, ":addr " + regeocodeResult.getRegeocodeAddress().getFormatAddress());
        List<PoiItem> poiItems = regeocodeResult.getRegeocodeAddress().getPois();
        StringBuilder loc = new StringBuilder();
        loc.append(regeocodeResult.getRegeocodeAddress().getDistrict()).append(" ");
        if (!poiItems.isEmpty()) {
            loc.append(poiItems.get(0).getTitle());
        } else {
            loc.append(regeocodeResult.getRegeocodeAddress().getFormatAddress());
        }
        mTextView.setText(loc);
    }

    private void playWav(LatLngBounds bounds) {
        GroundOverlay groundoverlay = mAMap.addGroundOverlay(new GroundOverlayOptions()
                .anchor(0.5f, 0.5f).transparency(0.1f)
                .image(BitmapDescriptorFactory.fromResource(R.drawable.r1))
                .positionFromBounds(bounds));
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    public void user(View view) {
        Toast.makeText(this, "用户反馈", Toast.LENGTH_SHORT).show();
    }

    public void radio(View view) {
        Toast.makeText(this, "雷达", Toast.LENGTH_SHORT).show();
    }
}
