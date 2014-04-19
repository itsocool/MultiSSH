package com.asokorea;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.JSch;

public class MultipleShell {

	private volatile JSch jsch = new JSch();
	private int port = 22;
	private int sessionTimeOut = 3000;
	
	public volatile Map<HostVo, SSHRunner> runners;
	private volatile ArrayList<HostVo> hostList;
	private int maxThread = 2 * 4;
	public volatile ExecutorService executorService;
	private volatile int currentPosition = -1;
	private int lastPosition = -1;
	public volatile Path logPath;
	public volatile String[] commands = null;
	public 	Collection<Future<?>> futures = new LinkedList<Future<?>>();

	public MultipleShell(final ArrayList<HostVo> hostList){
		this(hostList, 0, 0, null);
	}
	
	public MultipleShell(final ArrayList<HostVo> hostList, final int maxThread, final int sessionTimeOut, final Path logPath){
		
		if(maxThread > 0 && maxThread < 128){
			this.maxThread = maxThread;
		}
		
		this.executorService = Executors.newFixedThreadPool(this.maxThread);
		this.setHostList(hostList);
		this.setLastPosition(hostList.size());
		this.runners = new HashMap<HostVo, SSHRunner>();
		this.sessionTimeOut = (sessionTimeOut > 0) ? sessionTimeOut : this.sessionTimeOut;
		this.logPath = logPath;
	}
	
	public synchronized void executeAll() {
		setCurrentPosition(0);
		
		for (final HostVo host : hostList) {
			try {
				if(host.getPort() <= 0){
					host.setPort(port);
				}
				
				final SSHRunner runner = new SSHRunner();
				runners.put(host, runner);
				runner.init(this.jsch, host, this.logPath, sessionTimeOut);
				futures.add(executorService.submit(runner));
				setCurrentPosition(getCurrentPosition() + 1);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.err.println("[ERROR:SYSTEM] " + e.getMessage());
			}
		}
		
		for (Future<?> future:futures) {
		    try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("[ERROR:SYSTEM] " + e.getMessage());
				future.cancel(true);
			}
		}
		dispose();
		
	}
	
	public void dispose() {
		if(executorService != null && !executorService.isTerminated())
		{
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
			
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(sessionTimeOut, TimeUnit.MILLISECONDS)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException ie) {
				System.err.println("[ERROR:SYSTEM] " + ie.getMessage());
				executorService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public synchronized ArrayList<HostVo> getHostList() {
		return hostList;
	}

	public synchronized void setHostList(ArrayList<HostVo> hostList) {
		this.hostList = hostList;
	}
	
	public synchronized int getCurrentPosition() {
		return currentPosition;
	}

	public synchronized void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public synchronized int getLastPosition() {
		return lastPosition;
	}

	public synchronized void setLastPosition(int lastPosition) {
		this.lastPosition = lastPosition;
	}

}