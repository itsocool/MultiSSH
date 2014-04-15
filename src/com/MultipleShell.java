package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class MultipleShell {

	private JSch jsch = new JSch();
	private int port = 22;
	private int sessionTimeOut = 3000;
	
	public volatile Map<HostVo, SSHRunner> runners;
	private volatile ArrayList<HostVo> hostList;
	private int maxThread = 2 * 4;
	public ExecutorService executorService;
	private volatile int currentPosition = -1;
	private int lastPosition = -1;
	
	public MultipleShell(final ArrayList<HostVo> hostList, final int maxThread){
		
		if(maxThread > 0 && maxThread < 32){
			this.maxThread = maxThread;
		}
		
		this.executorService = Executors.newFixedThreadPool(this.maxThread);
		this.setHostList(hostList);
		this.setLastPosition(hostList.size());
		this.runners = new HashMap<HostVo, SSHRunner>();
		System.out.println("## Create MultipleShell");
	}
	
	public synchronized void executeAll() {
		
		try {
			setCurrentPosition(0);
			
			for (final HostVo host : hostList) {
				
				if(host.getPort() <= 0){
					host.setPort(port);
				}
				
				if(host.getSessionTimeOut() <= 0){
					host.setSessionTimeOut(sessionTimeOut);
				}
				
				final Session session = jsch.getSession(host.getUser(), host.getHost(), host.getPort());
				final SSHRunner runner = new SSHRunner(session, host);
				runners.put(host, runner);
				executorService.execute(runner);
				setCurrentPosition(getCurrentPosition() + 1);
			}
			
			dispose();
		} catch (JSchException | IOException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		
		if(runners !=null && runners.size() > 0)
		{
			for (final HostVo host : hostList) {

				SSHRunner runner = runners.get(host);
				
				if (runner != null) {
					runner.dispose();
				}

				runner = null;
				runners.remove(host);
			}
			
		}
		
		jsch = null;
	}

	public ArrayList<HostVo> getHostList() {
		return hostList;
	}

	public void setHostList(ArrayList<HostVo> hostList) {
		this.hostList = hostList;
	}
	
	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public int getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(int lastPosition) {
		this.lastPosition = lastPosition;
	}

}