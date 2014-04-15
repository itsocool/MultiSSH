package com.asokorea;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

public class HostVo {
	
	private String host;
	private String user;
	private String pass;
	private int port = 22;
	private String[] commands;
	private Path resultPath;
	private File resutFile;
	
	public HostVo(String host, String user, String pass){
		this(host, user, pass, null, FileSystems.getDefault().getPath(".", "logs"), 0);
	}

	public HostVo(String host, String user, String pass, String[] commands, Path resultPath, int port){
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.port = (port > 0) ? port : this.port;
		this.commands = commands;
	}
	
	@Override
	public String toString() {
		
		String format = "[%d] Host = %s, File = %s";
		File file = this.resutFile;
		String resultFileName = (file != null && file.exists()) ? file.getAbsolutePath() : "";
		return String.format(format, new Date().getTime(), this.host, resultFileName);
	}

	public String getHost() {
		return host;
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

	public File getResutFile() {
		return resutFile;
	}

	public Path getResultPath() {
		return resultPath;
	}

	public void setResultPath(Path resultPath) {
		this.resultPath = resultPath;
	}
}
