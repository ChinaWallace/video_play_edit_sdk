# video_play_edit_sdk

###针对ffmpeg在android手机平台视频处理慢的情况:	
* 我们直接在ffmpeg的底层增加<硬解码模块>和<硬编码模块>,极大的加速了ffmpeg的解码和编码能力.
* 您可以像指定一个编码器,解码器一样,使用硬件编解码:

###兼容完整的ffmpeg命令,调用非常简单

*  只有一个方法: VideoEditor executeVideoEditor(String[] command);//传入的是ffmpeg的命令字符串.
*  举例详见VideoEditDemoActivity.java


###此SDK对个人免费,如商用或需要我们技术支持,定制各种功能需求开发, 收取稍许费用

###联系我们
QQ  1852600324

邮件  support@lansongtech.com

下载地址 https://github.com/LanSoSdk/video_play_edit_sdk

==========
看到很多人来问sdk是否可以实现某某个功能，再次说明下，这个sdk兼容完整的ffmpeg功能。以下我们就问的比较常用的功能列举一下:
####裁剪类型操作
*  分离，---提取/删除音频，提取/删除视频。
*  合成，---把音频和视频合成为一个文件，或把多个音频和一个视频合成。
*  裁剪，---类似剪刀剪切绳子一样，对音频/视频进行裁剪成多段。
*  拼接，---对多段视频进行合成，比如把多个mp4文件合成一个  

####视频编辑操作
*  视频转码，转格式等
*  视频画面缩放，镜像，提取，分离等。
*  多个视频画面叠加
*  多个画面并列
*  多个画面按照一定的时间或某个系数或逻辑关系来叠加，实现各种效果。
*  视频和图片的叠加
*  视频像素颜色处理，视频像素在画面中的搬移等。
*  提取视频的YUV，RGB各种平面等。

####音频处理
*  在把多个音频合并成一个前，对每个音频做音量高低音控制，压缩比等控制后在合并。

####字幕处理
*  增加字幕，字幕支持ass，srt，把字幕烧写到视频画面中
*  可以在视频画面中临时增加文字
####图片处理
*  快速的把所有视频帧提取为图片，采用opengl的形式
*  按照一定的逻辑关系来提取图片，比如每隔5秒提取一次
*  把多个图片合并成视频

####以上功能可以混合使用

####本SDK的编解码有:
- 软解码器H264,
- 硬件加速解码器lansoh264_dec
- 软编码器libx264
- 硬编码器lansoh265_enc
- 音频编码器libmp3lame

####更多ffmpeg的命令，大家可以参考 https://ffmpeg.org/documentation.html 来获取。
  	 
