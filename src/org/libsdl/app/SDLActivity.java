package org.libsdl.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.reflect.Method;


import android.app.*;
import android.content.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.os.*;
import android.util.Log;
import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;

/**
    SDL Activity
    
*/
//针对ffmpeg在android手机平台视频处理慢的情况:	
//我们直接在ffmpeg的底层增加<硬解码模块>和<硬编码模块>,极大的加速了ffmpeg的解码和编码能力.
//您可以像指定一个编码器,解码器一样,使用硬件编解码:
//
//完全兼容完整的ffmpeg命令,举例如下:
//视频播放:  
//       普通播放:./ffplay testdemo.mp4  //采用ffmpeg自带的H264解码器
//       指定硬解码播放: ./ffplay -vcodec lansoh264_dec testdemo.mp4  //采用我们增加的lansoh264_dec硬件解码器
//       
//视频编码:
//    普通视频处理: ./ffmpeg -i testdemo.mp4 -vf format=gray -vcodec libx264 gray.mp4  //采用ffmpeg扩展的libx264编码器
//    指定硬编码处理: ./ffmpeg -i testdemo.mp4 -vf format=gray -vcodec lansoh264_enc gray.mp4  //采用我们增加的lansoh264_enc硬件编码器
//    
//    
//  注:调用流程是:java-->loadLibrary(加载库)-->调用native的C语言函数来实现.
//  	 java层演示代码详见:https://github.com/LanSoSdk/video_play_edit_sdk.
  		 
public class SDLActivity extends Activity implements OnClickListener,  OnCompletionListener {
    private static final String TAG = "SDL";

    // Keep track of the paused state
    public static boolean  mIsSurfaceReady, mHasFocus;

    protected static SDLActivity mSingleton;
    protected static SDLSurfaceView mSurface;
    protected static View mTextEdit;
    protected static String[] args;

    // This is what SDL runs in. It invokes SDL_main(), eventually
    protected static Thread mSDLThread;
    
    // Audio
    protected static AudioTrack mAudioTrack;

    protected String[] getLibraries() {
        return new String[] {
        	"lsffmpeg",
            "SDL2",
            "main"
        };
    }

    // Load the .so
    public void loadLibraries() {
       for (String lib : getLibraries()) {
          System.loadLibrary(lib);
       }
    }
    
    protected String[] getArguments() {
        return args;
    }
    
    public static void initialize() {
        mSingleton = null;
        mSurface = null;
        mTextEdit = null;
        mSDLThread = null;
        mAudioTrack = null;
        mIsSurfaceReady = false;
        mHasFocus = true;
    }
    String videoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
		SDLActivity.initialize();
        mSingleton = this;
		args = new String[5];
		Intent i = getIntent();
		args[0]="-vcodec";
		//args[1]="h264";  //<-----采用ffmpeg自带的H264解码器,
		args[1]="lansoh264_dec";  //<--------------采用我们优化的硬件加速解码器.
        videoPath = i.getStringExtra("FILE_PATH");
        
        args[2]=videoPath;
        try {
            loadLibraries();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_player);
        
        mSurface=(SDLSurfaceView)findViewById(R.id.id_video_sdlsurface);
        
	    findViewById(R.id.id_btnstart).setOnClickListener(this);
	    findViewById(R.id.id_btnpause).setOnClickListener(this);
	    findViewById(R.id.id_btnplay).setOnClickListener(this);
	    findViewById(R.id.id_btnstop).setOnClickListener(this);
	    findViewById(R.id.id_btnback100ms).setOnClickListener(this);
	    findViewById(R.id.id_btnfront100ms).setOnClickListener(this);

