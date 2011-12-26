package org.solrmarc.testUtils;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.net.SocketFactory;

import org.solrmarc.tools.Utils;

public class SolrJettyProcess
{
    private JavaInvoke vmspawner = null;
    private int jettyPort = 0;
    private Process jettyProcess = null;
    private ByteArrayOutputStream serverOut = null;
    private ByteArrayOutputStream serverErr = null;
    private boolean serverIsUp = false;

    public SolrJettyProcess(String solrHomeDir, String jettyPath, String jettyTestPortStr)
    {
        vmspawner = startJettyWithSolrAsBackgroundProcess(solrHomeDir, jettyPath, jettyTestPortStr);
        jettyPort = Integer.parseInt(jettyTestPortStr);
    }
    
    private static JavaInvoke startJettyWithSolrAsBackgroundProcess(String solrHomeDir, String jettyPath, String jettyTestPortStr) 
    {
        JavaInvoke vmspawner;
        
		String testSolrLogLevel = System.getProperty("test.solr.log.level");
		String testSolrmarcLogLevel = System.getProperty("test.solrmarc.log.level");
		IndexTest.setTestLoggingLevels(testSolrLogLevel, testSolrmarcLogLevel);

        Map<String, String> javaProps = new LinkedHashMap<String, String>();
        javaProps.put("solr.solr.home", myGetCanonicalPath(new File(solrHomeDir)));
        javaProps.put("jetty.port", jettyTestPortStr);
        List<String> addnlClassPath = new ArrayList<String>();
        addnlClassPath.add(myGetCanonicalPath(new File(jettyPath + File.separator + "start.jar")));
        System.out.println("Starting Jetty Solr server at " + myGetCanonicalPath(new File(jettyPath)) + " port " + jettyTestPortStr);

        vmspawner = new JavaInvoke("org.mortbay.start.Main",
                                   new File(myGetCanonicalPath(new File(jettyPath))), 
                                   javaProps, 
                                   null,
                                   addnlClassPath,
                                   null, false);
        return(vmspawner);
    }
    
    
    public boolean startProcessWaitUntilSolrIsReady() throws IOException 
    {
        serverOut = new ByteArrayOutputStream();
        serverErr = new ByteArrayOutputStream();
        
        jettyProcess = vmspawner.startStdinStderrInstance("JETTY", serverOut, serverErr);
        serverIsUp = false;
System.out.println("DEBUG: in startProcessWaitUntilSolrIsReady with port " + String.valueOf(jettyPort));
        if (jettyPort == 0)
        {
            String jettyPortStr = waitServerIsUp(60000, 100, serverErr, "INFO:  Started SocketConnector@0.0.0.0:", "INFO:  Started SocketConnector @ 0.0.0.0:");
            jettyPort = Integer.parseInt(jettyPortStr);
            serverIsUp = checkServerIsUp(5000, 100, getServerAddress(), jettyPort);
        }
        else
        {
        	serverIsUp = checkServerIsUp(15000, 100, getServerAddress(), jettyPort);
        }
        return(serverIsUp);
    }

    
    public void stopServer()
    {
        if (jettyProcess != null)
        {
            System.out.println("Stopping Jetty Solr server at port " + String.valueOf(jettyPort));
            jettyProcess.destroy();
            try
            {
                jettyProcess.waitFor();
            }
            catch (InterruptedException e)
            {
            }
        }     
    }


    private static String myGetCanonicalPath(File file)
    {
        String pathStr = null;
        try {
            pathStr = file.getCanonicalPath();
        }
        catch (IOException e)
        {
            pathStr = file.getAbsolutePath();
        }
        return(pathStr);
    }
    
    private static InetAddress getServerAddress() throws UnknownHostException 
    {
//        return InetAddress.getLocalHost();
        return InetAddress.getByName(null);
    }
    
