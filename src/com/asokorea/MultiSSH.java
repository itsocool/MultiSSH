package com.asokorea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.misc.Launcher;

public class MultiSSH {

	private static ArrayList<HostVo> hostList;
	private static MultipleShell shell;
	private static String baseDir = Launcher.class.getResource("/").getPath();
	private static String taskName = "_default_";
	private static Path taskRootPath = null;
	
	
	public static void main(String[] args) {
		
		taskName = args[0];
		taskRootPath = Paths.get(baseDir, "task", taskName);
		
		File hostFile = taskRootPath.resolve("hostlist.xml").toFile();
		File commandFile = taskRootPath.resolve("command.sh").toFile();
		
		Date sdt = new Date();
		Date edt = null;
//		String commandFile = basePath + args[0];
		BufferedReader br = null;
		String sCurrentLine = null;
		String line = null;
		ArrayList<String> commandList = null;
//
		try {
//			
//			br = new BufferedReader(new FileReader(commandFile));
//			commandList = new ArrayList<String>();
//			hostList = new ArrayList<HostVo>();
//			
//			while ((sCurrentLine = br.readLine()) != null)
//			{
//				commandList.add(line);
//			}
//			
//			if(commandList != null && commandList.size() > 0){
//				String[] commands = commandList.toArray(new String[]{});
//			}
//			
//			File logDir = new File(basePath + "logs");
//			
//			if(logDir != null && !logDir.exists()){
//				logDir.mkdir();
//			}
//			
//			HostVo vo;
//			
//			for (String string : hosts) {
//				if(string != null && string.trim().length() > 0){
//					String userAndPass = string.split("@")[0];
//					String hostAndPort = string.split("@")[1];
//					String user = userAndPass.split(":")[0];
//					String pass = userAndPass.replace(user + ":", "");
//					String host = hostAndPort.split(":")[0];
//					int port = Integer.parseInt(hostAndPort.split(":")[1]);
//					Path resultPath = Paths.get(basePath, "logs");
//					
//					vo = new HostVo(host, user, pass, commands, resultPath, port);
//					hostList.add(vo);
//				}
//			}
//			
//			shell = new MultipleShell(hostList, logDir);
//			shell.logDir = logDir.getAbsolutePath();
//			shell.executeAll();
//			shell.executorService.awaitTermination(3, TimeUnit.SECONDS);
			System.out.println(baseDir);
			System.out.println(taskName);
			System.out.println(taskRootPath);
			System.out.println(hostFile.getAbsolutePath());
			System.out.println(commandFile.getAbsolutePath());
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			System.exit(1);
		} finally {
			
            try
            {
                if (br != null)
                br.close();
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }

			if(shell != null)
			{
				shell.dispose();
			}
			
			shell = null;
			
			showMemory();
			edt = new Date();
			System.out.println(Long.valueOf(edt.getTime() - sdt.getTime()) + "ms");
			System.out.println("###################### finish ######################");
			System.exit(0);
		}
		
	}
	
	private ArrayList<HostVo> getHostList(String hostFilePath, String commandFilePath){
		ArrayList<HostVo> result = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(hostFilePath));
			NodeList row = document.getElementsByTagName("row");
			
			if(row != null && row.getLength() > 0){
				result = new ArrayList<HostVo>();
//				HostVo = 
				
			}
			
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Document getCommad(String filePath){
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new File(filePath));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return document;
	}
	
	private static void showMemory(){
	    long free  = Runtime.getRuntime().freeMemory();
	    long total = Runtime.getRuntime().totalMemory();
	    long max   = Runtime.getRuntime().maxMemory();

	    System.out.format("Total Memory : %6.2f MB%n", (double) total / (1024 * 1024));
	    System.out.format("Free  Memory : %6.2f MB%n", (double) free  / (1024 * 1024));
	    System.out.format("Max   Memory : %6.2f MB%n", (double) max   / (1024 * 1024));
	}
}
