# video_play_edit_sdk

###针对ffmpeg在android手机平台视频处理慢的情况:	
* 我们直接在ffmpeg的底层增加<硬解码模块>和<硬编码模块>,极大的加速了ffmpeg的解码和编码能力.
* 您可以像指定一个编码器,解码器一样,使用硬件编解码:

###兼容完整的ffmpeg命令,调用非常简单

*  只有一个方法: VideoEditor executeVideoEditor(String[] command);//传入的是ffmpeg的命令字符串.
*  举例详见VideoEditDemoActivity.java

****
###联系我们
*  QQ1852600324
  Email:support@lansongtech.com
=======
  	 
