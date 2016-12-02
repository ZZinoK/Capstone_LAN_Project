package com.lan.capstonedesign;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by kslee7746 on 2016. 11. 3..
 */
public class DaumMapActivity extends Activity implements MapView.MapViewEventListener {
    private MapView daumMapView;
    private String apikey = "0a103b404c5ab42d35918fd32b3efcd9";
    private double latitude = 0, longitude = 0;
    private int MT_ID = 0;
    private String mt_name = null;
    private boolean AdminLoginStatus = false;
    private String TAG = "DaumMapActivity";
    private ArrayList<NodeInfo> nodeInfoArrayList;
    private boolean threadState = true;
    private boolean initMarkerState = true;
    Thread regionTh, nodeTh;
    DynamoDBManager dbManager;
    RegionInfo region = null;
    private int NODE_NUM = 6;
    boolean nodeStatusArray[];

    Runnable selectRegionRunnable = new Runnable() {
        public void run() {
            try {
                region = dbManager.userSelectedRegionInfo(MT_ID);
                mt_name = region.getMountainName();
                latitude = region.getLatitude();
                longitude = region.getLongitude();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    Runnable selectNodeRunnable = new Runnable() {
        public void run() {
            while(threadState){
                try{
                    nodeInfoArrayList = dbManager.getNodeInfoArrayList();
                    if(nodeInfoArrayList.equals(null)){
                        nodeInfoArrayList = dbManager.getNodeInfoArrayList();
                        Thread.sleep(1500);
                    }
                    //Log.d(TAG, "Map Thread 돌고 있다.");
                    Thread.sleep(1500);
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            daumMapView.removeAllCircles();
                            if(AdminLoginStatus) {
                                daumMapView.removeAllPOIItems();
                                daumMapView.removeAllPolylines();
                            }
                            drawNodeRouteLine();
                            setAllNodeMarker();

                            Log.d(TAG, "UI Thread running!");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
    @Override
    protected void onStop() {
        super.onStop();
        threadState = false;
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        threadState = false;
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
                regionTh.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(AdminLoginStatus)
                showToast("당신은 " + mt_name + "의 관리자입니다.");
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
    private void checkDeadNode(){

        for(int i=0; i<nodeStatusArray.length; i++){
            if(!nodeStatusArray[i]){
                nodeInfoArrayList.get(i).setVariation(0);
                Log.d(TAG, i+1 + " node 죽음");
            }
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
    private void addLocationMarker(MapView mapView, Double latitude, Double longitude, int node_id, int variation){
        MapPoint ADD_MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        MapPOIItem objPoiMarker = new MapPOIItem();
        objPoiMarker.setMapPoint(ADD_MARKER_POINT);

        if(AdminLoginStatus) {
            if(node_id == 1) {
                objPoiMarker.setItemName(mt_name + " TelosB_Sink");
            } else {
                objPoiMarker.setItemName(mt_name + " TelosB_" + node_id);
            }
            objPoiMarker.setTag(0);
            objPoiMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            objPoiMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            objPoiMarker.setShowAnimationType(MapPOIItem.ShowAnimationType.NoAnimation);
            mapView.addPOIItem(objPoiMarker);
        }

        MapCircle mt_circle = new MapCircle(
                //mapPointWithGeoCoord(center, radius, strokeColor, fillColor)
                MapPoint.mapPointWithGeoCoord(latitude, longitude), 100, 0, 0);

        if(variation == Constants.DANGER){
            mt_circle.setFillColor(Color.argb(90, 255, 0, 0));
            mt_circle.setStrokeColor(Color.argb(255, 255, 0, 0));
        } else if(variation == Constants.SAFE){
            mt_circle.setFillColor(Color.argb(90, 0, 255, 0));
            mt_circle.setStrokeColor(Color.argb(255, 0, 255, 0));
        } else if(variation == Constants.WARNING){
            mt_circle.setFillColor(Color.argb(90, 255, 172, 0));
            mt_circle.setStrokeColor(Color.argb(255, 255, 172, 0));
        } else {
            return;
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
            if(node.getVariation() != 0){
                addLocationMarker(daumMapView, node.getLatitude(), node.getLongitude(), node.getNode_ID(), node.getVariation());
            }
        }
    }
    public int[] parsingNodeRoute(int route){
        int num[] = new int[String.valueOf(route).length()];
        int i = 0;

        while(route > 0){
            num[i] = route % 10;
            route = route / 10;
            i++;
        }
        return num;
    }
    public void drawNodeRouteLine(){
        int route[];
        nodeStatusArray = new boolean[NODE_NUM];
        Collections.sort(nodeInfoArrayList, new NodeMonitoringActivity.NoAscCompare());

        for(NodeInfo node : nodeInfoArrayList){
            MapPolyline mapPolyline = new MapPolyline();
            mapPolyline.setLineColor(Color.argb(128, 0, 0, 0));
            route = parsingNodeRoute(node.getRoute());

            if(route.length == 1 || node.getVariation() == 0){
                continue;
            }
            for(int i = 0; i<route.length; i++){
                nodeStatusArray[route[i]-1] = true;
                mapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(nodeInfoArrayList.get(route[i]-1).getLatitude(),
                        nodeInfoArrayList.get(route[i]-1).getLongitude()));
            }
            if(AdminLoginStatus)
                daumMapView.addPolyline(mapPolyline);
        }
        checkDeadNode();
    }
    @Override
    public void onMapViewInitialized(MapView mapView) {
        mapView.setMapType(MapView.MapType.Hybrid);
        if(MT_ID == 0){
            daumMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            showToast("지역을 선택하고 오세요.");
        } else {
            daumMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            daumMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 3, true);
            if(MT_ID != 4) {
                defaultMarker(daumMapView, latitude, longitude, 0);
                showToast("서비스 예정인 지역입니다.\n현재 서비스 가능한 지역은 팔달산입니다.");
            } else {
                //if(AdminLoginStatus){
                    drawNodeRouteLine();
                //}

                setAllNodeMarker();
                initMarkerState = false;

            }
        }

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) { }
    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) { }
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) { }
    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) { }
    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) { }
    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) { }
    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) { }
    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) { }
}
