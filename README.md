# video_play_edit_sdk

###针对ffmpeg在android手机平台视频处理慢的情况:	
* 我们直接在ffmpeg的底层增加<硬解码模块>和<硬编码模块>,极大的加速了ffmpeg的解码和编码能力.
* 您可以像指定一个编码器,解码器一样,使用硬件编解码:

###完全兼容完整的ffmpeg命令,举例如下:
* 视频播放:  
*  普通播放:./ffplay testdemo.mp4  //采用ffmpeg自带的H264解码器
*  指定硬解码播放: ./ffplay -vcodec lansoh264_dec testdemo.mp4  //采用我们增加的lansoh264_dec硬件解码器
       
* 视频编码:
*  普通视频处理: ./ffmpeg -i testdemo.mp4 -vf format=gray -vcodec libx264 gray.mp4  //采用ffmpeg扩展的libx264编码器
*  指定硬编码处理: ./ffmpeg -i testdemo.mp4 -vf format=gray -vcodec lansoh264_enc gray.mp4  //采用我们增加的lansoh264_enc硬件编码器
    
    
* 注:调用流程是:java-->loadLibrary(加载库)-->调用native的C语言函数来实现.
  	 **java层演示代码详见:https://github.com/LanSoSdk/video_play_edit_sdk.
  	 