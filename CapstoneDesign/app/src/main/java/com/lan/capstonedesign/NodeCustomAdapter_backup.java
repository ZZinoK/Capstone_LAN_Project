package com.lan.capstonedesign;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kslee7746 on 2016. 11. 10..
 */

public class NodeCustomAdapter_backup extends BaseAdapter{
    private NodeInfo node;
    private ArrayList<NodeInfo> nodeInfoArrayList;
    private Context mContext;
    private TextView node_id;
    private TextView node_x;
    private TextView node_y;
    private TextView node_z;
    private TextView node_route;
    private TextView node_variation;

    public NodeCustomAdapter_backup(Context context){
        super();
        nodeInfoArrayList = new ArrayList<NodeInfo>();
        mContext = context;
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public NodeInfo getItem(int position) {
        return nodeInfoArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
//        View v = convertView;
        if (convertView == null ) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.node_item, null);
            // TextView에 현재 position의 문자열 추가
            node_id = (TextView) convertView.findViewById(R.id.node_id);
            node_x = (TextView) convertView.findViewById(R.id.node_x);
            node_y = (TextView) convertView.findViewById(R.id.node_y);
            node_z = (TextView) convertView.findViewById(R.id.node_z);
            node_route = (TextView) convertView.findViewById(R.id.node_route);
            node_variation = (TextView) convertView.findViewById(R.id.node_variation);
            //text.setText(m_List.get(position));

        }
        node = getItem(position);
        if(node != null){
            node_id.setText(""+node.getNode_ID());
            node_route.setText(""+node.getRoute());
            node_x.setText(""+node.getNode_X());
            node_y.setText(""+node.getNode_Y());
            node_z.setText(""+node.getNode_Z());
            node_variation.setText(""+node.getVariation());
            Log.d("CustomAdapter", node_id + " " + node_route + " " + node_x);
        }


        return convertView;
    }
    public void add(NodeInfo node){
        nodeInfoArrayList.add(node);
    }
}
