package util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Loggers created here create logs in logdir
 * LoggerFactory keeps track of created logs so that
 * same name logs dont overwrite each other
 */
public class LoggerFactory {
    static Map<String,Integer> nameCount= new HashMap<>();
    static final String logdir="logs/";
    public LoggerFactory(){

    }
    public Logger createLogger(String name){
       int logcount=0;
        if(nameCount.containsKey(name)){
           logcount= nameCount.get(name)+1;
           nameCount.replace(name,logcount);
        }
        else
            nameCount.put(name,0);
        Logger logger = Logger.getLogger(name);
        initLogger(logger,logcount);
      return logger;
    }

    private void initLogger(Logger logger, int logCount){

        FileHandler fh;
        String suffix= logCount==0?"":String.valueOf(logCount);
        String name= logger.getName();
        try {

            fh = new FileHandler(logdir+name+suffix+".log");
            logger.addHandler(fh);
            EmptyFormatter formatter = new EmptyFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.setUseParentHandlers(false);
        logger.info(name+" created");

    }
    public static void closeHandlerls(Logger logger){
        for(Handler h: logger.getHandlers())
            h.close();
    }

}

