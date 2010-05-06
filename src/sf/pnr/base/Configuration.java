package sf.pnr.base;

public final class Configuration {
    private static final Configuration INSTANCE = new Configuration();

    public static Configuration getInstance() {
        return INSTANCE;
    }

    private int transpositionTableSizeInMB = 1;
    private int evalHashTableSizeInMB = 1;

    private Configuration() {
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
}