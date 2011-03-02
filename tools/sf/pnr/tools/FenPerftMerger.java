package sf.pnr.tools;

import java.io.*;

/**
 */
public class FenPerftMerger {

    public static void main(final String[] args) throws IOException {
        final String cmdFileName = args[0];
        final String resultFileName = args[1];
        final String outputFileName = args[2];

        final BufferedReader cmdReader = new BufferedReader(new FileReader(cmdFileName));
        final BufferedReader resultReader = new BufferedReader(new FileReader(resultFileName));
        final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

        boolean newFen = true;
        boolean first = true;
        try {
            for (String cmd = cmdReader.readLine(); cmd != null; cmd = cmdReader.readLine()) {
                cmd = cmd.trim();
                if (cmd.startsWith("setboard ")) {
                    if (!first) {
                        writer.newLine();
                    } else {
                        first = false;
                    }
                    writer.write(cmd.substring(9, cmd.length() - 3));
                    newFen = true;
                } else if (cmd.startsWith("perft ")) {
                    if (!newFen) {
                        writer.write("; ");
                    } else {
                        newFen = false;
                    }
                    writer.write('p');
                    writer.write(cmd.substring(6));
                    writer.write(' ');
                    final String resultLine = resultReader.readLine();
                    final String perft = resultLine.split(",")[0].split(":")[1].trim();
                    writer.write(perft);
                } else if (cmd.equals("quit")) {
                    break;
                } else {
                    throw new IllegalStateException("Unexpected line: " + cmd);
                }
            }
        } finally {
            writer.close();
            cmdReader.close();
            resultReader.close();
        }
    }
}