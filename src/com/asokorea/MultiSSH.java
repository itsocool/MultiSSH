package com.asokorea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.misc.Launcher;

public class MultiSSH {

	private static ArrayList<HostVo> hostList;
	private static MultipleShell shell;
	private static String baseDir = Launcher.class.getResource("/").getPath();
	private static String taskName = "_default_";
	private static String logDirName = "logs";
	private static String taskXmlFileName = "task.xml";
	private static File taskFile = null;
	private static Path logPath = null;
	private static int maxThreadPoolCount = 2;
	private static int timeOut = 3 * 1000;
	
	public static void main(String[] args) {
		
//		if(args != null && args.length > 0){
//			taskName = args[0];
//		}
		
		Document document = null;
		String[] commands = null;
		Date sdt = new Date();
		Date edt = null;

		System.out.println("###################### start ######################");
		document = getXML(taskName);
		commands = getCommand(document);
		hostList = getHostList(document, commands);
		logPath = getLogPath(taskName);
		shell = new MultipleShell(hostList, maxThreadPoolCount, timeOut, logPath);
		shell.executeAll();
			
		if(document != null){
			document = null;
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

	private static Document getXML(String taskName){
		
		Document document = null;
		
		try {
			
			Path path = new File(baseDir + "../task/" + taskName).toPath();
			taskFile = path.resolve(taskXmlFileName).toFile();
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskFile.getCanonicalFile());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return document;
	}
	
	private static String[] getCommand(Document document){
		
		String[] result = null;
		XPath xpath = null;
		NodeList commandNodes = null;
		int length = 0;
		
		try {
			xpath = XPathFactory.newInstance().newXPath();
			commandNodes = (NodeList)xpath.evaluate("//commands/command", document, XPathConstants.NODESET);
			length = commandNodes.getLength();
			result = new String[length];
			
			for (int i = 0; i < length; ++i) {
				Node command = (Node) commandNodes.item(i);
				result[i] = command.getTextContent();
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static ArrayList<HostVo> getHostList(Document document, String[] commands){
		
		ArrayList<HostVo> result = null;
		XPath xpath = null;
		NodeList hostNodes = null;

		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskFile.getCanonicalFile());
			xpath = XPathFactory.newInstance().newXPath();
			hostNodes = (NodeList)xpath.evaluate("//hosts/host", document, XPathConstants.NODESET);
			result = new ArrayList<HostVo>();
			
			for (int i = 0; i < hostNodes.getLength(); i++) {
				NodeList children = hostNodes.item(i).getChildNodes();
				String ip = null;
				String user = null;
				String pass = null;
				
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					
					if("ip".equals(child.getNodeName())){
						ip = child.getTextContent();
					} else if("username".equals(child.getNodeName())){
						user = child.getTextContent();
					} else if("password".equals(child.getNodeName())){
						pass = child.getTextContent();
					}
				}
				
				HostVo vo = new HostVo(ip, user, pass);
				vo.setCommands(commands);
				result.add(vo);
			}
		} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static Path getLogPath(String taskName) {
		
		Path result = null;
		File file = new File(baseDir).getParentFile();

		result = Paths.get(file.getAbsolutePath(), "task", taskName);
		file = result.toFile();
		
		if(file != null && !file.exists()){
			file.mkdir();
		}

		result = Paths.get(file.getAbsolutePath(), logDirName);
		file = result.toFile();
		
		if(file != null && !file.exists()){
			file.mkdir();
		}
		
		return result;
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
