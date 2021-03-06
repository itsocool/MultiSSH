package com.asokorea;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

public class HostVo {
	
	private String ip;
	private String hostName;
	private String user;
	private String pass;
	private int port = 22;
	private String[] commands;
	private File resultFile;
	
	public HostVo(String ip, String user, String pass){
		this(ip, user, pass, null, FileSystems.getDefault().getPath(".", "logs"), 0);
	}

	public HostVo(String ip, String user, String pass, String[] commands, Path resultPath, int port){
		this.ip = ip;
		this.user = user;
		this.pass = pass;
		this.port = (port > 0) ? port : this.port;
		this.commands = commands;
	}
	
	public String getTimeStamp(boolean includeDate) {
		String format = (includeDate) ? "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS" : "%1$tH:%1$tM:%1$tS";
		return String.format(format, new Date());
	}
	
	@Override
	public String toString() {
		String format = "\"timeStamp\":\"%s\", \"ip\":\"%s\", \"hostName\":\"%s\"";
		
		return String.format(format, getTimeStamp(true), ip, hostName);
	}
	
	public String getErrorMessage(String type, String message) {
		
		String result = null;
		String format = "{\"%s\":{\"ip\":\"%s\", \"message\":\"%s\"}}";
		
		if(message != null)
		{
			message = message.replaceAll("\"", "\\\"");
			message = message.replaceAll("\'", "\\\'");
		}
		result = String.format(format, type, ip, message);
		return result;
	}

	public String getConnectedMessage() {
		String result = null;
		String format = "{\"%s\":{\"ip\":\"%s\"}}";
		String mIp = (ip == null) ? "" : ip; 
		result = String.format(format, MessageType.CONNECTED, mIp);
		return result;
	}
	
	public String getCompleteMessage() {
		String result = null;
		String format = "{\"%s\":{\"ip\":\"%s\",\"hostName\":\"%s\",\"fileName\":\"%s\"}}";
		String mIp = (ip == null) ? "" : ip; 
		String mHostName = (hostName == null) ? "" : hostName; 
		String mResultFile = (resultFile == null || !resultFile.exists()) ? "" : resultFile.getName(); 
		result = String.format(format, MessageType.COMPLETE, mIp, mHostName, mResultFile);
		return result;
	}
	
	public String getIP() {
		return ip;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	public String[] getCommands() {
		return commands;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public File getResultFile() {
		return resultFile;
	}

	public void setResultFile(File resultFile) {
		this.resultFile = resultFile;
	}

}
