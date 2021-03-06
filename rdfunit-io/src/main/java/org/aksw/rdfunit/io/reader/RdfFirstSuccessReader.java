package org.aksw.rdfunit.io.reader;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * <p>RDFFirstSuccessReader class.</p>
 *
 * @author Dimitris Kontokostas
 *         Description
 * @since 11/14/13 8:51 AM
 * @version $Id: $Id
 */
public class RdfFirstSuccessReader implements RdfReader {

    /*MD*/  private static final Logger LOGGER = LoggerFactory.getLogger(RdfFirstSuccessReader.class);

    /*MD*/  int count = 0;

    private final Collection<RdfReader> readers;

    /**
     * <p>Constructor for RDFFirstSuccessReader.</p>
     *
     * @param readers a {@link java.util.Collection} object.
     */
    public RdfFirstSuccessReader(Collection<RdfReader> readers) {
        super();
        this.readers = readers;
    }

    /** {@inheritDoc} */
    @Override
    public void read(Model model) throws RdfReaderException {
        StringBuilder message = new StringBuilder();
        // return the first successful attempt

        /*MD*/  LOGGER.info( "Total " + readers.size() + " readers");

        for (RdfReader r : readers) {

            /*MD*/  count++;
            /*MD*/  LOGGER.info( "No." + count + ", reading " + r.getRdfReaderName() );

            try {

                r.read(model);
                // return on first read() that does not throw an exception
                return;
            } catch (RdfReaderException e) {

                /*MD*/  LOGGER.info( "No." + count + ", reading entries catch");

                message.append('\n');
                if (e.getMessage() != null) {
                    message.append(e.getMessage());
                } else {
                    message.append(e);
                }
            }
        }

        throw new RdfReaderException("Cannot read from any reader: " + message.toString());
    }

    /** {@inheritDoc} */
    @Override
    public void readDataset(Dataset dataset) throws RdfReaderException {
        StringBuilder message = new StringBuilder();
        // return the first successful attempt
        for (RdfReader r : readers) {
            try {
                r.readDataset(dataset);
                // return on first read() that does not throw an exception
                return;
            } catch (RdfReaderException e) {
                message.append("\n");
                if (e.getMessage() != null) {
                    message.append(e.getMessage());
                } else {
                    message.append(e);
                }
            }
        }

        throw new RdfReaderException("Cannot read from any reader: " + message.toString());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "RDFFirstSuccessReader{" +
                "readers=" + readers +
                '}';
    }
}
