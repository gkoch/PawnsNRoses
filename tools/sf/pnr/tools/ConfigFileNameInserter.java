package sf.pnr.tools;

import sf.pnr.tests.TestUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;

public class ConfigFileNameInserter {

    public static void main(final String[] args) throws IOException {
        final File dir = new File(args[0]);
        final Pattern pattern = Pattern.compile(args[1]);
        final File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return pattern.matcher(name).matches();
                }
            });
        for (File file: files) {
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            final String configName = TestUtils.getFileNameWithoutExtension(file);
            final StringBuilder builder = new StringBuilder((int) (file.length() + configName.length() + 2));
            builder.append("# ").append(configName);
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    builder.append("\r\n").append(line);
                }
            } finally {
                reader.close();
            }
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            try {
                writer.write(builder.toString());
            } finally {
                writer.close();
            }
        }
    }

}
