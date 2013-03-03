package com.example.calibration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.Plot;
import com.example.calibration.AccelsService.LocalBinder;


public class CalibrationActivity extends Activity{
	
    private final String DEBUG_TAG = CalibrationActivity.class.getSimpleName();
	private TextView mCountdownTv;
	private Button mStartButton;
	
	private final int countdownPeriod = 60; //3 Minutes
	private PowerManager mPm;
	private PowerManager.WakeLock mWakelock;
	private AccelsService mService;
	private boolean mBound;
	
	private Intent mIntent;
	private CountDownTimer mCountdown;
	
	private class myCountdown extends CountDownTimer{ //This counter will control the the 3-minute countdown 
		public myCountdown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		public void onTick(long millisUntilFinished) { //OnTick, we will update the display of the digital counter
			int minutes = (int) millisUntilFinished / (60*1000);
			int seconds = (int) (millisUntilFinished - minutes*60*1000)/1000;
			if (seconds >= 10){
				mCountdownTv.setText(minutes+":" + seconds);	
			}
			else{
				mCountdownTv.setText(minutes+":0" + seconds);
			}
	     }
	     public void onFinish() { 
	    	//When the countdown is finished, we will set the transactionStatus to be true and thus data will be stored  
			mCountdownTv.setText("Done!");
			mCountdown = null;
			Log.d(DEBUG_TAG,"Done Calibrating! With mBound = "+mBound);
			releaseResources(true);
			mStartButton.setEnabled(true); 
	     }	
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
        public void onServiceConnected(ComponentName className, IBinder iservice) {
        	LocalBinder binder = (LocalBinder) iservice;
            mService = binder.getService();
            mBound = true;
        }
		@Override
        public void onServiceDisconnected(ComponentName className) {
			mBound = false;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		        
		mCountdownTv = (TextView) findViewById(R.id.countdown_timer);
		mStartButton = (Button) findViewById(R.id.start_button);
	}
	@Override
	protected void onStart(){
		super.onStart();
		mStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					mPm = (PowerManager) getSystemService(CalibrationActivity.POWER_SERVICE);
					mWakelock = mPm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
					mWakelock.acquire();
				} catch (Exception ex) {
					Log.e("exception", "Acquiring WakeLock Failed");
				}
				mIntent = new Intent(CalibrationActivity.this, AccelsService.class);		
				bindService(mIntent,mConnection,Context.BIND_AUTO_CREATE);
				//Start to countdown 3 minutes and stop the service
				if (mCountdown==null){
					mCountdown = new myCountdown(1000*countdownPeriod, 1000);
					mCountdown.start();
				}
				mStartButton.setEnabled(false);//Disable the button after it's clicked
			}
		});
	}
	@Override
    protected void onStop() {
        super.onStop();
    }
	@Override
	public void onBackPressed() {
		if (mService != null && mService.getTransactionStatus()==false){
		    new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Closing Activity")
		        .setMessage("Are you sure you want to exit the session? All data will be lost!")
		        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
		    {
		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		        	releaseResources(false);
		            finish();    
		        }
	
		    })
		    .setNegativeButton("No", null)
		    .show();
		}
		else{
			finish();
			mWakelock.release();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_calibration, menu);
		return true;
	}
	private void releaseResources(boolean transactionStatus){
		if (mBound){// Unbind from the service
        	mService.setTransactionStatus(transactionStatus);
            unbindService(mConnection);
            if (mCountdown !=null){
            	mCountdown.cancel();
            }
            mBound = false;	
            mWakelock.release();
        }
	}
}




