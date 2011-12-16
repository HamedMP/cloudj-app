package org.phnq.core.util.io;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class FileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private Set<File> files;
    private long markedModTimeSum;
    private long markedFileSizeSum;

    public FileWatcher() {
        this.files = new HashSet<File>();
    }

    public int getNumFiles() {
        return files.size();
    }

    public void addFile(File file) {
        if (file != null && file.exists()) {
            this.files.add(file);
            mark();
        } else {
            logger.warn("Attempt to add null or non-existent file.");
        }
    }

    public void mark() {
        markedModTimeSum = getModifiedTimeSum();
        markedFileSizeSum = getFileSizeSum();
    }

    private long getModifiedTimeSum() {
        long modTimeSum = 0;
        for (File file : files) {
            modTimeSum += file.lastModified();
        }
        return modTimeSum;
    }

    private long getFileSizeSum() {
        long fileSizeSum = 0;
        for (File file : files) {
            fileSizeSum += file.length();
        }
        return fileSizeSum;
    }

    public boolean hasModificationsSinceLastMark() {
        return markedModTimeSum != getModifiedTimeSum() || markedFileSizeSum != getFileSizeSum();
    }
}