    /**
     * Repeats a TCP connection check every <em>sleepTime</em> milliseconds until it either succeeds
     * or times out after <em>timeout</em> milliseconds.
     * 
     * @see Server#checkServerIsUp(InetAddress, int) An explanation of the TCP checking mechanism.
     * 
     * @param timeout If no check is successful after this many milliseconds has passed, fail the 
     * overall checking process.
     * @param sleepTime How long to wait (in milliseconds) between checks of the service.
     * @param server address of server to check.
     * @param port port to check.
     * @return true if a connection attempt succeeds, false in the case of error or 
     * no connection attempt successful.
     */
    private static String waitServerIsUp(long timeout, long sleepTime, ByteArrayOutputStream out, String patternToWatchFor1, String patternToWatchFor2  ) 
    {
        long start = System.currentTimeMillis();
        String socketStr = "0";
        int lastLineRead = 0;
        while((System.currentTimeMillis() - start) < timeout)
        {
            String outputSoFar = new String(out.toByteArray());
            String lines[] = outputSoFar.split("\r?\n");
            for (int i = lastLineRead; i < lines.length; i++)
            {
//                System.out.println(lines[i]);
                if (lines[i].contains(patternToWatchFor1))
                {
                    socketStr = lines[i].replaceAll(".*"+patternToWatchFor1 + "([0-9]*).*", "$1");
                    return(socketStr);
                }
                else if (lines[i].contains(patternToWatchFor2))
                {
                    socketStr = lines[i].replaceAll(".*"+patternToWatchFor2 + "([0-9]*).*", "$1");
                    return(socketStr);
                }
            }
            lastLineRead = lines.length;
            try {
                Thread.sleep(sleepTime);
            } 
            catch (InterruptedException e) 
            {
                return socketStr;
            }
        }
        return socketStr;
    }
    
    /**
     * Repeats a TCP connection check every <em>sleepTime</em> milliseconds until it either succeeds
     * or times out after <em>timeout</em> milliseconds.
     * 
     * @see Server#checkServerIsUp(InetAddress, int) An explanation of the TCP checking mechanism.
     * 
     * @param timeout If no check is successful after this many milliseconds has passed, fail the 
     * overall checking process.
     * @param sleepTime How long to wait (in milliseconds) between checks of the service.
     * @param server address of server to check.
     * @param port port to check.
     * @return true if a connection attempt succeeds, false in the case of error or 
     * no connection attempt successful.
     */
    private static boolean checkServerIsUp(long timeout, long sleepTime, InetAddress server, int port ) 
    {
        long start = System.currentTimeMillis();
        while((System.currentTimeMillis() - start) < timeout)
        {
            if(!checkServerIsUp(server, port))
            {
                try {
                    Thread.sleep(sleepTime);
                } 
                catch (InterruptedException e) 
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Performs a simple TCP connection check to the specified address and port.
     * 
     * @param server address of the server to contact.
     * @param port TCP port to connect to on the specified server.
     * @return true if that port is accepting connections, 
     * false in all other cases: not listening and/or connection error.
     */
    private static boolean checkServerIsUp(InetAddress server, int port) 
    {
System.out.println("DEBUG:  checkServerIsUp will use " + server.getHostName());        	

    	Socket sock = null;
        try {        	
            sock = SocketFactory.getDefault().createSocket(server, port);
//            sock = SocketFactory.getDefault().createSocket("localhost", port);
            sock.setSoLinger(true, 0);
System.out.println("DEBUG:  checkServerIsUp HAD SUCCESS!!!!!");        	
            return true;
        } 
        catch (IOException e) 
        { 
System.out.println("DEBUG:  checkServerIsUp caught IOException " + e.getMessage());        	
            return false;
        }
        finally
        {
            if(sock != null)
            {
                try {
                    sock.close();
                } 
                catch (IOException e) 
                {
                    // don't care
                }
            }
        }
    }

    // if you need to see the output generated after the jetty server is up and running
    // but the amount in the buffer is too large to see the later log info, call outputReset()
    // In general this routine won't be needed.
    public void outputReset()
    {
        serverErr.reset();
        serverOut.reset();
    }
    
    public int getJettyPort()
    {
        return jettyPort;
    }

    public boolean isServerRunning()
    {
        return serverIsUp;
    }

    
}
