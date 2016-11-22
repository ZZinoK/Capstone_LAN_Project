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
    private String mt_status = "warning";
    private String TAG = "DaumMapActivity";

    MapCircle mt_circle;
    Thread t;
    DynamoDBManager dbManager;
    RegionInfo region = null;

    Runnable runnable = new Runnable() {
        public void run() {
            region = dbManager.userSelectedRegionInfo(MT_ID);
            try {
                latitude = region.getLatitude();
                longitude = region.getLongitude();
            } catch (Exception e){
                e.printStackTrace();
                showToast("지역 선택하고 오셈");
            }
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
        t = new Thread(runnable);

        if(getIntent().getExtras() != null){
            MT_ID = getIntent().getExtras().getInt("MT_ID");
            t.start();
            //showToast("MT_ID from AWS : " + MT_ID);
        }
        try {
            t.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    private void addLocationMarker(MapView mapView, Double latitude, Double longitude, String name){
        MapPoint ADD_MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);

        MapPOIItem objPoiMarker = new MapPOIItem();
        objPoiMarker.setItemName(name);
        objPoiMarker.setTag(0);
        objPoiMarker.setMapPoint(ADD_MARKER_POINT);
        objPoiMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        objPoiMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        objPoiMarker.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
        mapView.addPOIItem(objPoiMarker);
        Color color = new Color();
        if(mt_status.equals("danger")){
            color.argb(128, 255, 0, 0);
            color.argb(128, 255, 0, 0);
        } else if(mt_status.equals("safe")){
            color.argb(128, 255, 0, 0);
            color.argb(128, 255, 0, 0);
        } else {
            color.argb(128, 255, 172, 0);
            color.argb(128, 255, 172, 0);
        }

        mt_circle = new MapCircle(
                MapPoint.mapPointWithGeoCoord(latitude, longitude), // center
                150, // radius
                Color.argb(128, 255, 0, 0), // strokeColor
                Color.argb(128, 255, 0, 0) // fillColor
        );
        mt_circle.setTag(1234);
        mapView.addCircle(mt_circle);
    }
//    private void setAllObjectMarker() {
//        for(ObjectGPS obj : objPosList){
//            addObjectMarker(daumMapView, obj.getLatitude(), obj.getLongitude(), obj.getObjName());
//        }
//    }
    @Override
    public void onMapViewInitialized(MapView mapView) {
        if(latitude == 0 || longitude == 0){
            daumMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            showToast("지역을 선택하고 다시오셈");
        } else {
            daumMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 4, true);
        }
        addLocationMarker(daumMapView, latitude, longitude, mt_status);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

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
