import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;


public class Spider {
	/*下面根据日期下载人民网上对应日期的所有资料
	 * url格式如下：
	 * http://paper.people.com.cn/rmrb/html/2013-03/06/nbs.D110000renmrb_01.htm
	 * 该url表示第1版的页面
	 * 匹配href=nbs.D110000renmrb即版面总数
	 * 匹配href=nw.D110000renmrb即某版面专题数*2
	 * 专题页面url：
	 * http://paper.people.com.cn/rmrb/html/2013-03/06/nw.D110000renmrb_20130306_1-01.htm?div=-1
	 * */
	//为每个版面生成一个子文件夹，为每个专题生成一个txt
	private String yearString;
	private String monthString;
	private String dayString;
	private String path;		//生成的最顶层文件夹的路径
	private String topicUrl;	//专题url前面一部分
	
	public Spider(String dir,int year,int month,int day) {
		yearString = "" + year;
		if(month < 10)
			monthString = "0" + month;
		else
			monthString = "" + month;
		if(day < 10)
			dayString = "0" + day;
		else
			dayString = "" + day;
		
		String fileName = yearString + monthString + dayString;
		
		path = dir + "\\" + fileName;
		
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("http://paper.people.com.cn/rmrb/html/");
		stringBuffer.append(year+"-"+monthString+"/"+dayString+"/nw.D110000renmrb_"+yearString+monthString+dayString);
		topicUrl = stringBuffer.toString();
	}
	public boolean Initial(){
		File file = new File(path);
		//在下载目录下生成对应日期的一个文件夹
		if(file.exists()){
			JOptionPane.showMessageDialog(null,"文件夹已存在","Warning!",JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if(!file.mkdir()){
			JOptionPane.showMessageDialog(null,"创建文件夹失败","Warning!",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	private int getNumberOfPages(){
		int pageNum = 0;
		StringBuffer contentBuffer = new StringBuffer();
		URL url = null;
		HttpURLConnection con = null;
		//生成一个正确的url
		String urlString = "http://paper.people.com.cn/rmrb/html/"+yearString+"-"+monthString+
				"/"+dayString+"/nbs.D110000renmrb_01.htm";
		
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			System.err.println("The URL is wrong!");
		}
		
		try {
			con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(60000);  
            con.setReadTimeout(60000); 
            
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String str;
            
            //get the source code
            while((str = input.readLine()) != null)
            	contentBuffer.append(str);
            input.close();
            
            String sourceCode = contentBuffer.toString();
            
            /*得到源代码后，通过匹配获得版块总数*/
            Pattern p = Pattern.compile("href=nbs.D110000renmrb");
            Matcher matcher = p.matcher(sourceCode);
            while(matcher.find())
            	pageNum++;
            
            
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			con.disconnect();
		}
		
		return pageNum;
	}
	
	private int getNumberOfTopics(int page){
		int doubleTopicNum = 0;
		StringBuffer contentBuffer = new StringBuffer();
		URL url = null;
		HttpURLConnection con = null;
		//生成一个正确的url
		String urlString = "http://paper.people.com.cn/rmrb/html/"+yearString+"-"+monthString+"/"+
		dayString+"/nbs.D110000renmrb_";
		
		if(page < 10)
			urlString = urlString + "0" + page + ".htm";
		else
			urlString = urlString + page + ".htm";
		//得到url
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			System.err.println("The URL is wrong!");
		}
		
		
		try {
			con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(60000);  
            con.setReadTimeout(60000); 
            
            //貌似页面请求的时候经常出错，会跳转到付费页面
            int response = con.getResponseCode();
            if(response > 400){
            	JOptionPane.showMessageDialog(null, "Error " + response +"\n这个网页多次访问貌似会跳转到付费页面\n导致无法继续获取内容" ,"Sorry",JOptionPane.OK_OPTION);
            	System.exit(-1);
            }
            
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String str;
            
            //get the source code
            while((str = input.readLine()) != null)
            	contentBuffer.append(str);
            input.close();
            
            String sourceCode = contentBuffer.toString();
            
            //得到源码后，通过匹配，获得专题数
            //TODO
            String matchingStr = "href=nw.D110000renmrb";
            /*int pos = 0;
            while(pos != -1){
            	pos = sourceCode.indexOf(matchingStr, pos);
            	if(pos != -1){
            		doubleTopicNum++;
            		pos = pos + matchingStr.length();
            	}
            }*/
            Pattern p = Pattern.compile("href=nw.D110000renmrb");
            Matcher matcher = p.matcher(sourceCode);
            while(matcher.find())
            	doubleTopicNum++;
            
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			con.disconnect();
		}
		
		return doubleTopicNum / 2;
	}
	
	private boolean makeFiles(int pageNum){
		//make pageNum files under this.path
		for(int i = 1;i <= pageNum; i++){
			File file = null;
			if(i < 10)
				file = new File(path + "\\" + "0" + i);
			else
				file = new File(path + "\\" + i);
			if(!file.mkdir())
				return false;
		}
		return true;
	}
	
	private boolean topicParser(int page,int topic){
		/*download the content of some page of some topic in a txt file*/
		String topicPath = null;	//topic的存储路径
		String topicPageUrl = null;	//topic的url字符串
		StringBuffer content = new StringBuffer();
		HttpURLConnection con = null;
		
		if(page < 10){
			topicPageUrl = topicUrl + "_" + topic + "-0" + page + ".htm?div=-1";
			topicPath = path + "\\" + "0" + page;
		}
		else{
			topicPageUrl = topicUrl + "_" + topic + "-" + page + ".htm?div=-1";
			topicPath = path + "\\" + page;
		}
		
		System.out.println(topicPageUrl);
		
		try {
			URL url = new URL(topicPageUrl);
			con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(60000);  
            con.setReadTimeout(60000); 
            
            //貌似页面请求的时候经常出错，会跳转到付费页面
            int response = con.getResponseCode();
            if(response > 400){
            	JOptionPane.showMessageDialog(null, "Error " + response +"\n这个网页多次访问貌似会跳转到付费页面\n导致无法继续获取内容" ,"Sorry",JOptionPane.OK_OPTION);
            	System.exit(-1);
            }
            
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String str;
            
            //get the source code
            while((str = input.readLine()) != null)
            	content.append(str);
            input.close();
            
            String sourceCode = content.toString();
            
            /*解析源代码内容，将正文保存在一个txt文件里*/
            /*标题在<h1></h1>标签中
             * 主要内容在<div class="c_c"></div>中
             * */
            String title = ""+ topic;
            Pattern pattern = Pattern.compile("<h1>.*</h1>");
            Matcher matcher = pattern.matcher(sourceCode);
            if(matcher.find()){
            	title = matcher.group();
            	//提取出标题内容
            	title = title.substring(4, title.length()-5);
            }
            //网页时utf-8的，windows的文件名编码是utf-16，出现乱码
            File file = new File(topicPath+"\\"+title+".txt");
            PrintWriter output = new PrintWriter(file);
            output.println(title);
            output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			con.disconnect();
		}
		
		return true;
	}
	
	public boolean beginDownload(){
		int pageNum = getNumberOfPages();
		if(!makeFiles(pageNum)){
			System.err.println("makeFiles error!");
			return false;
		}
		for(int i = 1;i <= pageNum;i++){
			int topicNum = getNumberOfTopics(i);
			for(int k = 1;k <= topicNum;k++){
				if(!topicParser(i, k)){
					System.err.println("topicParser error!");
					return false;
				}
			}
		}
		return true;
	}
}
