package com.baidu.location.service;

import java.io.InputStream;

public abstract class AbsHttpTask
{
	
	public abstract void onComplete(InputStream paramInputStream);
	
	public abstract void onError();
	
	public abstract void onError(Object msg);
}

