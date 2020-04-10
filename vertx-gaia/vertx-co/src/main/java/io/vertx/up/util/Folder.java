package io.vertx.up.util;

import io.vertx.up.eon.FileSuffix;
import io.vertx.up.eon.Protocols;
import io.vertx.up.fn.Fn;
import io.vertx.up.log.Annal;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

final class Folder {
    private static final Annal LOGGER = Annal.get(Folder.class);

    private Folder() {
    }

    static List<String> listFiles(final String folder) {
        return listFiles(folder, null);
    }

    static List<String> listFiles(final String folder, final String extension) {
        return Fn.getNull(new ArrayList<>(), () -> list(folder, extension, false), folder);
    }

    static List<String> listDirectories(final String folder) {
        return Fn.getNull(new ArrayList<>(), () -> list(folder, null, true), folder);
    }

    private static List<String> list(final String folder,
                                     final String extension,
                                     final boolean isDirectory) {
        /*
         * list folder by related file first
         * /folder here
         */
        final File folderObj = new File(folder);
        final List<String> retList = new ArrayList<>();
        if (folderObj.exists()) {
            /*
             * Related path here, it means that
             * such as /folder/extra/ etc.
             */
            retList.addAll(getFiles(folderObj, extension, isDirectory));
        } else {
            URL url = IO.getURL(folder);
            if (Objects.isNull(url)) {
                /*
                 * Fix jar path issue here.
                 */
                if (folder.contains(FileSuffix.JAR_DIVIDER)) {
                    url = Fn.getJvm(() -> new URL(folder), folder);
                }
            }
            /*
             * Split steps for url extraction
             */
            if (Objects.isNull(url)) {
                LOGGER.error("The url of folder = `{0}` is null", folder);
            } else {
                /*
                 * Whether it's jar path or common path.
                 */
                final String protocol = url.getProtocol();
                if (Protocols.FILE.equals(protocol)) {
                    /*
                     * Common file
                     */
                    retList.addAll(getFiles(url, extension, isDirectory));
                } else if (Protocols.JAR.equals(protocol)) {
                    /*
                     * Jar File
                     */
                    retList.addAll(getJars(url, extension, isDirectory));
                } else {
                    LOGGER.error("protocol error! protocol = {0}, url = {1}", protocol, url);
                }
            }
        }
        return retList;
    }

    private static List<String> getJars(final URL url, final String extension, final boolean isDirectory) {
        return Fn.getJvm(() -> {
            final List<String> retList = new ArrayList<>();
            final JarURLConnection jarCon = (JarURLConnection) url.openConnection();
            final JarFile jarFile = jarCon.getJarFile();
            /*
             * JarEntry iterator
             */
            final String[] specifics = url.getPath().split("!/");
            final String folder = specifics[1];     // Jar Specification
            final Enumeration<JarEntry> entities = jarFile.entries();
            while (entities.hasMoreElements()) {
                final JarEntry entry = entities.nextElement();
                /*
                 * First, we must filter the folder
                 */
                if (entry.getName().startsWith(folder)) {
                    /*
                     * isDirectory to identify the pick up
                     */
                    if (isDirectory) {
                        if (entry.isDirectory()) {
                            /*
                             * The condition is ok to pickup folder only
                             */
                            retList.add(entry.getName().substring(entry.getName().lastIndexOf('/') + 1));
                        }
                    } else {
                        if (!entry.isDirectory()) {
                            if (Objects.isNull(extension)) {
                                /*
                                 * No Extension
                                 */
                                retList.add(entry.getName().substring(entry.getName().lastIndexOf('/') + 1));
                            } else {
                                if (entry.getName().endsWith(extension)) {
                                    /*
                                     * Extension enabled
                                     */
                                    retList.add(entry.getName().substring(entry.getName().lastIndexOf('/') + 1));
                                }
                            }
                        }
                    }
                }
            }
            return retList;
        }, url);
    }

    private static List<String> getFiles(final File directory, final String extension, final boolean isDirectory) {
        final List<String> retList = new ArrayList<>();
        if (directory.isDirectory() && directory.exists()) {
            final File[] files = (isDirectory) ?
                    directory.listFiles(File::isDirectory) :
                    (null == extension ?
                            directory.listFiles(File::isFile) :
                            directory.listFiles((item) -> item.isFile()
                                    && item.getName().endsWith(extension)));
            if (null != files) {
                retList.addAll(Arrays.stream(files)
                        .map(File::getName)
                        .collect(Collectors.toList()));
            }
        } else {
            LOGGER.error("The file doest not exist, file = `{0}`", directory.getAbsolutePath());
        }
        return retList;
    }

    private static List<String> getFiles(final URL url, final String extension, final boolean isDirectory) {
        final File directory = new File(url.getFile());
        return getFiles(directory, extension, isDirectory);
    }
}
