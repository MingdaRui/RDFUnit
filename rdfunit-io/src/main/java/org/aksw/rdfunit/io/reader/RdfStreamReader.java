package org.aksw.rdfunit.io.reader;

import org.aksw.rdfunit.io.format.FormatService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * <p>RDFStreamReader class.</p>
 *
 * @author Dimitris Kontokostas
 *         Reads a model from an InputStream (or a filename)
 * @since 11/14/13 8:37 AM
 * @version $Id: $Id
 */
public class RdfStreamReader implements RdfReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdfStreamReader.class);

    /*MD*/ public static int count = 0;

    /*MD*/ public static String RdfReaderName = "RdfStreamReader";
    private final InputStream inputStream;
    private final String format;

    /**
     * <p>Constructor for RDFStreamReader.</p>
     *
     * @param filename a {@link java.lang.String} object.
     */
    public RdfStreamReader(String filename) {
        this(getInputStreamFromFilename(filename), FormatService.getFormatFromExtension(filename));
    }

    //public RDFStreamReader(InputStream inputStream) {
    //    this(inputStream, "TURTLE");
    //}

    /**
     * <p>Constructor for RDFStreamReader.</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param format a {@link java.lang.String} object.
     */
    public RdfStreamReader(String filename, String format) {
        this(getInputStreamFromFilename(filename), format);
    }

    /**
     * <p>Constructor for RDFStreamReader.</p>
     *
     * @param inputStream a {@link java.io.InputStream} object.
     * @param format a {@link java.lang.String} object.
     */
    public RdfStreamReader(InputStream inputStream, String format) {
        super();
        this.inputStream = inputStream;
        this.format = format;
    }

    /** {@inheritDoc} */
    @Override
    public void read(Model model) throws RdfReaderException {
        try {

            /*MD*/ ByteArrayOutputStream baos = new ByteArrayOutputStream();
            /*MD*/ IOUtils.copy(inputStream, baos);
            /*MD*/ byte[] bytes = baos.toByteArray();

            /*MD*/ ByteArrayInputStream bais1 = new ByteArrayInputStream(bytes);

            // Read and add triples from inputStream to the model.
//            RDFDataMgr.read(model, inputStream, null, RDFLanguages.nameToLang(format));
            /*MD*/ RDFDataMgr.read(model, bais1, null, RDFLanguages.nameToLang(format));
            /*MD*/ LOGGER.info( "RdfStreamReader.read(Model)" );

            /*MD*/ File directory = new File("/mnt/c/Users/Mingda Rui/Desktop/Semantic Web/LogFile/");
            /*MD*/ if (!directory.exists()) {
            /*MD*/     directory = new File("c:/Users/Mingda Rui/Desktop/Semantic Web/LogFile/");
            /*MD*/ }
            /*MD*/ File file = new File( directory + "/RdfStreamReaderOutput.txt" );
            /*MD*/ PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            /*MD*/ out.println("No." + ++count + ": ");
            /*MD*/ out.flush();
            /*MD*/ IOUtils.copy(new ByteArrayInputStream(bytes), new FileOutputStream(file, true));

        } catch (Exception e) {
            throw new RdfReaderException(e.getMessage(), e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void readDataset(Dataset dataset) throws RdfReaderException {
        try {
            RDFDataMgr.read(dataset, inputStream, null, RDFLanguages.nameToLang(format));
        } catch (Exception e) {
            throw new RdfReaderException(e.getMessage(), e);
        }

    }

    // Returns a FileInputStream or null in case of exception
    // We want to raise the exception only at ready time
    private static InputStream getInputStreamFromFilename(String filename) {
        try {
            return new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            LOGGER.debug("Error initializing RdfStreamReader, not handled atm", e);
            // do not handle exception at construction time
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "RDFStreamReader{" +
                "inputStream=" + inputStream +
                ", format=" + format +
                '}';
    }

    /*MD*/
    @Override
    public String getRdfReaderName() {
        return RdfReaderName;
    }
}
