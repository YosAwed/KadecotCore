package com.sonycsl.Kadecot.core;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;

import com.sonycsl.Kadecot.utils.FileUtils;

/**
 * Kadecot My Page用のJSインターフェース
 *
 */
public class LocalCoreObject {
	@SuppressWarnings("unused")
	private static final String TAG = LocalCoreObject.class.getSimpleName();
	private final LocalCoreObject self = this;
	
	protected final KadecotCoreActivity mKadecot;
	private SoundPool mSoundPool = null;
	
	public LocalCoreObject(KadecotCoreActivity kadecot) {
		mKadecot = kadecot;
	}
	public void startKadecot(){
		mKadecot.startKadecot() ;
	}
	public void openWebBrowser(final String url) {
		mKadecot.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mKadecot.startActivity(new Intent(Intent.ACTION_VIEW , Uri.parse(url)));
			}
		});
	}
	// Audio
	
	public void playAudio(String path){
		if( path.startsWith("file://") )
			path = path.substring("file://".length()) ;
		stopAudio(path) ;

		mSoundPool = new SoundPool(1,AudioManager.STREAM_MUSIC,0) ;
		int sid = mSoundPool.load(path,1) ;
		while(mSoundPool.play(sid, 1.0F, 1.0F,0,0,1.0F)==0){
			try {
				Thread.sleep(100) ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void stopAudio(String path){
		if( mSoundPool == null ) return ;
		mSoundPool.release() ;
		mSoundPool = null ;
	}
}
