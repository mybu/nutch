package org.apache.nutch.api;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.data.Form;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class ConfResource extends ServerResource {
  
  public static final String PATH = "confs";
  public static final String DESCR = "Configuration manager";
  public static final String DEFAULT_CONF = "default";
  
  private AtomicInteger seqId = new AtomicInteger();
  
  @Get("json")
  public Object retrieve() throws Exception {
    String id = (String)getRequestAttributes().get(Params.CONF_ID);
    if (id == null) {
      return NutchApp.confMgr.list();
    } else {
      String prop = (String)getRequestAttributes().get(Params.PROP_NAME);
      if (prop == null) {
        return NutchApp.confMgr.getAsMap(id);
      } else {
        Map<String,String> cfg = NutchApp.confMgr.getAsMap(id);
        if (cfg == null) {
          return null;
        } else {
          return cfg.get(prop);
        }
      }
    }
  }
  
  @Put("json")
  public String create(Map<String,Object> args) throws Exception {
    System.out.println("args=" + args);
    String id = (String)args.get(Params.CONF_ID); 
    if (id == null) {
      id = String.valueOf(seqId.incrementAndGet());
    }
    Map<String,String> props = (Map<String,String>)args.get(Params.PROPS);
    Boolean force = (Boolean)args.get(Params.FORCE);
    boolean f = force != null ? force : false;
    NutchApp.confMgr.create(id, props, f);
    return id;
  }
  
  @Post("json")
  public void update(Map<String,Object> args) throws Exception {
    String id = (String)args.get(Params.CONF_ID); 
    if (id == null) id = (String)getRequestAttributes().get(Params.CONF_ID);
    if (id == null) {
      throw new Exception("Missing config id");
    }
    String prop = (String)args.get(Params.PROP_NAME);
    if (prop == null) prop = (String)getRequestAttributes().get(Params.PROP_NAME);
    if (prop == null) {
      throw new Exception("Missing property name prop");
    }
    String value = (String)args.get(Params.PROP_VALUE);
    if (value == null) {
      throw new Exception("Missing property value");
    }
    NutchApp.confMgr.setProperty(id, prop, value);
  }
  
  @Delete
  public void remove() throws Exception {
    String id = (String)getRequestAttributes().get(Params.CONF_ID);
    if (id == null) {
      throw new Exception("Missing config id");
    }
    NutchApp.confMgr.delete(id);
  }
}
