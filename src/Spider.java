import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;


public class Spider {
	/*����������������������϶�Ӧ���ڵ���������
	 * url��ʽ���£�
	 * http://paper.people.com.cn/rmrb/html/2013-03/06/nbs.D110000renmrb_01.htm
	 * ��url��ʾ��1���ҳ��
	 * ƥ��href=nbs.D110000renmrb����������
	 * ƥ��href=nw.D110000renmrb��ĳ����ר����*2
	 * ר��ҳ��url��
	 * http://paper.people.com.cn/rmrb/html/2013-03/06/nw.D110000renmrb_20130306_1-01.htm?div=-1
	 * */
	
	//Ϊÿ����������һ�����ļ��У�Ϊÿ��ר������һ��txt
	private String yearString;
	private String monthString;
	private String dayString;
	private String path;		//���ɵ�����ļ��е�·��
	private String topicUrl;	//ר��urlǰ��һ����
	
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
		//������Ŀ¼�����ɶ�Ӧ���ڵ�һ���ļ���
		if(file.exists()){
			JOptionPane.showMessageDialog(null,"�ļ����Ѵ���","Warning!",JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if(!file.mkdir()){
			JOptionPane.showMessageDialog(null,"�����ļ���ʧ��","Warning!",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	private String getSourceCode(URL url){
		HttpURLConnection con = null;
		String sourceCode = null;
		StringBuffer contentBuffer = new StringBuffer();
		try {
			con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(60000);  
            con.setReadTimeout(60000); 
            
          //ò��ҳ�������ʱ�򾭳���������ת������ҳ��
            int response = con.getResponseCode();
            if(response > 400){
            	JOptionPane.showMessageDialog(null, "Error " + response +"\n����֮ǰ���ݻ���ת������ҳ��\n�����޷�������ȡ����" ,"Sorry",JOptionPane.OK_OPTION);
            	System.exit(-1);
            }
           /* while(response > 400){
            	con.disconnect();
            	System.out.println("reconnect...");
            	try {
					Thread.sleep(2000);
					con = (HttpURLConnection)url.openConnection();
	    			con.setConnectTimeout(60000);  
	                con.setReadTimeout(60000); 
	                response = con.getResponseCode();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }*/
            
            //ע�����
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
            String str;
            
            //get the source code
            while((str = input.readLine()) != null)
            	contentBuffer.append(str);
            input.close();
            
           sourceCode = contentBuffer.toString();
           
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			con.disconnect();
		}
		
		return sourceCode;
	}
	
	private int getNumberOfPages(){
		int pageNum = 0;
		URL url = null;
		//����һ����ȷ��url
		String urlString = "http://paper.people.com.cn/rmrb/html/"+yearString+"-"+monthString+
				"/"+dayString+"/nbs.D110000renmrb_01.htm";
		
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			System.err.println("The URL is wrong!");
		}
		
		String sourceCode = getSourceCode(url);
		
		 /*�õ�Դ�����ͨ��ƥ���ð������*/
        Pattern p = Pattern.compile("href=nbs.D110000renmrb");
        Matcher matcher = p.matcher(sourceCode);
        while(matcher.find())
        	pageNum++;
		
		return pageNum;
	}
	
	private int getNumberOfTopics(int page){
		int doubleTopicNum = 0;
		URL url = null;
		//����һ����ȷ��url
		String urlString = "http://paper.people.com.cn/rmrb/html/"+yearString+"-"+monthString+"/"+
		dayString+"/nbs.D110000renmrb_";
		
		if(page < 10)
			urlString = urlString + "0" + page + ".htm";
		else
			urlString = urlString + page + ".htm";
		//�õ�url
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			System.err.println("The URL is wrong!");
		}
		
		String sourceCode = getSourceCode(url);
		
		 //�õ�Դ���ͨ��ƥ�䣬���ר����
        String matchingStr = "href=nw.D110000renmrb";
        Pattern p = Pattern.compile(matchingStr);
        Matcher matcher = p.matcher(sourceCode);
        while(matcher.find())
        	doubleTopicNum++;
		
		return doubleTopicNum / 2;
	}
	
	private boolean makeFile(String path){
		//make pageNum file under this.path
		File file = new File(path);
		if(!file.mkdir())
			return false;
		
		return true;
	}
	
	private boolean topicParser(int page,int topic){
		/*download the content of some page of some topic in a txt file*/
		String topicPath = null;	//topic�Ĵ洢·��
		String topicPageUrl = null;	//topic��url�ַ���
		
		if(page < 10){
			topicPageUrl = topicUrl + "_" + topic + "-0" + page + ".htm?div=-1";
			topicPath = path + "\\" + "0" + page;
		}
		else{
			topicPageUrl = topicUrl + "_" + topic + "-" + page + ".htm?div=-1";
			topicPath = path + "\\" + page;
		}
		
		System.out.println(topicPageUrl);
		
		URL url = null;
		try {
			url = new URL(topicPageUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		String sourceCode = getSourceCode(url);
		
		/*����Դ�������ݣ������ı�����һ��txt�ļ���
         *������<h1></h1>��ǩ��
         *��Ҫ������<div class="c_c"></div>��
         *������<!--enpcontent--><!--/enpcontent-->��
         * */
        String title = null;
        Pattern pattern = Pattern.compile("<h1>.*</h1>");
        Matcher matcher = pattern.matcher(sourceCode);
        if(matcher.find()){
        	title = matcher.group();
        	//��ȡ����������
        	title = title.substring(4, title.length()-5);
        }
        title = title.replaceAll("<BR/>", " ").trim();
        System.out.println(title);
        
        topicPath += "\\" + title;
        makeFile(topicPath);
        topicPath += "\\";
        //��������ȡ����
        File contentFile = new File(topicPath+"content.txt");
        PrintWriter output;
		try {
			output = new PrintWriter(contentFile);
			 //��������ƥ��
	        pattern = Pattern.compile("<!--enpcontent-->.*?<!--/enpcontent-->");
	        matcher = pattern.matcher(sourceCode);
	        String content = "";
	        if(matcher.find()){
	        	content = matcher.group();
	        	content = content.substring(17, content.length()-18);
	        	content = content.replaceAll("<P>","");
	        	content = content.replaceAll("</P>", "\r\n");
	        	content = content.replaceAll("&nbsp;", " ");
	        }
	        output.write(content);
	        output.close();
	        System.out.println("content downloaded");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//��ͼƬ����������,topicPath+"xxx.jpg"
		//TODO
		
		return true;
	}
	
	public boolean beginDownload(){
		int pageNum = getNumberOfPages();
		
		for(int i = 1;i <= pageNum;i++){
			int topicNum = getNumberOfTopics(i);
			String filePathString = null;
			if(i < 10)
				filePathString = path + "\\" + "0" + i;
			else
				filePathString = path + "\\" + i;
			
			if(!makeFile(filePathString)){
				System.err.println("fail to make file!");
				System.exit(-1);
			}
			
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
