package sf.pnr.tests;

import sf.pnr.base.Board;

import java.util.Map;

/**
 */
public interface EpdProcessorTask {
    public void run(String fileName, Board board, Map<String, String> commands);
    public void completed();
}