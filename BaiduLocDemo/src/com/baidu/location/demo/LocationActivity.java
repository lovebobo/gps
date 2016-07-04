package com.baidu.location.demo;

import java.util.ArrayList;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.location.service.LocationService;
import com.baidu.location.service.infoData;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
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
	private LocationService locationService;
	private TextView LocationResult;
	private Button startLocation;
	private Button getLocation;
	
	private Handler mHandler = new Handler();  
    SQLiteDatabase db;
    private ArrayList<infoData> gpsList=new ArrayList<infoData>();
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// -----------demo view config ------------
		setContentView(R.layout.location);
		LocationResult = (TextView) findViewById(R.id.textView1);
		LocationResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		startLocation = (Button) findViewById(R.id.addfence);
		
		getLocation=(Button) findViewById(R.id.getData);
		
		getLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				query(db);
			}
		});
		initSQL();

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
		private void query(SQLiteDatabase db) {   
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
}
