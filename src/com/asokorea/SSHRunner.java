package com.asokorea;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
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
			this.session = jsch.getSession(host.getUser(), host.getIP(), host.getPort());
			this.session.setConfig("StrictHostKeyChecking", "no");
			this.session.setPassword(host.getPass());
			this.session.connect(sessionTimeOut);
			
			this.channel = this.session.openChannel("shell");
			this.shellStream = new PrintStream(this.channel.getOutputStream(), true);
			this.stdOut = channel.getInputStream();
			this.stdErr = channel.getExtInputStream();
			this.channel.connect(sessionTimeOut);

			synchronized (System.out) {
				System.out.println(host.getConnectedMessage());	//INFO return connected
			}

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
				
				while (stdOut.available()>0)
				{
					int i = stdOut.read(byteArray, 0, byteArray.length);
					if( i < 0 ){
						break;
					}
					data += new String(byteArray, 0, i);
				}
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
					
					String format = "%s-%s-%3$tY%3$tm%3$td%3$tH%3$tM%3$tS";
					String fileName = String.format(format, host.getIP(), hostName, new Date());

					if(hostName != null && hostName.length() > 0){
						resultPath = logPath.resolve(fileName + ".txt");
					}else{
						resultPath = logPath.resolve(fileName + ".err");
					}
					
					Files.write(resultPath, data.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
					
					if(resultPath != null && resultPath.toFile() != null)
					{
						if(resultPath.toFile().exists() && resultPath.toFile().isFile() && resultPath.toFile().length() > 0){
							host.setResultFile(resultPath.toFile());
						}
					}
				}
				
				host.setHostName(hostName);
				
				synchronized (System.out) {
					System.out.println(host.getCompleteMessage());	//INFO return complete
				}
			}
			
			isRunning = false;
			data = null;
		} catch (JSchException | IOException e) {
			
			synchronized (System.err) {
				if(e.getMessage().indexOf("Connection refused") >= 0){
					System.err.println(host.getErrorMessage(MessageType.LOGIN_FAIL, e.getMessage()));	//INFO return loginfail
				}else if(e.getMessage().indexOf("timeout") >= 0) {
					System.err.println(host.getErrorMessage(MessageType.TIMEOUT, e.getMessage()));	//INFO return timeout
				}else{
					System.err.println(host.getErrorMessage(MessageType.SSH_ERROR, e.getMessage()));	//INFO return error
				}
			}
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
