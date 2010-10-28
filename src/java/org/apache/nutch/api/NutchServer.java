package org.apache.nutch.api;

import java.util.List;
import java.util.logging.Level;

import org.apache.nutch.api.JobStatus.State;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NutchServer {
  private static final Logger LOG = LoggerFactory.getLogger(NutchServer.class);
  
  private Component component;
  private NutchApp app;
  private int port;
  private boolean running;
  
  public NutchServer(int port) {
    this.port = port;
    // Create a new Component. 
    component = new Component();
    component.getLogger().setLevel(Level.FINEST);
   
    // Add a new HTTP server listening on port 8182. 
    component.getServers().add(Protocol.HTTP, port); 
   
    // Attach the application. 
    app = new NutchApp();
    component.getDefaultHost().attach("/nutch", app); 
  }
  
  public boolean isRunning() {
    return running;
  }
  
  public void start() throws Exception {
    LOG.info("Statring NutchServer on port " + port + "...");
    component.start();
    LOG.info("Started NutchServer on port " + port);
    running = true;
  }
  
  public boolean stop(boolean force) throws Exception {
    if (!running) {
      return true;
    }
    List<JobStatus> jobs = NutchApp.jobMgr.list(null, State.RUNNING);
    if (!jobs.isEmpty() && !force) {
      LOG.warn("There are running jobs - NOT stopping at this time...");
      return false;
    }
    LOG.info("Stopping NutchServer on port " + port + "...");
    component.stop();
    LOG.info("Stopped NutchServer on port " + port);
    running = false;
    return true;
  }

  public static void main(String[] args) throws Exception { 
    if (args.length == 0) {
      System.err.println("Usage: NutchServer <port>");
      System.exit(-1);
    }
    int port = Integer.parseInt(args[0]);
    NutchServer server = new NutchServer(port);
    server.start();
  }
}
