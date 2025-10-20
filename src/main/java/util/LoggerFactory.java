package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Loggers created here create logs in <code>LoggerFactory.logdir</code>
 * LoggerFactory keeps track of created logs so that
 * same name logs dont overwrite each other
 */
public class LoggerFactory {
    static Map<String,Integer> nameCount= new HashMap<>();
    public LoggerFactory(){

    }
    public Logger createLogger(String logdir ,String name ){
       int logcount=0;
        if(nameCount.containsKey(name)){
           logcount= nameCount.get(name)+1;
           nameCount.replace(name,logcount);
        }
        else
            nameCount.put(name,0);
        Logger logger = Logger.getLogger(name);
        initLogger(logger, logdir, logcount);
      return logger;
    }

    private void initLogger(Logger logger,String logdir, int logCount){

        FileHandler fh;
        String suffix= logCount==0?"":String.valueOf(logCount);
        String name= logger.getName();
        try {

            Files.createDirectories(Paths.get(logdir));
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

