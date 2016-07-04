package com.baidu.location.service;

public class infoData {

	private String Longitude1;
	
	private String latitude1;
	
	private String UniqueID;
	
	private String time;

	

	

	public infoData(String Longitude1,String latitude1,String UniqueID,String time)
	{
		this.Longitude1=Longitude1;
		this.latitude1=latitude1;
		this.UniqueID=UniqueID;
		this.time=time;
		
	}
	
	public String getLongitude1() {
		return Longitude1;
	}

	public void setLongitude1(String longitude1) {
		Longitude1 = longitude1;
	}

	public String getLatitude1() {
		return latitude1;
	}

	public void setLatitude1(String latitude1) {
		this.latitude1 = latitude1;
	}

	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public String getUniqueID() {
		return UniqueID;
	}

	public void setUniqueID(String uniqueID) {
		UniqueID = uniqueID;
	}
}
