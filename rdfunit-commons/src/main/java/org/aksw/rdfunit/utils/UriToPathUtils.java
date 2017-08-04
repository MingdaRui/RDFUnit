package org.aksw.rdfunit.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Description
 *
 * @author Dimitris Kontokostas
 * @since 12/5/15 4:16 AM
 */
public final class UriToPathUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriToPathUtils.class);


    private UriToPathUtils(){}

    /**
     * <p>getCacheFolderForURI.</p>
     *
     * @param uri a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getCacheFolderForURI(String uri) {
        String retVal = "";
        try {
            URI tmp = new URI(uri);
            String host = tmp.getHost();
            String path = tmp.getPath();
            retVal = host + path + (path.endsWith("/") ? "" : "/");

            /*MD*/ File file = new File( "/mnt/c/Users/Mingda Rui/Desktop/Semantic Web/LogFile/");
            /*MD*/ if ( !file.exists() ) {
            /*MD*/ file = new File( "c:/Users/Mingda Rui/Desktop/Semantic Web/LogFile/");
            /*MD*/ }
            /*MD*/ PrintWriter pw = new PrintWriter(new FileOutputStream(file+"/CacheFolderForURI.txt",true));
            /*MD*/ String pwString;
            /*MD*/ pwString =   " URI: " + uri + "\n" +
            /*MD*/              "Host: " + host + "\n" +
            /*MD*/              "Path: " + path + (path.endsWith("/") ? "" : "/") + "\n";
            /*MD*/ pw.println(pwString);
            /*MD*/ pw.flush();
            /*MD*/ pw.close();


        } catch (URISyntaxException e) {
            LOGGER.debug("Cannot create cache folder for IRI {}", uri, e);
        } catch (FileNotFoundException e) { // MD
            e.printStackTrace(); // MD
        } // MD

        return retVal;
    }

    /**
     * <p>getAutoPrefixForURI.</p>
     *
     * @param uri a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getAutoPrefixForURI(String uri) {
        return uri
                .replace("http://", "")
                .replace("https://", "")
                .replaceAll("[?\"'\\/<>*|:#,&]", "_");
    }

}
