package util.Logging;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * java.util.logging.Logger adapter class to add detail level
 */
public class LeveledLogger {
    private final Logger logger;
    private LogDetailLevel detailLevel;
    public LeveledLogger(Logger logger, LogDetailLevel detailLevel){
        this.logger=logger;
        this.detailLevel=detailLevel;
    }
    public void info(String msg, LogDetailLevel lvl){
        if( detailLevel.ordinal()>= lvl.ordinal())
            logger.info(msg);
    }
    public void closeHandlers(){
        for(Handler h: logger.getHandlers())
            h.close();
    }
}