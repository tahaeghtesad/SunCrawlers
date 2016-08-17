package ir.arcinc.yourgraph;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by tahae on 4/11/2016.
 */
public class LoggingLinkedBlockingQueue<T> extends LinkedBlockingDeque<T> {
    Logger logger = LoggerFactory.getLogger("ir.arcinc.yourgraph.BlockingQueue");
    @Override
    public T take() throws InterruptedException {
        T ret = super.poll();
        if (ret == null) {
            logger.debug("Queue emptied.");
            return super.take();
        }
        return ret;
    }
}
