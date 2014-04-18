package com.asokorea;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHRunner implements Runnable {

	private JSch jsch;
	private HostVo host;
	private Session session;
	private Channel channel;
	private InputStream stdOut;
	private InputStream stdErr;
	private PrintStream shellStream;
	private boolean isRunning = false;
	private Path logPath;
	private int sessionTimeOut;
	
	private boolean autoExit = true;
	
	public synchronized void init(final JSch jsch, final HostVo host, final Path logPath, int sessionTimeOut) {
		this.jsch = jsch;
		this.host = host;
		this.logPath = logPath;
		this.sessionTimeOut = sessionTimeOut;
	}
	
	@Override
	public void run() {

		try {
			this.session = jsch.getSession(host.getUser(), host.getHost(), host.getPort());
			this.session.setConfig("StrictHostKeyChecking", "no");
			this.session.setPassword(host.getPass());
			this.session.connect(sessionTimeOut);
			
			this.channel = this.session.openChannel("shell");
			this.shellStream = new PrintStream(this.channel.getOutputStream(), true);
			this.stdOut = channel.getInputStream();
			this.stdErr = channel.getExtInputStream();
			this.channel.connect(sessionTimeOut);
			System.out.println(host.toString() + " : CONNECTED");
			
			isRunning = true;
			String data = "";
			
			for (String command : host.getCommands()) {
				shellStream.println(command.trim());
			}
			
			if(autoExit && (host.getCommands() == null || !"exit".equals(host.getCommands()[host.getCommands().length-1])))
			{
				shellStream.println("exit");
			}
			
			byte[] byteArray = new byte[1024 * 4];
			
			while(this.isRunning)
			{
				if(this.channel.isClosed())
				{
					break;
				}
				
				while(stdOut.available()>0)
				{
					int i = stdOut.read(byteArray, 0, byteArray.length);
					if( i < 0 ){
						break;
					}
					data += new String(byteArray, 0, i);
				}
				
				Thread.sleep(100);
			}

			shellStream.close();
			
			if(data != null && !"".equals(data.trim())){
				
				String regexp = "(hostname )(.+)";
				Pattern pattern = Pattern.compile(regexp);
				Matcher matcher = pattern.matcher(data);
				String hostName = null;
				Path resultPath = null;
				
				if(matcher.find()) {
					hostName = matcher.group(2);
					
					if(hostName != null && hostName.length() > 0){
						resultPath = logPath.resolve(hostName + ".txt");
					}else{
						resultPath = logPath.resolve(host.getHost() + ".err");
					}
					
					Files.write(resultPath, data.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}
				
				host.setHostName(hostName);
				host.setResultFile(resultPath.toFile());
				System.out.println("[RESULT] " + host.toString());
			}
			
			isRunning = false;
			data = null;
		} catch (IOException | InterruptedException | JSchException e) {
			e.printStackTrace(System.err);
		} finally {
			dispose();
		}
	}
	
	public void disconnect() {
		if(channel != null){
			channel.disconnect();
		}
		if(session != null){
			session.disconnect();
		}
		
		isRunning = false;
	}
	
	public void close() {
		try {
			if(stdOut != null) {
				stdOut.close();
			}
			if(stdErr != null) {
				stdErr.close();
			}
		} catch (IOException e) {
		}
	}

	public void dispose() {
		close();
		disconnect();
		
		this.channel = null;
		this.session = null;
		this.stdOut = null;
		this.stdErr = null;
	}

	public boolean isAutoExit() {
		return autoExit;
	}

	public void setAutoExit(boolean autoExit) {
		this.autoExit = autoExit;
	}

}
