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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHRunner implements Runnable {

	public HostVo host;
	public Session session;
	public Channel channel;
	public InputStream stdOut;
	public InputStream stdErr;
	public PrintStream shellStream;
	public boolean isRunning = false;
	private int sessionTimeOut = 3000;
	
	public SSHRunner(final Session session, final HostVo host) throws JSchException, IOException{
		this.host = host;
		this.session = session;
		this.session.setConfig("StrictHostKeyChecking", "no");
		this.session.setTimeout(sessionTimeOut);
		this.channel = this.session.openChannel("shell");
	}
	
	@Override
	public void run() {
		try {
			
			isRunning = true;
			String data = "";
			
			try {
				this.shellStream = new PrintStream(this.channel.getOutputStream(), true);
				this.channel.connect();
				this.stdOut = channel.getInputStream();
				this.stdErr = channel.getExtInputStream();
				
				for (String command : host.getCommands()) {
					shellStream.println(command.trim());
				}

				if(host.getCommands() == null || !"exit".equals(host.getCommands()[host.getCommands().length-1]))
				{
					shellStream.println("exit");
				}
				
				shellStream.close();
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

				String fileName = "";

				if(data != null && !"".equals(data.trim())){
					
					Pattern p = Pattern.compile("(^hostname )(.+)");
					Matcher m = p.matcher(data);
					Path path = host.getResultPath();
					
					if(m != null && m.groupCount() > 0 && m.group(2) != null){
						path = path.resolve(m.group(2) + ".txt");
					}else{
						path = path.resolve(host.getHost() + ".err");
					}
					Files.write(path, data.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
					host.setResultPath(path);
				}
				
				System.out.println("[RESULT FILE]:" + fileName);
				isRunning = false;
				data = null;
				
			} catch (IOException | InterruptedException e) {
				throw new JSchException("Connection Error");
			} finally {
				disconnect();
			}
			
		} catch (JSchException e) {
			System.err.println("### [ERROR] === " + host);
			e.printStackTrace(System.err);
		}
	}
	
	public void disconnect() {
		if(channel != null){
			channel.disconnect();
		}
		if(session != null){
			session.disconnect();
		}
		
		this.channel = null;
		this.session = null;
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
			e.printStackTrace(System.err);
		}
	}

	public void dispose() {
		close();
		disconnect();
	}

	public int getSessionTimeOut() {
		return sessionTimeOut;
	}

	public void setSessionTimeOut(int sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}
}
