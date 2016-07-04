package com.baidu.location.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Utility {

	
	//�������֪ͨ
	public static void sendMsg(final Handler handler, int code, Object obj) {
		final Message msg=handler.obtainMessage();
		msg.what = code;
		msg.obj = obj;
		handler.sendMessage(msg);
	}
	
	//�������֪ͨ,������url
	public static void sendMsg(final Handler handler, int code, Object obj,String url) {
		final Message msg=handler.obtainMessage();
		Bundle bundle=new Bundle();
		bundle.putString("url", url);
		msg.what = code;
		msg.obj = obj;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
	public static void sendMsg(final Handler handler, int code) {
		final Message msg=handler.obtainMessage();
		msg.what = code;
		handler.sendMessage(msg);
	}
	
	//������
	public static void sendMsg(final WeakReference<Handler> w_handler, int code) {
		Handler handler=w_handler.get();
		final Message msg=handler.obtainMessage();
		msg.what = code;
		handler.sendMessage(msg);
	}
	
	public static String toUtf8(String s)
	{
		String tmp = null;
		try
		{
			tmp = new String(s.getBytes(), "utf-8");
		} catch (Exception e)
		{
			System.out.println("to uft8 failed");
		}
		return tmp;
	}
	
	public static String streamToString(InputStream stream)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));  
            String data = "";  
            while ((data = br.readLine()) != null) 
            {  
                sb.append(data);  
            }  
            
            String result = sb.toString();
            return result;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static String streamToString1(InputStream stream)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(stream, "gb2312"));  
            String data = "";  
            while ((data = br.readLine()) != null) 
            {  
                sb.append(data);  
            }  
            
            String result = sb.toString();
            return result;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static String getRegex(String s, String e) // e�������ʾʽ��
	{
		Pattern pattern = Pattern.compile(e);    
		Matcher matcher = pattern.matcher(s);  
		if(matcher.find())
		{
			return matcher.toMatchResult().group(0).toString();
		}
		return null;
	}
}
