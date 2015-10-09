package baidumapsdk.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.cloud.CloudListener;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.cloud.DetailSearchResult;
import com.baidu.mapapi.cloud.NearbySearchInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 演示poi搜索功能
 */
public class PoiSearchDemo extends FragmentActivity implements
		OnGetPoiSearchResultListener, OnGetSuggestionResultListener,BaiduMap.OnMapClickListener,
		OnGetRoutePlanResultListener, CloudListener {
	private static final String TAG = "PoiSearchDemo";

	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	//private BaiduMap mBaiduMap = null;

	/**
	 * 搜索关键字输入窗口
	 */
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index = 0;
	/**
	 * 定位组件
	 */
	MapView mMapView;
	BaiduMap mBaiduMap;

	//浏览路线节点相关
	Button mBtnPre = null;//上一个节点
	Button mBtnNext = null;//下一个节点
	int nodeIndex = -1;//节点索引,供浏览节点时使用
	RouteLine route = null;
	OverlayManager routeOverlay = null;
	boolean useDefaultIcon = false;
	private TextView popupText = null;//泡泡view

	//搜索相关
	RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用

	// 定位相关
	LocationClient mLocClient;
	MyLocationData locData;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;
	PoiDetailResult PDR;
	CloudPoiInfo poi ;

	//UI相关组件
	boolean isFirstLoc = true;// 是否首次定位
	Button requestLocButton;
//	Button goThereBtn = (Button)findViewById(R.id.gothereBtn);
//	Button subscribeBtn = (Button)findViewById(R.id.subscribeBtn);
    LinearLayout PoiInfo;
	TextView percentage ;
	//蓝牙相关
	//Bluetooth_service btService ;
	ServiceConnection conn;
	Bluetooth_service.myBinder myBinder;
	public Handler handler = new Handler() {
		public void handleMessage(Message msg){
			switch (msg.what){
				case Constants.MESSAGE_STATE_CHANGE:
					if(msg.arg1==-1){
						Toast.makeText(PoiSearchDemo.this,"blurtooth connection break",Toast.LENGTH_SHORT).show();
						if (conn != null) {
							unbindService(conn);
							conn = null;
						}
					}else
					Toast.makeText(PoiSearchDemo.this,"bluetooth connection status changed",Toast.LENGTH_SHORT).show();
					break;
//				case Constants.MESSAGE_DEVICE_NAME:
//					Toast.makeText(PoiSearchDemo.this,"scan bluetooth device overtime",Toast.LENGTH_SHORT).show();
//					break;
				case Constants.MESSAGE_READ:
					switch (msg.arg2){
						case 1:
							percentage.setText("Ack");
							break;
						case 2:
							try{
								percentage.setText(Integer.toString(msg.arg1));
							}catch (Exception e){
								e.printStackTrace();
							}
						default:
							break;
					}
					//Toast.makeText(PoiSearchDemo.this,new String((byte[])msg.obj),Toast.LENGTH_SHORT).show();
					break;
				case Constants.MESSAGE_SCAN_OVERTIME:
					Toast.makeText(PoiSearchDemo.this,"scan bluetooth device overtime",Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_poisearch);
		Log.v(TAG,"poi Starts");
		CloudManager.getInstance().init(PoiSearchDemo.this);
		//蓝牙相关初始化
		//btService = new Bluetooth_service(handler);
		/**
		 * 此处进行Bluetooth_service的绑定
		 * bindService需要ServiceConnection类作为参数
		 * 该类里的方法指定了绑定后和绑定解除后的操作
		 */
		Intent intent = new Intent(PoiSearchDemo.this,Bluetooth_service.class);
		conn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.v(TAG, "onServiceConnected");
				myBinder = (Bluetooth_service.myBinder) service;
				myBinder.set_handler(handler);
				myBinder.start_Bluetooth();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				myBinder = null;
				Log.v(TAG, "onServiceDisconnected");
			}
		};
		bindService(intent,conn , BIND_AUTO_CREATE);

		//初始化UI相关组件
		Log.v(TAG,"init component");
		percentage = (TextView)findViewById(R.id.percentage);
		requestLocButton = (Button) findViewById(R.id.button1);
		mCurrentMode = LocationMode.NORMAL;requestLocButton.setText("普通");
        PoiInfo = (LinearLayout)findViewById(R.id.PoiInfo);

		View.OnClickListener btnClickListener = new View.OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
					case NORMAL:
						requestLocButton.setText("跟随");
						mCurrentMode = LocationMode.FOLLOWING;
						mBaiduMap
								.setMyLocationConfigeration(new MyLocationConfiguration(
										mCurrentMode, true, mCurrentMarker));
						break;
					case COMPASS:
						requestLocButton.setText("普通");
						mCurrentMode = LocationMode.NORMAL;
						mBaiduMap
								.setMyLocationConfigeration(new MyLocationConfiguration(
										mCurrentMode, true, mCurrentMarker));
						break;
					case FOLLOWING:
						requestLocButton.setText("罗盘");
						mCurrentMode = LocationMode.COMPASS;
						mBaiduMap
								.setMyLocationConfigeration(new MyLocationConfiguration(
										mCurrentMode, true, mCurrentMarker));
						break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		// 初始化搜索模块，注册搜索事件监听
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		//keyWorldsView.setAdapter(sugAdapter);

		//初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBtnPre = (Button) findViewById(R.id.pre);
		mBtnNext = (Button) findViewById(R.id.next);
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		//地图点击事件处理
		mBaiduMap.setOnMapClickListener(this);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();


		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
//		keyWorldsView.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void afterTextChanged(Editable arg0) {
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence arg0, int arg1,
//					int arg2, int arg3) {
//
//			}
//
//			@Override
//			public void onTextChanged(CharSequence cs, int arg1, int arg2,
//					int arg3) {
//				if (cs.length() <= 0) {
//					return;
//				}
//				String city = ((EditText) findViewById(R.id.city)).getText()
//						.toString();
//				/**
//				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
//				 */
//				mSuggestionSearch
//						.requestSuggestion((new SuggestionSearchOption())
//								.keyword(cs.toString()).city(city));
//			}
//		});
		//requestForConnection();
	}


//	public void requestForConnection(){
//		outputStream = info.outputStream;
//		try{
//			outputStream.write(Bluetooth_service.getRequestForConnectionPack());
//		}catch (IOException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * 发起路线规划搜索
	 *
	 * @param v
	 */
	public void SearchButtonProcess(View v) {
		//重置浏览节点的路线数据
		route = null;
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		mBaiduMap.clear();
		// 处理搜索按钮响应
//		EditText editSt = (EditText) findViewById(R.id.start);
//		EditText editEn = (EditText) findViewById(R.id.end);
		//设置起终点信息，对于tranist search 来说，城市名无意义
//		PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", editSt.getText().toString());
//		PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", editEn.getText().toString());
		PlanNode stNode = PlanNode.withLocation(new LatLng(locData.latitude,locData.longitude));
//		PlanNode enNode = PlanNode.withLocation(PDR.getLocation());
		PlanNode enNode = PlanNode.withLocation(new LatLng(poi.latitude,poi.longitude));
		// 实际使用中请对起点终点城市进行正确的设定
//		if (v.getId() == R.id.drive) {
//			mSearch.drivingSearch((new DrivingRoutePlanOption())
//					.from(stNode)
//					.to(enNode));
//		} else if (v.getId() == R.id.transit) {
//			mSearch.transitSearch((new TransitRoutePlanOption())
//					.from(stNode)
//					.city("北京")
//					.to(enNode));
//		} else if (v.getId() == R.id.walk) {
			mSearch.walkingSearch((new WalkingRoutePlanOption())
					.from(stNode)
					.to(enNode));
		//}
	}

    /**
     * 点击“预订电池”按钮触发此函数
     * 此处向服务器发送受预订的加电站和预订请求
     * @param v
     */
    public void bookBatt(View v){
        //Toast.makeText(PoiSearchDemo.this,"此处预订加电站电池",Toast.LENGTH_LONG).show();
		Intent intent = new Intent();
		intent.setClass(PoiSearchDemo.this, BattStation.class);
		startActivity(intent);
	}

	/**
	 * 节点浏览
     * 按下结点Node上下选择按钮，选择结点
	 * @param v
	 */
	public void nodeClick(View v) {
		if (route == null ||
				route.getAllStep() == null) {
			return;
		}
		if (nodeIndex == -1 && v.getId() == R.id.pre) {
			return;
		}
		//设置节点索引
		if (v.getId() == R.id.next) {
			if (nodeIndex < route.getAllStep().size() - 1) {
				nodeIndex++;
			} else {
				return;
			}
		} else if (v.getId() == R.id.pre) {
			if (nodeIndex > 0) {
				nodeIndex--;
			} else {
				return;
			}
		}
		//获取节结果信息
		LatLng nodeLocation = null;
		String nodeTitle = null;
		Object step = route.getAllStep().get(nodeIndex);
		if (step instanceof DrivingRouteLine.DrivingStep) {
			nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
			nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
		} else if (step instanceof WalkingRouteLine.WalkingStep) {
			nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
			nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
		} else if (step instanceof TransitRouteLine.TransitStep) {
			nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
			nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
		}

		if (nodeLocation == null || nodeTitle == null) {
			return;
		}
		//移动节点至中心
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
		// show popup
		popupText = new TextView(PoiSearchDemo.this);
		popupText.setBackgroundResource(R.drawable.popup);
		popupText.setTextColor(0xFF000000);
		popupText.setText(nodeTitle);
		mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

	}

    /**
     * 当搜索到路线结果时调用此函数
     * 效果：显示结点node上下选择按钮，将路线调掉屏幕中央
     * @param result
     */
	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			//起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			//result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {

		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			//起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			//result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			//起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			//result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

    /**
     * 点击地图空白处
     * 效果：隐藏popup TextView
     * @param point
     */
	@Override
	public void onMapClick(LatLng point) {
		mBaiduMap.hideInfoWindow();
		//PoiInfo.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onMapPoiClick(MapPoi poi) {
		return false;
	}

	/**
	 * 定位我的位置监听类
	 * 收到后把我的位置移到中心
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不再处理新接收的位置
			if (location == null || mMapView == null)
				return;
			locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
							// 此处设置开发者获取到的方向信息，顺时针0-360，但是直接暴力设置了100因为没有调用传感器
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {//把地图中心移到我的位置
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}
		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (conn != null) {
			unbindService(conn);
			conn = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
//		if(btService != null){
//			if(btService.getState()==Bluetooth_service.STATE_NONE){
//				btService.start();
//			}
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		if (conn != null) {
			unbindService(conn);
			conn = null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * 按下“查看附近加油站按钮”，执行此函数
	 * 搜索附近的加油站
	 * @param v
	 */
	public void searchButtonProcess(View v) {
//		EditText editCity = (EditText) findViewById(R.id.city);
//		EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
//		mPoiSearch.searchInCity((new PoiCitySearchOption())
//				.city(editCity.getText().toString())
//				.keyword(editSearchKey.getText().toString())
//				.pageNum(load_Index));

		//POI搜索
//		mPoiSearch.searchInCity((new PoiCitySearchOption())
//				.city("北京")
//				.keyword("加油站")
//				.pageNum(load_Index));

		//LBS云搜索
		NearbySearchInfo info = new NearbySearchInfo();
		info.ak = "7FUxvnQElOk78AH6RYZ66RjT";
		//info.q = "天安门";
		info.geoTableId = 116982;
		info.radius = 30000;
		info.location = "116.403689,39.914957";
		try{
			CloudManager.getInstance().nearbySearch(info);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void searchButtonProcess2(View v){
		NearbySearchInfo info = new NearbySearchInfo();
		info.ak = "7FUxvnQElOk78AH6RYZ66RjT";
		//info.q = "天安门";
		info.geoTableId = 116982;
		info.radius = 30000;
		info.location = "116.403689,39.914957";
		try{
			CloudManager.getInstance().nearbySearch(info);
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	public void onGetSearchResult(CloudSearchResult result, int error) {
		if (result != null && result.poiList != null
				&& result.poiList.size() > 0) {
			Log.d(TAG, "onGetSearchResult, result length: " + result.poiList.size());
//			mBaiduMap.clear();
//			BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
//			LatLng ll;
//			LatLngBounds.Builder builder = new LatLngBounds.Builder();
//			for (CloudPoiInfo info : result.poiList) {
//				ll = new LatLng(info.latitude, info.longitude);
//				OverlayOptions oo = new MarkerOptions().icon(bd).position(ll);
//				mBaiduMap.addOverlay(oo);
//				builder.include(ll);
//			}
//			mBaiduMap.setOnMarkerClickListener();
//			LatLngBounds bounds = builder.build();
//			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
//			mBaiduMap.animateMapStatus(u);
			mBaiduMap.clear();
			MyCloudOverlay overlay = new MyCloudOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	public void onGetDetailSearchResult(DetailSearchResult result, int error) {
		if (result != null) {
			if (result.poiInfo != null) {
				Toast.makeText(PoiSearchDemo.this, result.poiInfo.title,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(PoiSearchDemo.this,
						"status:" + result.status, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 点击查看下一组POI地点
	 * 一组10个
	 * @param v
	 */
//	public void goToNextPage(View v) {
//		load_Index++;
//		searchButtonProcess(null);
//	}

	/**
	 * 监听poi搜索
	 * 收到执行此程序
	 * @param result
	 */
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(PoiSearchDemo.this, "未找到结果", Toast.LENGTH_LONG)
			.show();
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			mBaiduMap.clear();
			PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到结果";
			Toast.makeText(PoiSearchDemo.this, strInfo, Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * 监听点击POI后的返回信息
	 * 收到信息执行此函数
	 * @param result
	 */
	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
					.show();
		} else {
//			Toast.makeText(PoiSearchDemo.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
//			.show();
			PDR = result;
			PoiInfo.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}

	/**
	 * 定制PoiOverlay
	 * 点击搜索POI详细信息，并触发onGetPoiDetailResult();
	 */
	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			// if (poi.hasCaterDetails) {
				mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
						.poiUid(poi.uid));
			// }
			return true;
		}
	}

	/**
	 * 定制了LBS云搜索结果的overlay
	 * 因为云搜索结果CloudSearchResult不能被PoiOverlay解析，导致无法将CloudSearchResult的点击结果返回，因此无法进行路径搜索
	 * 该类在OverlayManager以及其子类PoiOverlay基础上修改
	 * 使得可以构造一个MyCloudOverlay显示CloudSearchResult
	 * 由于实现了OnMarkerClickListener接口，可以得到地图点击位置的索引Index
	 * 通过CloudSearchResult.List找到该CloudPoiInfo，即被点击的POI点
	 * 然后通过POI搜索DetailedResult
	 *
	 */
	public class MyCloudOverlay implements BaiduMap.OnMarkerClickListener {
		BaiduMap mBaiduMap = null;
		private List<OverlayOptions> overlayOptionses = null;
		List<Overlay> overlayList = null;
		private CloudSearchResult cloudResult = null;

		public MyCloudOverlay(BaiduMap var1) {
			this.mBaiduMap = var1;
			if(this.overlayOptionses == null) {
				this.overlayOptionses = new ArrayList();
			}

			if(this.overlayList == null) {
				this.overlayList = new ArrayList();
			}

		}

		public void setData(CloudSearchResult var1) {
			this.cloudResult = var1;
		}

		public final List<OverlayOptions> getOverlayOptions() {
			if(this.cloudResult != null && this.cloudResult.poiList != null) {
				ArrayList var1 = new ArrayList();
				int var2 = 0;

				for(int var3 = 0; var3 < this.cloudResult.poiList.size() && var2 < 10; ++var3) {
					if(this.cloudResult.poiList.get(var3).longitude != 0 && this.cloudResult.poiList.get(var3).latitude != 0) {
						++var2;
						Bundle var4 = new Bundle();
						var4.putInt("index", var3);
						var1.add(
								(new MarkerOptions())
										.icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark" + var2 + ".png"))
										.extraInfo(var4)
										.position(new LatLng(((CloudPoiInfo) this.cloudResult.poiList.get(var3)).latitude, ((CloudPoiInfo) this.cloudResult.poiList.get(var3)).longitude))
										);
					}
				}

				return var1;
			} else {
				return null;
			}
		}

		public final void addToMap() {
			if(this.mBaiduMap != null) {
				this.removeFromMap();
				List var1 = this.getOverlayOptions();
				if(var1 != null) {
					this.overlayOptionses.addAll(this.getOverlayOptions());
				}

				Iterator var2 = this.overlayOptionses.iterator();

				while(var2.hasNext()) {
					OverlayOptions var3 = (OverlayOptions)var2.next();
					this.overlayList.add(this.mBaiduMap.addOverlay(var3));
				}

			}
		}

		public final void removeFromMap() {
			if(this.mBaiduMap != null) {
				Iterator var1 = this.overlayList.iterator();

				while(var1.hasNext()) {
					Overlay var2 = (Overlay)var1.next();
					var2.remove();
				}

				this.overlayOptionses.clear();
				this.overlayList.clear();
			}
		}

		public void zoomToSpan() {
			if(this.mBaiduMap != null) {
				if(this.overlayList.size() > 0) {
					LatLngBounds.Builder var1 = new LatLngBounds.Builder();
					Iterator var2 = this.overlayList.iterator();

					while(var2.hasNext()) {
						Overlay var3 = (Overlay)var2.next();
						if(var3 instanceof Marker) {
							var1.include(((Marker)var3).getPosition());
						}
					}

					this.mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(var1.build()));
				}

			}
		}

		@Override
		public final boolean onMarkerClick(Marker var1) {
			return !this.overlayList.contains(var1)?false:(var1.getExtraInfo() != null?this.onPoiClick(var1.getExtraInfo().getInt("index")):false);
		}

		public boolean onPoiClick(int index) {
			poi = cloudResult.poiList.get(index);
			// if (poi.hasCaterDetails) {
//			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
//					.poiUid(Integer.toString(poi.geotableId)));
			PoiInfo.setVisibility(View.VISIBLE);
			// }
			return true;
		}
	}

	//定制RouteOverlay
	private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	private class MyTransitRouteOverlay extends TransitRouteOverlay {

		public MyTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

}
