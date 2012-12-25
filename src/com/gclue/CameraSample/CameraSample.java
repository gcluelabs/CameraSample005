package com.gclue.CameraSample;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

public class CameraSample extends Activity implements SensorEventListener, LocationListener {

	private MyView mView;

	/**
	 * Sensor Manager
	 */
	private SensorManager mSensorManager = null;
	/**
	 * Location Manager
	 */
	private LocationManager mLocationManager = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Notification Barを消す
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Title Barを消す
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// LocationManagerでGPSの値を取得するための設定
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 値が変化した際に呼び出されるリスナーの追加
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

		// Sensorの取得とリスナーへの登録
		List < Sensor > sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) {
			Sensor sensor = sensors.get(0);
			mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
		}

		CameraView mCamera = new CameraView(this);
		setContentView(mCamera);

		mView = new MyView(this);
		addContentView(mView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Log.i("SURFACE", "SensorChanged()");
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			Log.i("SURFACE", "yaw:" + sensorEvent.values[0]);
			Log.i("SURFACE", "picth:" + sensorEvent.values[1]);
			Log.i("SURFACE", "roll:" + sensorEvent.values[2]);
			mView.setOrientation("" + sensorEvent.values[0], "" + sensorEvent.values[1], "" + sensorEvent.values[2]);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.i("GPS", "lat=" + location.getLatitude());
		Log.i("GPS", "lon=" + location.getLongitude());
		mView.setGps("" + location.getLatitude(), "" + location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
	public void onDestroy() {
		super.onDestroy();
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
		}
	}
}

/**
 * CameraView
 */
class CameraView extends SurfaceView implements SurfaceHolder.Callback {
	/**
	 * Cameraのインスタンスを格納する変数
	 */
	private Camera mCamera;

	public CameraView(Context context) {
		super(context);
		getHolder().addCallback(this);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/**
	 * Surfaceに変化があった場合に呼ばれる
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i("CAMERA", "surfaceChaged");

		// 画面設定
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(width, height);
		mCamera.setParameters(parameters);

		// プレビュー表示を開始
		mCamera.startPreview();
	}

	/**
	 * Surfaceが生成された際に呼ばれる
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("CAMERA", "surfaceCreated");

		// カメラをOpen
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception e) {
		}
	}

	/**
	 * Surfaceが破棄された場合に呼ばれる
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("CAMERA", "surfaceDestroyed");

		// カメラをClose
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
}

/**
 * オーバーレイ描画用のクラス
 */
class MyView extends View {

	/**
	 * x座標
	 */
	private int x;

	/**
	 * y座標
	 */
	private int y;

	/**
	 * Roll
	 */
	private String roll;

	/**
	 * Yaw
	 */
	private String yaw;

	/**
	 * Pitch
	 */
	private String pitch;

	/**
	 * Lat
	 */
	private String lat;

	/**
	 * Lon
	 */
	private String lon;

	/**
	 * コンストラクタ
	 * 
	 * @param context
	 */
	public MyView(Context context) {
		super(context);
		setFocusable(true);
	}

	/**
	 * 値を渡す
	 */
	public void setOrientation(String yaw, String pitch, String roll) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
		invalidate();
	}

	/**
	 * 値を渡す(GPS)
	 */
	public void setGps(String lat, String lon) {
		this.lat = lat;
		this.lon = lon;
		invalidate();
	}

	/**
	 * 描画処理
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 背景色を設定
		canvas.drawColor(Color.TRANSPARENT);

		// 描画するための線の色を設定
		Paint mainPaint = new Paint();
		mainPaint.setStyle(Paint.Style.FILL);
		mainPaint.setARGB(255, 255, 255, 100);

		// 線で描画
		canvas.drawLine(x, y, 50, 50, mainPaint);

		// 文字を描画
		canvas.drawText("" + yaw, 10, 10, mainPaint);
		canvas.drawText("" + roll, 10, 30, mainPaint);
		canvas.drawText("" + pitch, 10, 50, mainPaint);

		canvas.drawText("" + lat, 10, 100, mainPaint);
		canvas.drawText("" + lon, 10, 120, mainPaint);
	}

	/**
	 * タッチイベント
	 */
	public boolean onTouchEvent(MotionEvent event) {

		// X,Y座標の取得
		x = (int) event.getX();
		y = (int) event.getY();

		// 再描画の指示
		invalidate();

		return true;
	}
}