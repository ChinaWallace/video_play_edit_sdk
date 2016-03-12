# video_play_edit_sdk

###针对ffmpeg在android手机平台视频处理慢的情况:	
* 我们直接在ffmpeg的底层增加<硬解码模块>和<硬编码模块>,极大的加速了ffmpeg的解码和编码能力.
* 您可以像指定一个编码器,解码器一样,使用硬件编解码:

###兼容完整的ffmpeg命令,调用非常简单

*  只有一个方法: VideoEditor executeVideoEditor(String[] command);//传入的是ffmpeg的命令字符串.
*  举例如下(见VideoEditDemoActivity.java):

	 private void demoVideoGray()
		  {
			  List<String> cmdList=new ArrayList<String>();
		   	cmdList.add("-vcodec");
		   	cmdList.add("lansoh264_dec");  //使用我们的硬解码加速
				cmdList.add("-i");
				cmdList.add(videoPath);
				cmdList.add("-vf");
				cmdList.add("format=gray");
				cmdList.add("-vcodec");
				cmdList.add("lansoh264_enc"); //使用我们的硬编码加速
				cmdList.add("-strict");
				cmdList.add("-2");
				cmdList.add("-y");
				cmdList.add("/sdcard/video_demo_gray.mp4");
				String[] command=new String[cmdList.size()];  
		     for(int i=0;i<cmdList.size();i++){  
		    	 command[i]=(String)cmdList.get(i);  
		     }  
		     mVideoEditor.executeVideoEditor(command);
	  }
****
###联系我们
*  QQ1852600324
*  Email:support@lansongtech.com
=======
  	 