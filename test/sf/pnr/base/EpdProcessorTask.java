package sf.pnr.base;

import java.util.Map;

/**
 */
public interface EpdProcessorTask {
    public void run(String fileName, Board board, Map<String, String> commands);
}