package sf.pnr.base;

import java.io.File;

public final class Configuration {
    private static final Configuration INSTANCE = new Configuration();
    public static Configuration getInstance() {
        return INSTANCE;
    }

    private File book;
    private int transpositionTableSizeInMB = 1;
    private int evalHashTableSizeInMB = 1;
    private int depthExtCheck = Utils.PLY / 2;
    private int depthExt7ThRankPawn = Utils.PLY / 4;

    private Configuration() {
    }

    public File getOpeningBook() {
        return book;
    }

    public void setOpeningBook(final File book) {
        this.book = book;
    }

    public int getTranspositionTableSizeInMB() {
        return transpositionTableSizeInMB;
    }

    public void setTranspositionTableSizeInMB(final int transpositionTableSizeInMB) {
        this.transpositionTableSizeInMB = transpositionTableSizeInMB;
    }

    public int getEvalHashTableSizeInMB() {
        return evalHashTableSizeInMB;
    }

    public void setEvalHashTableSizeInMB(final int evalHashTableSizeInMB) {
        this.evalHashTableSizeInMB = evalHashTableSizeInMB;
    }

    public int getDepthExtCheck() {
        return depthExtCheck;
    }

    public void setDepthExtCheck(final int depthExtCheck) {
        this.depthExtCheck = depthExtCheck;
    }

    public int getDepthExt7ThRankPawn() {
        return depthExt7ThRankPawn;
    }

    public void setDepthExt7ThRankPawn(final int depthExt7ThRankPawn) {
        this.depthExt7ThRankPawn = depthExt7ThRankPawn;
    }
}