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
	
	@Override
	public String toString() {
		String format = "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS] IP = %2$s, File = %3$s";
		String resultFileName = (resultFile != null && resultFile.exists()) ? getResultFile().getAbsolutePath() : "file not exist";
		return String.format(format, new Date(), hostName, ip, resultFileName);
	}

	public String getHost() {
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