	    findViewById(R.id.id_do_ffmpeg_cmd).setOnClickListener(this);
	
    }
    @Override
    protected void onPause() {
        super.onPause();
        SDLActivity.stopVideoPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				testCmd();
			}
		}, 1000);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        SDLActivity.nativeLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SDLActivity.nativeQuit();
        Log.i("sno","native quit...");
    }
    float mTimePerCent=(float) 0.1;
    int timeMs=1000;  //1s
    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	switch (v.getId()) {
			case R.id.id_btnpause:
				SDLActivity.videoPause();
				break;
			case R.id.id_btnstart:
				  SDLActivity.startVideoPlay();
				break;
			case R.id.id_btnplay:
				SDLActivity.videoPlay();
				break;
			case R.id.id_btnstop:
				  SDLActivity.stopVideoPlay();
				break;	
			case R.id.id_btnback100ms:
					SDLActivity.videoSeekBack100Ms();
				break;
			case R.id.id_btnfront100ms:
					SDLActivity.videoSeekFront100Ms();
				break;
			case R.id.id_do_ffmpeg_cmd:
					List<String> cmdList=new ArrayList<String>();
					cmdList.add("-i");
					cmdList.add(videoPath);
					cmdList.add("-vf");
					cmdList.add("format=gray");
					cmdList.add("-vcodec");
					cmdList.add("lansoh264_enc");
					cmdList.add("-strict");
					cmdList.add("-2");
					cmdList.add("-y");
					cmdList.add("/sdcard/demo_gray.mp4");
				  String[] command=new String[cmdList.size()];  
         	     for(int i=0;i<cmdList.size();i++){  
         	    	 command[i]=(String)cmdList.get(i);  
         	     }  
 				SDLActivity.executeVideoEdit(command);
				//./ffmpeg -i fashion1.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate1.ts
				break;
			default:
				break;
		}
    }
    public void testCmd()
    {
    	 SDLActivity.startVideoPlay();
//    	List<String> cmdList=new ArrayList<String>();
//		cmdList.add("-i");
//		cmdList.add("/sdcard/tenSecond.mp4");
//		cmdList.add("-vf");
//		cmdList.add("format=gray");
//		cmdList.add("-vcodec");
//		cmdList.add("lansoh264_enc");
//		cmdList.add("-strict");
//		cmdList.add("-2");
//		cmdList.add("-y");
//		cmdList.add("/sdcard/demo_gray66.mp4");
//		String[] command=new String[cmdList.size()];  
//	     for(int i=0;i<cmdList.size();i++){  
//	    	 command[i]=(String)cmdList.get(i);  
//	     }  
//		SDLActivity.executeVideoEdit(command);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    	// TODO Auto-generated method stub
    	
    }
    
    
    static void startVideoPlay()
    {
    	  if (SDLActivity.mSDLThread == null) 
    	  {
              final Thread sdlThread = new Thread(new SDLMain(), "SDLThread");
              sdlThread.start();
              SDLActivity.mSDLThread = new Thread(new Runnable(){
                  @Override
                  public void run(){
                      try {
                          sdlThread.join();
                          Log.i("sno","java test main exit!!!");
                      }catch(Exception e){}
                      finally{
                    	  SDLActivity.mSDLThread=null;
                      }
                  }
              }, "SDLThreadListener");
              SDLActivity.mSDLThread.start();
          }
    }
    static void stopVideoPlay()
    {
    	 SDLActivity.videoStop();
         if (SDLActivity.mSDLThread != null) {
             try {
                 SDLActivity.mSDLThread.join();
             } catch(Exception e) {
                 Log.i("SDL", "Problem stopping thread: " + e);
             } finally{
            	 SDLActivity.mSDLThread=null;
             }
         }
    }

    static final int COMMAND_CHANGE_TITLE = 1;
    static final int COMMAND_UNUSED = 2;
    static final int COMMAND_TEXTEDIT_HIDE = 3;
    static final int COMMAND_SET_KEEP_SCREEN_ON = 5;

    protected static final int COMMAND_USER = 0x8000;

    static final int MSG_VIDEO_PLAYING=501;
    static final int MSG_VIDEO_STOPED= 502;
    static final int MSG_VIDEO_PAUSE= 503;
    static final int MSG_VIDEO_REACH_END= 504;
    static final int MSG_VIDEO_PRECISIONSEEK_OK= 505;
    
    
    static final int MSG_VIDEO_PLAY_ERROR= 601;
    
    
    /**
     * This method is called by SDL if SDL did not handle a message itself.
     * This happens if a received message contains an unsupported command.
     * Method can be overwritten to handle Messages in a different class.
     * @param command the command of the message.
     * @param param the parameter of the message. May be null.
     * @return if the message was handled in overridden method.
     */
    protected boolean onUnhandledMessage(int command, Object param) {
        return false;
    }

    /**
     * A Handler class for Messages from native SDL applications.
     * It uses current Activities as target (e.g. for the title).
     * static to prevent implicit references to enclosing object.
     */
    protected static class SDLCommandHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Context context = getContext();
            if (context == null) {
                Log.e(TAG, "error handling message, getContext() returned null");
                return;
            }
            switch (msg.arg1) {
            case MSG_VIDEO_PLAYING:
            	// Log.i(TAG, "MSG_VIDEO_PLAYING " + SDLActivity.videoGetDuration());
            	// seekbarPrecision.setMax(SDLActivity.videoGetDuration());
            	break;
            case MSG_VIDEO_STOPED:
            	break;
            case MSG_VIDEO_PAUSE:
            	
            	break;
            case MSG_VIDEO_PRECISIONSEEK_OK:
                if (msg.obj instanceof Integer) {
                	  Log.i(TAG, "error handling message, command is " + ((Integer) msg.obj).intValue());
                	  int value=((Integer) msg.obj).intValue();
                	  float pos=(float)value;
  					pos/=1000;
                }
            	break;
            default:
            }
        }
    }

    // Handler for the messages
    Handler commandHandler = new SDLCommandHandler();

    // Send a message from the SDLMain thread
    boolean sendCommand(int command, Object data) {
        Message msg = commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        return commandHandler.sendMessage(msg);
    }

    // C functions we call
	public static native String stringFromJNI();
	public static native int ffInit();
    public static native int nativeInit(Object arguments);  //重要的init
    
    public static native void nativeLowMemory();
    public static native void nativeQuit();
    public static native void nativePause();
    public static native void nativeResume();
    public static native void onNativeResize(int x, int y, int format);
    public static native int onNativePadDown(int device_id, int keycode);
    public static native int onNativePadUp(int device_id, int keycode);
    public static native void onNativeJoy(int device_id, int axis,
                                          float value);
    public static native void onNativeHat(int device_id, int hat_id,
                                          int x, int y);
    public static native void onNativeKeyDown(int keycode);
    public static native void onNativeKeyUp(int keycode);
    public static native void onNativeKeyboardFocusLost();
    public static native void onNativeTouch(int touchDevId, int pointerFingerId,
                                            int action, float x, 
                                            float y, float p);
    public static native void onNativeAccel(float x, float y, float z);
    public static native void onNativeSurfaceChanged();
    public static native void onNativeSurfaceDestroyed();
    
    public static native void nativeFlipBuffers();
    
    public static native int nativeAddJoystick(int device_id, String name, 
                                               int is_accelerometer, int nbuttons, 
                                               int naxes, int nhats, int nballs);
    public static native int nativeRemoveJoystick(int device_id);
    public static native String nativeGetHint(String name);

    
    //lansosdk++
    public static native int  videoStart(Object arguments);
    public static native void videoStop();
    public static native void videoPause();
    public static native void videoPlay();
    public static native void videoSeekByPercent(float percent,boolean isPause);
    public static native void videoSeekByTime(int ms,boolean isPause);
    public static native void videoPrecisionSeek(int ms);
    public static native int videoGetDuration();
    public static native int videoSeekBack100Ms();
    public static native int videoSeekFront100Ms();
    public static native int executeVideoEdit(Object arguments);
    
    /**
     * This method is called by SDL using JNI.
     */
    public static void flipBuffers() {
        SDLActivity.nativeFlipBuffers();
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean setActivityTitle(String title) {
        // Called from SDLMain() thread and can't directly affect the view
        return mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean sendMessage(int command, int param) {
        return mSingleton.sendCommand(command, Integer.valueOf(param));
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static Context getContext() {
        return mSingleton;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static Surface getNativeSurface() {
        return SDLActivity.mSurface.getNativeSurface();
    }

    // Audio

    /**
     * This method is called by SDL using JNI.
     */
    public static int audioInit(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);
        
        Log.v("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit") + " " + (sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");
        
        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);
        
        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);
            
            // Instantiating AudioTrack can "succeed" without an exception and the track may still be invalid
            // Ref: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
            // Ref: http://developer.android.com/reference/android/media/AudioTrack.html#getState()
            if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e("SDL", "Failed during initialization of Audio Track");
                mAudioTrack = null;
                return -1;
            }
            
            mAudioTrack.play();
        }
       
        Log.v("SDL", "SDL audio: got " + ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono") + " " + ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " " + (mAudioTrack.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");
        
        return 0;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioWriteShortBuffer(short[] buffer) {
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(short)");
                return;
            }
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioWriteByteBuffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(byte)");
                return;
            }
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioQuit() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }

     /** com.android.vending.expansion.zipfile.ZipResourceFile object or null. */
    private Object expansionFile;

    /** com.android.vending.expansion.zipfile.ZipResourceFile's getInputStream() or null. */
    private Method expansionFileMethod;

    /**
     * This method is called by SDL using JNI.
     */
    public InputStream openAPKExtensionInputStream(String fileName) throws IOException {
        // Get a ZipResourceFile representing a merger of both the main and patch files
        if (expansionFile == null) {
            Integer mainVersion = Integer.valueOf(nativeGetHint("SDL_ANDROID_APK_EXPANSION_MAIN_FILE_VERSION"));
            Integer patchVersion = Integer.valueOf(nativeGetHint("SDL_ANDROID_APK_EXPANSION_PATCH_FILE_VERSION"));

            try {
                // To avoid direct dependency on Google APK extension library that is
                // not a part of Android SDK we access it using reflection
                expansionFile = Class.forName("com.android.vending.expansion.zipfile.APKExpansionSupport")
                    .getMethod("getAPKExpansionZipFile", Context.class, int.class, int.class)
                    .invoke(null, this, mainVersion, patchVersion);

                expansionFileMethod = expansionFile.getClass()
                    .getMethod("getInputStream", String.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                expansionFile = null;
                expansionFileMethod = null;
            }
        }

        // Get an input stream for a known file inside the expansion file ZIPs
        InputStream fileStream;
        try {
            fileStream = (InputStream)expansionFileMethod.invoke(expansionFile, fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            fileStream = null;
        }

        if (fileStream == null) {
            throw new IOException();
        }

        return fileStream;
    }

    // Messagebox

    /** Result of current messagebox. Also used for blocking the calling thread. */
    protected final int[] messageboxSelection = new int[1];

    /** Id of current dialog. */
    protected int dialogs = 0;

    /**
     * This method is called by SDL using JNI.
     * Shows the messagebox from UI thread and block calling thread.
     * buttonFlags, buttonIds and buttonTexts must have same length.
     * @param buttonFlags array containing flags for every button.
     * @param buttonIds array containing id for every button.
     * @param buttonTexts array containing text for every button.
     * @param colors null for default or array of length 5 containing colors.
     * @return button id or -1.
     */
    public int messageboxShowMessageBox(
            final int flags,
            final String title,
            final String message,
            final int[] buttonFlags,
            final int[] buttonIds,
            final String[] buttonTexts,
            final int[] colors) {
    	return -1;
    }
}

	/**
	    Simple nativeInit() runnable
	*/
	class SDLMain implements Runnable {
	    @Override
	    public void run() {
	        SDLActivity.videoStart(SDLActivity.mSingleton.getArguments());
	    }
	}


