# video_play_edit_sdk
安卓平台,优化ffmpeg,增加硬件编码,硬件解码,速度更快, 全面支持ffmpeg的强大的视频编辑功能.手机端ffmpeg的视频处理不再慢.

##支持完整的ffmpeg的命令, 采用ffmpeg-2.8.6的版本, 您可以直接指定解码器.举例如下:
List<String> cmdList=new ArrayList<String>();
					cmdList.add("-i");
					cmdList.add("/sdcard/2x.mp4");
					cmdList.add("-vf");
					cmdList.add("format=gray");
					cmdList.add("-vcodec");
					cmdList.add("libx264");
					cmdList.add("-strict");
					cmdList.add("-2");
					cmdList.add("-y");
					cmdList.add("/sdcard/demo_gray.mp4");
				  String[] command=new String[cmdList.size()];  
         	     for(int i=0;i<cmdList.size();i++){  
         	    	 command[i]=(String)cmdList.get(i);  
         	     }  
 				SDLActivity.executeVideoEdit(command);
