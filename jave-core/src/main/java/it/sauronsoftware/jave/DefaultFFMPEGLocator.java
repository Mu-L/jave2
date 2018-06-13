/*
 * JAVE - A Java Audio/Video Encoder (based on FFMPEG)
 * 
 * Copyright (C) 2008-2009 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.sauronsoftware.jave;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default ffmpeg executable locator, which exports on disk the ffmpeg
 * executable bundled with the library distributions. It should work both for
 * windows and many linux distributions. If it doesn't, try compiling your own
 * ffmpeg executable and plug it in JAVE with a custom {@link FFMPEGLocator}.
 *
 * @author Carlo Pelliccia
 */
public class DefaultFFMPEGLocator extends FFMPEGLocator {

    private final static Log LOG = LogFactory.getLog(FFMPEGExecutor.class);

    /**
     * Trace the version of the bundled ffmpeg executable. It's a counter: every
     * time the bundled ffmpeg change it is incremented by 1.
     */
    private static final int MY_EXE_VERSION = 2;

    /**
     * The ffmpeg executable file path.
     */
    private final String path;

    /**
     * It builds the default FFMPEGLocator, exporting the ffmpeg executable on a
     * temp file.
     */
    public DefaultFFMPEGLocator() {
        // Windows?
        boolean isWindows;
        String os = System.getProperty("os.name").toLowerCase();
        isWindows = os.contains("windows");

        // Temporary folder
        File temporaryFolder = new File(System.getProperty("java.io.tmpdir"), "jave-"
                + MY_EXE_VERSION);
        if (!temporaryFolder.exists())
        {
            temporaryFolder.mkdirs();
            temporaryFolder.deleteOnExit();
        }
        // ffmpeg executable export on disk.
        String suffix = isWindows ? ".exe" : "";
        String arch = System.getProperty("os.arch");

        File exe = new File(temporaryFolder, "ffmpeg-" + arch + suffix);
        if (!exe.exists())
        {
            copyFile("ffmpeg-" + arch + suffix, exe);
        }
        // Need a chmod?
        if (!isWindows)
        {
            Runtime runtime = Runtime.getRuntime();
            try
            {
                runtime.exec(new String[]
                {
                    "/bin/chmod", "755",
                    exe.getAbsolutePath()
                });
            } catch (IOException e)
            {
                LOG.error(e);
            }
        }
        // Ok.
        this.path = exe.getAbsolutePath();
    }

    @Override
    protected String getFFMPEGExecutablePath() {
        return path;
    }

    /**
     * Copies a file bundled in the package to the supplied destination.
     *
     * @param path The name of the bundled file.
     * @param dest The destination.
     * @throws RuntimeException If aun unexpected error occurs.
     */
    private void copyFile(String path, File dest) throws RuntimeException {
        copy(getClass().getResourceAsStream("native/" + path), dest.getAbsolutePath());
    }

    /**
     * Copy a file from source to destination.
     *
     * @param source The name of the bundled file.
     * @param destination the destination
     * @return True if succeeded , False if not
     */
    private boolean copy(InputStream source, String destination) {
        boolean success = true;

        try
        {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex)
        {
            LOG.warn("Cannot write file " + destination, ex);
            success = false;
        }

        return success;
    }
}