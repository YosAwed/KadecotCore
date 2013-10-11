package com.sonycsl.Kadecot.core;

import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.NotificationProcessor;
import com.sonycsl.Kadecot.call.RequestProcessor;
import com.sonycsl.Kadecot.server.KadecotServerService;
import com.sonycsl.Kadecot.server.ServerBinder;
import com.sonycsl.Kadecot.server.ServerSettings;

import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

public class KadecotCoreActivity extends FragmentActivity {
	@SuppressWarnings("unused")
	private static final String TAG = KadecotCoreActivity.class
			.getSimpleName();
	private final KadecotCoreActivity self = this;

	// layout
	private FrameLayout mMainLayout;
	private WebView mKadecotMyPage;
	private WebView mAppView;
	
	private KadecotCall mKadecotCall;

	// javascript interface(mKadecotMyPage & mAppView)
	private LocalCoreObject mLocal;
	// javascript interface(mKadecotMyPage)
	private ServerCall mServerCall;
	private UserApp mUserApp;
	// javascript interface(mAppView)
	private MyPageCall mMyPageCall;
	/*
	private ServerBinder mServerBinder;
	private ServiceConnection mServerConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mServerBinder = (ServerBinder)service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// KadecotServerServiceにbind
		Intent intent = new Intent(this, KadecotServerService.class);
		startService(intent);
		//bindService(intent, mServerConn, Context.BIND_AUTO_CREATE);
		
		mKadecotCall = new KadecotCall(this, new RequestProcessor(this, 0), new NotificationProcessor(this, 0)) {
			@Override
			public void send(JSONObject obj) {

				StringBuilder builder = new StringBuilder();
				//builder.append("if(\"kHAPI\" in window){kHAPI.net.callFromServer(");
				builder.append("if((\"kHAPI\" in window) && (\"net\" in kHAPI) && (\"callFromServer\" in kHAPI.net)){kHAPI.net.callFromServer(");
				builder.append("\"");
				// this in String,so we must espace double quotes.
				String jsonObjStr = obj.toString().replaceAll("\\\\", "\\\\\\\\");
				jsonObjStr = jsonObjStr.replaceAll("\\\"", "\\\\\"");
				builder.append(jsonObjStr);
				builder.append("\"");
				builder.append(");};");
				final String script = new String(builder);
				System.out.println(script);
				callJsOnKadecotMyPage(script);
			}
		};

		mLocal = new LocalCoreObject(self);
		
		setupLayout();
		
		startKadecot();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//unbindService(mServerConn);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mKadecotMyPage.resumeTimers();
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				mKadecotCall.start();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();

		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				mKadecotCall.stop();
			}
		});
		mKadecotMyPage.pauseTimers();
	}
	
	private void setupLayout() {
		// mMainLayout
		mMainLayout = new FrameLayout(this);
		this.setContentView(mMainLayout);
		
		// mKadecotMyPage
		mKadecotMyPage = new WebView(this);
		setupWebView(mKadecotMyPage);
				
		mKadecotMyPage.loadUrl("file:///android_asset/booting_page.html");
		
		mMainLayout.addView(mKadecotMyPage, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));
		
		// mAppView
		mAppView = new WebView(this);

		setupWebView(mAppView);
		mAppView.setVisibility(WebView.INVISIBLE);
		
		mMainLayout.addView(mAppView, 1, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));

		// JS interface
		mKadecotMyPage.addJavascriptInterface(getLocalObject(), "Local");
		mAppView.addJavascriptInterface(getLocalObject(), "Local");

		mServerCall = new ServerCall(this, getKadecotCall());
		mKadecotMyPage.addJavascriptInterface(mServerCall, "ServerCall");
		
		mUserApp = new UserApp(this);
		mKadecotMyPage.addJavascriptInterface(mUserApp, "UserApp");
		
		mMyPageCall = new MyPageCall(this);
		mAppView.addJavascriptInterface(mMyPageCall, "MyPageCall");

	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setupWebView(WebView webView) {
		webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		WebSettings wsets = webView.getSettings();
		wsets.setJavaScriptEnabled(true);
		wsets.setDomStorageEnabled(true);
		wsets.setDatabaseEnabled(true);
		wsets.setDatabasePath(getFilesDir() + "/localstorage");

		wsets.setGeolocationEnabled(true);
		webView.setBackgroundColor(Color.BLACK);

		webView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				return true;
			} // 長押し選択回避
		});

		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				Toast.makeText(self, message,
						Toast.LENGTH_SHORT).show();
				result.confirm();
				return true;
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				super.onGeolocationPermissionsShowPrompt(origin, callback);
				callback.invoke(origin, true, false);
			}

		});

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				view.requestFocus(View.FOCUS_DOWN);
			}

		});

		wsets.setCacheMode(WebSettings.LOAD_NO_CACHE);
		wsets.setAppCacheEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.activity_kadecot_core, menu);
		//return true;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
	    if(e.getKeyCode() == KeyEvent.KEYCODE_BACK && e.getAction() == KeyEvent.ACTION_DOWN) {
	    	callJsOnKadecotMyPage("kHAPI.onBackBtn()") ;
	    	return true ;
	    }
	    return super.dispatchKeyEvent(e);
	}
	
	protected void startKadecot() {
		loadUrlOnKadecotMyPage("file:///android_asset/html/index-t.html");
	}
	
	public WebView getKadecotMyPage() {
		return mKadecotMyPage;
	}
	
	public WebView getAppView() {
		return mAppView;
	}

	public void loadUrlOnKadecotMyPage(final String url) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mKadecotMyPage.loadUrl(url);
			}
		});
	}

	public void loadUrlOnAppView(final String url) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//System.out.println("url : "+url) ;
				mAppView.loadUrl(url);
			}
		});
	}
	public void callJsOnKadecotMyPage(final String cmd) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mKadecotMyPage.loadUrl("javascript:" + cmd);
			}
		});
	}

	public void callJsOnAppView(final String cmd) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAppView.loadUrl("javascript:" + cmd);
			}
		});
	}
	
	protected LocalCoreObject getLocalObject() {
		return mLocal;
	}
	
	protected KadecotCall getKadecotCall() {
		return mKadecotCall;
	}

}
