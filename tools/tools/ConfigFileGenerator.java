package tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public class ConfigFileGenerator {

    public static void main(String[] args) throws IOException {
        final File templateFile = new File(args[0]);
        final String templateFileName = templateFile.getName();
        final String templateName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));
        final File configRootDir = new File(args[1]);

        final LinkedHashMap<String, List<TemplateEntry>> entries = new LinkedHashMap<String, List<TemplateEntry>>();
        final Properties template = new Properties();
        template.load(new FileReader(templateFile));

        for (String name: template.stringPropertyNames()) {
            final String value = template.getProperty(name);
            for (int start = value.indexOf('[', 0); start != -1; start = value.indexOf('[', start)) {
                final int end = value.indexOf(']', start + 1);
                final String entryStr = value.substring(start + 1, end);
                final String[] parts = entryStr.split(",");
                final int min = Integer.parseInt(parts[0].trim());
                final int max = Integer.parseInt(parts[1].trim());
                final int inc;
                if (parts.length > 2) {
                    inc = Integer.parseInt(parts[2].trim());
                } else {
                    inc = 1;
                }
                List<TemplateEntry> list = entries.get(name);
                if (list == null) {
                    list = new ArrayList<TemplateEntry>();
                    entries.put(name, list);
                }
                list.add(new TemplateEntry(min, max, inc, start, end));
                start = end + 1;
            }
        }

        boolean hasNext = true;
        int templateCount = 0;
        final File targetDir = new File(configRootDir, templateName);
        targetDir.mkdirs();
        while (hasNext) {
            final Properties config = new Properties();
            for (String name: template.stringPropertyNames()) {
                String value = template.getProperty(name);
                final List<TemplateEntry> list = entries.get(name);
                if (list != null) {
                    final StringBuilder builder = new StringBuilder(value);
                    final ListIterator<TemplateEntry> iter = list.listIterator(list.size());
                    while (iter.hasPrevious()) {
                        final TemplateEntry entry = iter.previous();
                        builder.replace(entry.getStartPos(), entry.getEndPos() + 1, String.valueOf(entry.getCounter()));
                    }
                    value = builder.toString();
                }
                config.setProperty(name, value);
            }
            config.store(new FileWriter(new File(targetDir, templateName + "-" + templateCount + ".ini")),
                "Config #" + templateCount);
            templateCount++;
            hasNext = increment(entries);
        }
    }

    private static boolean increment(final LinkedHashMap<String, List<TemplateEntry>> entries) {
        for (List<TemplateEntry> list: entries.values()) {
            for (TemplateEntry entry: list) {
                if (entry.incCounter()) {
                    return true;
                } else {
                    entry.resetCounter();
                }
            }
        }
        return false;
    }

    private static class TemplateEntry {
        private final int min;
        private final int max;
        private final int inc;
        private final int startPos;
        private final int endPos;
        private int counter;

        private TemplateEntry(final int min, final int max, final int inc, final int startPos, final int endPos) {
            this.min = min;
            this.max = max;
            this.inc = inc;
            this.startPos = startPos;
            this.endPos = endPos;
            counter = min;
        }

        public int getStartPos() {
            return startPos;
        }

        public int getEndPos() {
            return endPos;
        }

        public int getCounter() {
            return counter;
        }

        public void resetCounter() {
            counter = min;
        }

        public boolean incCounter() {
            final boolean success = counter <= max - inc;
            if (success) {
                counter += inc;
            }
            return success;
        }
    }
}