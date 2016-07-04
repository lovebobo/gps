package com.baidu.location.demo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.location.service.AbsHttpTask;
import com.baidu.location.service.LocationService;
import com.baidu.location.service.NetworkTask;
import com.baidu.location.service.Utility;
import com.baidu.location.service.infoData;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/***
 * 单点定位示例，用来展示基本的定位结果，配置在LocationService.java中
 * 默认配置也可以在LocationService中修改
 * 默认配置的内容自于开发者论坛中对开发者长期提出的疑问内容
 * 
 * @author baidu
 *
 */
public class LocationActivity extends Activity {
	private final int SDK_PERMISSION_REQUEST = 127;
	private String permissionInfo;
	
	
	private LocationService locationService;
	private TextView LocationResult;
	private Button startLocation;
	private Button getLocation;
	
	private Handler mHandler = new Handler();  
    SQLiteDatabase db;
    private ArrayList<infoData> gpsList=new ArrayList<infoData>();
    private static NetworkTask taskPool = new NetworkTask();
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// -----------demo view config ------------
		setContentView(R.layout.location);
		getPersimmions();
		LocationResult = (TextView) findViewById(R.id.textView1);
		LocationResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		startLocation = (Button) findViewById(R.id.addfence);
		
		getLocation=(Button) findViewById(R.id.getData);
		
		getLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				query();
			}
		});
		initSQL();
		handler.postDelayed(runnable, 120000);

	}

	final Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}
		
	}; 
	
	Runnable runnable = new Runnable(){ 

		@Override 
		public void run() { 
		// TODO Auto-generated method stub 
			postData("http://222.197.181.28:5000/gpsdata");
			handler.postDelayed(runnable, 120000); 
		} 

	}; 

	//检测是否wifi在线
	public boolean checkNetworkConnection(Context context)  
	   {  
	       final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	  
	       final android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
	       	  
	       if(wifi.isAvailable())  
	           return true;  
	       else  
	           return false;  
	   }  	
	
	//传送数据
	protected void postData(String GPSURL) {
		// TODO Auto-generated method stub
		gpsList.clear();
		query();
		JSONArray jsonArray = new JSONArray();
		for (int i=0; i < gpsList.size(); i++) {
	        jsonArray.put(gpsList.get(i).getJSONObject());
		}
		HashMap<String, String> localHashMap = new HashMap<String, String>();
		
		localHashMap.put(Utility.toUtf8("gpsdata"),jsonArray.toString());
		//检测是否wifi在线，是就传数据 否则取消
		if(checkNetworkConnection(this)){
		taskPool.addHttpPostTask(GPSURL, localHashMap,new AbsHttpTask() {
			
			@Override
			public void onError(Object msg) {
				// TODO Auto-generated method stub
				Log.e("error"," " + msg);  
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				Log.e("error"," " + "upload_error");  
			}
			
			@Override
			public void onComplete(InputStream paramInputStream) {
				// TODO Auto-generated method stub
				//
				String result = Utility.streamToString(paramInputStream);
				if(result.indexOf("success")!=-1)				
				{					
						drop();//清空数据库
						Log.e("result", "success");  
				}
				else{
						Log.e("result_error", "error");  
				}
			}
			
		});
		}
	}
	
	//String  to json
		public static Object getNameFromJson(String result, String name)
		{
			try
			{
		   		JSONObject jsonObject = new JSONObject(result);
			   	if(jsonObject != null)
		   			return jsonObject.optString(name);
			} catch (Exception e)
			{
			}
			return null;
		}
	
	/**
	 * 显示请求字符串
	 * 
	 * @param str
	 */
	public void logMsg(String str) {
		try {
			if (LocationResult != null)
				LocationResult.setText(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	


	/***
	 * Stop location service
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		locationService.unregisterListener(mListener); //注销掉监听
		locationService.stop(); //停止定位服务
		super.onStop();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// -----------location config ------------
		locationService = ((LocationApplication) getApplication()).locationService; 
		//获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		locationService.registerListener(mListener);
		//注册监听
		int type = getIntent().getIntExtra("from", 0);
		if (type == 0) {
			locationService.setLocationOption(locationService.getDefaultLocationClientOption());
		} else if (type == 1) {
			locationService.setLocationOption(locationService.getOption());
		}
		startLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (startLocation.getText().toString().equals(getString(R.string.startlocation))) {
					locationService.start();// 定位SDK
											// start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
					startLocation.setText(getString(R.string.stoplocation));
				} else {
					locationService.stop();
					startLocation.setText(getString(R.string.startlocation));
				}
			}
		});
	}

	
	//初始化数据
		private void initSQL() {
			// TODO Auto-generated method stub
			db = openOrCreateDatabase("gps.db", Context.MODE_PRIVATE, null);
			//db=SQLiteDatabase.openOrCreateDatabase("/data/data/com.HelloGoogleMaps/databases/gps.db",null);  
			//创建表SQL语句   
			String gps_table="create table gps_table(UniqueID text,Longitude1 text,latitude1 text,time text)";   
			//执行SQL语句   
			try {
				db.execSQL(gps_table);  
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("Thread"," " + 1);  
			}
			db.close();
			
		}
		
		//插入数据
		private void insert(String Longitude1,String latitude1,String UniqueID,String time ){   
			db = openOrCreateDatabase("gps.db", Context.MODE_PRIVATE, null);
			//实例化常量值   
			ContentValues cValue = new ContentValues();   
			//添加用户名   
			cValue.put("Longitude1",Longitude1);   
			//添加密码   
			cValue.put("latitude1",latitude1);  
			
			cValue.put("UniqueID",UniqueID); 
			
			cValue.put("time",time);
			//调用insert()方法插入数据   
			db.insert("gps_table",null,cValue);   
			}   
		
		//查询数据
		private void query() {   
			
			db = openOrCreateDatabase("gps.db", Context.MODE_PRIVATE, null);
			//查询获得游标   
			Cursor cursor = db.query ("gps_table",null,null,null,null,null,null);   
			//判断游标是否为空   
			while (cursor.moveToNext()) {  
				
			//获得地址
			String UniqueID=cursor.getString(0); 
			//获得经度   
			String Longitude1 = cursor.getString(1);   
			//获得纬度
			String latitude1=cursor.getString(2);   
			
			String time=cursor.getString(3);  
			 
			gpsList.add(new infoData(Longitude1,latitude1,UniqueID,time));
			 
			}  
			
			cursor.close();
			db.close();
		}  
		//删除数据
		private void drop() {
			// TODO Auto-generated method stub
			db = openOrCreateDatabase("gps.db", Context.MODE_PRIVATE, null);
			db.delete("gps_table", null, null);
			db.close();
		}
	
	/*****
	 * @see copy funtion to you project
	 * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
	 *
	 */
	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				StringBuffer sb = new StringBuffer(256);
				sb.append("time : ");
				/**
				 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
				 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
				 */
				CharSequence time=DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis());
				sb.append(time);
				sb.append("\nerror code : ");
				sb.append(location.getLocType());
				sb.append("\nlatitude : ");
				sb.append(location.getLatitude());
				sb.append("\nlontitude : ");
				sb.append(location.getLongitude());
				sb.append("\nradius : ");
				sb.append(location.getRadius());
				sb.append("\nCountryCode : ");
				sb.append(location.getCountryCode());
				sb.append("\nCountry : ");
				sb.append(location.getCountry());
				sb.append("\ncitycode : ");
				sb.append(location.getCityCode());
				sb.append("\ncity : ");
				sb.append(location.getCity());
				sb.append("\nDistrict : ");
				sb.append(location.getDistrict());
				sb.append("\nStreet : ");
				sb.append(location.getStreet());
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\nDescribe: ");
				sb.append(location.getLocationDescribe());
				sb.append("\nDirection(not all devices have value): ");
				sb.append(location.getDirection());
				sb.append("\nPoi: ");
				if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
					for (int i = 0; i < location.getPoiList().size(); i++) {
						Poi poi = (Poi) location.getPoiList().get(i);
						sb.append(poi.getName() + ";");
					}
				}
				if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
					sb.append("\nspeed : ");
					sb.append(location.getSpeed());// 单位：km/h
					sb.append("\nsatellite : ");
					sb.append(location.getSatelliteNumber());
					sb.append("\nheight : ");
					sb.append(location.getAltitude());// 单位：米
					sb.append("\ndescribe : ");
					sb.append("gps定位成功");
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
					// 运营商信息
					sb.append("\noperationers : ");
					sb.append(location.getOperators());
					sb.append("\ndescribe : ");
					sb.append("网络定位成功");
				} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
					sb.append("\ndescribe : ");
					sb.append("离线定位成功，离线定位结果也是有效的");
				} else if (location.getLocType() == BDLocation.TypeServerError) {
					sb.append("\ndescribe : ");
					sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
				} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
					sb.append("\ndescribe : ");
					sb.append("网络不同导致定位失败，请检查网络是否通畅");
				} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
					sb.append("\ndescribe : ");
					sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				}
				logMsg(sb.toString());
			    TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
				String szImei = TelephonyMgr.getDeviceId();
				insert(location.getLongitude()+"", location.getLatitude()+"", szImei,time.toString());
				
				
				
			}
		}

	};
	
	@TargetApi(23)
	private void getPersimmions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ArrayList<String> permissions = new ArrayList<String>();
			/***
			 * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
			 */
			// 定位精确位置
			if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
			}
			if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
			}
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
			// 读写权限
			if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
			}
			// 读取电话状态权限
			if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
				permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
			}
			
			if (permissions.size() > 0) {
				requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
			}
		}
	}

	@TargetApi(23)
	private boolean addPermission(ArrayList<String> permissionsList, String permission) {
		if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请	
			if (shouldShowRequestPermissionRationale(permission)){
				return true;
			}else{
				permissionsList.add(permission);
				return false;
			}
				
		}else{
			return true;
		}
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
	}
}
