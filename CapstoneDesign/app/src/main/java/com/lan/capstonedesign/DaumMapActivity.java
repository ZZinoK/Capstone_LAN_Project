package com.lan.capstonedesign;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

/*new Thread(new Runnable() {
@Override
public void run() {
        try {
        Thread.sleep(5000);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
@Override
public void run() {
        pic.get(0).setImageDrawable(getResources().getDrawable(R.drawable.coin));
        }
        });
        }
        }).start();
       handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    t.start();
                }
            }, 1000);
       */
/**
 * Created by kslee7746 on 2016. 11. 3..
 */
public class DaumMapActivity extends Activity implements MapView.MapViewEventListener {
    private MapView daumMapView;
    private String apikey = "0a103b404c5ab42d35918fd32b3efcd9";
//    private double latitude = 37.283077, longitude = 127.044908;
    private double latitude = 0, longitude = 0;
    private int MT_ID = 0;
    private String mt_name = null;
    private boolean AdminLoginStatus = false;
    private String mt_status = "warning";
    private String TAG = "DaumMapActivity";
    private ArrayList<NodeInfo> nodeInfoArrayList;

    Thread regionTh, nodeTh;
    DynamoDBManager dbManager;
    RegionInfo region = null;

    Runnable selectRegionRunnable = new Runnable() {
        public void run() {
            region = dbManager.userSelectedRegionInfo(MT_ID);
            try {
                mt_name = region.getMountainName();
                latitude = region.getLatitude();
                longitude = region.getLongitude();
                mt_status = region.getMountainStatus();
            } catch (Exception e){
                e.printStackTrace();
                showToast("지역 선택하고 오셈");
            }
        }
    };
    Runnable selectNodeRunnable = new Runnable() {
        public void run() {
            nodeInfoArrayList = dbManager.getNodeInfoArrayList();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.daum_map);

        dbManager = DynamoDBManager.getInstance(this);
        regionTh = new Thread(selectRegionRunnable);
        nodeTh = new Thread(selectNodeRunnable);

        if(getIntent().getExtras() != null){
            MT_ID = getIntent().getExtras().getInt("MT_ID");
            AdminLoginStatus = getIntent().getExtras().getBoolean("Admin");
            try {
                regionTh.start();
                nodeTh.start();
                regionTh.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(AdminLoginStatus)
                showToast("너는 관리자야");
            else
                showToast("너는 양민이야");
        }

        try {
            daumMapView = new MapView(DaumMapActivity.this);
            daumMapView.setDaumMapApiKey(apikey);
            daumMapView.setMapViewEventListener(this);
            ViewGroup mapViewContainer = (ViewGroup)findViewById(R.id.daumView);
            mapViewContainer.removeAllViews();
            mapViewContainer.addView(daumMapView);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DaumMapActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addLocationMarker(MapView mapView, Double latitude, Double longitude, int node_id){
        MapPoint ADD_MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        MapPOIItem objPoiMarker = new MapPOIItem();
        objPoiMarker.setItemName(mt_name +" TelosB_"+node_id);
        objPoiMarker.setTag(0);
        objPoiMarker.setMapPoint(ADD_MARKER_POINT);
        objPoiMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        objPoiMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        objPoiMarker.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
        mapView.addPOIItem(objPoiMarker);

        MapCircle mt_circle = new MapCircle(
                //mapPointWithGeoCoord(center, radius, strokeColor, fillColor)
                MapPoint.mapPointWithGeoCoord(latitude, longitude), 100, 0, 0);

        if(mt_status.equals("danger")){
            mt_circle.setFillColor(Color.argb(90, 255, 0, 0));
            mt_circle.setStrokeColor(Color.argb(255, 255, 0, 0));
        } else if(mt_status.equals("safe")){
            mt_circle.setFillColor(Color.argb(90, 0, 255, 0));
            mt_circle.setStrokeColor(Color.argb(255, 0, 255, 0));
        } else {
            mt_circle.setStrokeColor(Color.argb(90, 255, 172, 0));
            mt_circle.setStrokeColor(Color.argb(255, 255, 172, 0));
        }

        mt_circle.setTag(1234);
        mapView.addCircle(mt_circle);
    }
    private void defaultMarker(MapView mapView, Double latitude, Double longitude, int node_id){
        MapPoint ADD_MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        MapPOIItem objPoiMarker = new MapPOIItem();
        objPoiMarker.setItemName(mt_name);
        objPoiMarker.setTag(0);
        objPoiMarker.setMapPoint(ADD_MARKER_POINT);
        objPoiMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        objPoiMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        objPoiMarker.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
        mapView.addPOIItem(objPoiMarker);
    }
    private void setAllNodeMarker() {
        for(NodeInfo node : nodeInfoArrayList){
            addLocationMarker(daumMapView, node.getLatitude(), node.getLongitude(), node.getNode_ID());
        }
    }
    @Override
    public void onMapViewInitialized(MapView mapView) {
        mapView.setMapType(MapView.MapType.Hybrid);
        if(MT_ID == 0){
            daumMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            showToast("지역을 선택하고 다시오셈");
        } else {
            daumMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            daumMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 3, true);
            if(MT_ID != 4) {
                defaultMarker(daumMapView, latitude, longitude, 0);
                showToast("아직 서비스 예정인 지역입니다. 돌아가셈");
            } else {
                setAllNodeMarker();
            }
        }

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) { }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) { }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
}
