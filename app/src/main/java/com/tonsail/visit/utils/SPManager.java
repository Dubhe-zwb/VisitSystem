package com.tonsail.visit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.tonsail.visit.VisitApplication;


public class SPManager {

	private static final String SHARE_PREFERENCES = "visit_shared_preference";

	public static final String IS_RELEASE = "IS_RELEASE";

	private static SPManager instance;
	
	private SharedPreferences sp;
	
	private SPManager(){
		sp = getSharedPreferences();
	}
	
	private SharedPreferences getSharedPreferences(){
		if(sp==null){
			sp = VisitApplication.getApp().getSharedPreferences(SHARE_PREFERENCES, Context.MODE_PRIVATE);
		}
		return sp;
	}
	
	private Editor getEdit(){
		return getSharedPreferences().edit();
	}
	

	public static SPManager getInstance(){
		if(instance==null){
			synchronized (SPManager.class) {
				instance = new SPManager();
			}
		}

		return instance;
	}

	public void putInt(String key, int value){
		getEdit().putInt(key, value).commit();
	}
	
	public int getInt(String key, int defValue){
		return getSharedPreferences().getInt(key, defValue);
	}
	
	public void putLong(String key, long value){
		getEdit().putLong(key, value).commit();
	}
	
	public long getLong(String key, long defValue){
		return getSharedPreferences().getLong(key, defValue);
	}
	
	public void putFloat(String key, float value){
		getEdit().putFloat(key, value).commit();
	}
	
	public float getFloat(String key, float defValue){
		
		return getSharedPreferences().getFloat(key, defValue);
	}
	
	public void putString(String key, String value){
		getEdit().putString(key, value).commit();
	}
	
	public String getString(String key, String defValue){
		return getSharedPreferences().getString(key, defValue);
	}
	
	public void putBoolean(String key, boolean value){
		getEdit().putBoolean(key, value).commit();
	}
	
	public boolean getBoolean(String key, boolean defValue){
		return getSharedPreferences().getBoolean(key, defValue);
	}
	
	public void remove(String key){
		getEdit().remove(key).commit();
	}
	

}
