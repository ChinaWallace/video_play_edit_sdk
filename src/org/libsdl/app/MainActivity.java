package org.libsdl.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import java.io.File; 

import android.os.Handler;
import android.content.Intent;
import android.view.View; 
import android.view.View.OnClickListener; 
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity { 

	EditText mEtPath;
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
			super.onCreate(savedInstanceState); 
			 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
			setContentView(R.layout.main_file); 
			mEtPath=(EditText)findViewById(R.id.id_file_etpath);
			mEtPath.setText("/sdcard/chongchukabuer.mp4");
			findViewById(R.id.id_start_video).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					startShowVideo();
				}
			});
	} 
	
	private void startShowVideo()
	{
		String  path=mEtPath.getText().toString();
		File file1=new File(path);
		
		if(file1.exists()&& path.contains(".mp4"))
		{
			Intent intent=new Intent(MainActivity.this,SDLActivity.class);
			intent.putExtra("FILE_PATH", path);
			Log.i("sno","------------start");
			startActivity(intent);
		}else{
			Toast.makeText(getApplicationContext(), "文件不存在", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				showHintDialog();
			}
		}, 500);
	}
	private void showHintDialog()
	{
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("注意:我们深入优化ffmpeg中的视频硬件解码,视频硬件编码, 您可以使用ffmpeg库的所有命令,如果指定使用硬件codec.请看SDLActivity.java代码.QQ1852600324 .")
        .setPositiveButton(android.R.string.ok, null)
        .show();
	}
	
	
} 