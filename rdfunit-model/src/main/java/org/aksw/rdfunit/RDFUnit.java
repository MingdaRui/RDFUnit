package org.aksw.rdfunit;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.rdfunit.io.reader.*;
import org.aksw.rdfunit.model.interfaces.Pattern;
import org.aksw.rdfunit.model.interfaces.TestGenerator;
import org.aksw.rdfunit.model.readers.BatchPatternReader;
import org.aksw.rdfunit.model.readers.BatchTestGeneratorReader;
import org.aksw.rdfunit.services.PatternService;
import org.aksw.rdfunit.services.PrefixNSService;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Main class used to load and instantiate patterns and provide access to Test Generators
 *
 * @author Dimitris Kontokostas
 * @since 9/20/13 5:59 PM
 * @version $Id: $Id
 */
public class RDFUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDFUnit.class);

    private final Collection<String> baseDirectories; // Originally it is dataFolder

    private volatile Collection<TestGenerator> autoGenerators;
    private volatile Collection<Pattern> patterns;
    private volatile QueryExecutionFactoryModel patternQueryFactory;

    /**
     * <p>Constructor for RDFUnit.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     */
    public RDFUnit(Collection<String> baseDirectories) {
        this.baseDirectories = baseDirectories;
    }

    /**
     * <p>Constructor for RDFUnit.</p>
     *
     * @param baseDirectory a {@link java.lang.String} object.
     */
    public RDFUnit(String baseDirectory) {
        this(Collections.singletonList(baseDirectory));
    }

    /**
     * <p>Constructor for RDFUnit.</p>
     */
    public RDFUnit() {
        this(new ArrayList<>());
    }

    /**
     * <p>init.</p>
     *
     * @throws RdfReaderException if any.
     */
    public void init() throws RdfReaderException {

        /*MD*/  LOGGER.info( "****\n5. RDFUnit.init();\n****\n"  );

        LOGGER.info("Enter RDFUnit.init()");

        Model model = ModelFactory.createDefaultModel();
        // Set the defined prefixes
        PrefixNSService.setNSPrefixesInModel(model);
        // Read prefix.tll (Namespace & prefix map) as BiMap and put the BiMap into model.

        try {
            getPatternsReader(baseDirectories).read(model); // Get a RdfFirstSuccessReader
            // Actually, getPatternsReader(baseDirectories) get a colletion of RdfStreamReader
            // and give those RdfStreamReader to a RdfFirstSuccessReader, by which find the first RdfStreamReader
            // and read triples into model from a inputstream (pattern.ttl) holded by RdfStreamReader.

            getAutoGeneratorsALLReader(baseDirectories).read(model);
            /*MD*/  // RdfMultiReader ----> RdfFirstReader
                    //                  |-> RdfFirstReader...

            LOGGER.info("Finish getPatternsReader(baseDirectories).read(model) and getAutoFeneratorsALLReader(baseDirecoties).read(model)");

        } catch (RdfReaderException e) {
            throw new RdfReaderException(e.getMessage(), e);
        }

        // Store model in the QueryExecutionFactoryModel patternQueryFactory = new QueryExecutionFactoryModel(model);
        patternQueryFactory = new QueryExecutionFactoryModel(model);

        // Update pattern service
        for (Pattern pattern : getPatterns()) {
            PatternService.addPattern(pattern.getId(),pattern.getIRI(), pattern); /*MD*/// static fuction
        }

        LOGGER.info("Finish RDFUnit.init()");

    }

    private Collection<Pattern> getPatterns() {
        synchronized(this) {
            if (patterns == null) {
                patterns =
                        Collections.unmodifiableCollection(
                                BatchPatternReader.create().getPatternsFromModel(patternQueryFactory.getModel()));
                // 1. BatchPatternReader.create() returns a new BatchPatternReader();
                // 2. BatchPatternReader.getPatternsFromModel(model) returns a Collection of Pattern
                //    What really happens is:
                //      1). take a model ->
                //      2). get all the pattern resources ->
                //      3). convert those pattern resources to Pattern object (by extract pattern information and assign to a new Pattern object).
            }
            return patterns;
        }
    }

    /**
     * <p>Getter for the field <code>autoGenerators</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<TestGenerator> getAutoGenerators() {
        synchronized(this) {
            if (autoGenerators == null) {
                autoGenerators =
                        Collections.unmodifiableCollection(
                                BatchTestGeneratorReader.create().getTestGeneratorsFromModel(patternQueryFactory.getModel()));
            }
            return autoGenerators; // TestGeneratorImpl(Resource element, String description, String query, Pattern pattern, Collection<ResultAnnotation> generatorAnnotations)
        }
    }

    private static RdfReader createReaderFromBaseDirsAndResource(Collection<String> baseDirectories, String relativeName) {
        ArrayList<RdfReader> readers = new ArrayList<>();
        for (String baseDirectory : baseDirectories) {
            String normalizedBaseDir = baseDirectory.endsWith("/") ? baseDirectory : baseDirectory + "/";
            readers.add(new RdfStreamReader(normalizedBaseDir + relativeName));
            // (For Rdfunit init()) RdfStreamReader holds a null InputStream and a String format (.ttl).

            LOGGER.info("Reader: NormalizedBaseDir + relativeName -> " + normalizedBaseDir + relativeName);

        }
        readers.add(RdfReaderFactory.createResourceReader("/org/aksw/rdfunit/configuration/" + relativeName));
        /*MD*/  // (For Rdfunit init()) RdfStreamReader holds a InputStream (patterns.ttl) and a String format (.ttl).

        LOGGER.info("Reader: /org/aksw/rdfunit/configuration/" + relativeName);

        return new RdfFirstSuccessReader(readers);
    }

    /**
     * <p>getPatternsReader.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getPatternsReader(Collection<String> baseDirectories) {
        return createReaderFromBaseDirsAndResource(baseDirectories, "patterns.ttl");
    }

    /**
     * <p>getPatternsReader.</p>
     *
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getPatternsReader() {
        return getPatternsReader(new ArrayList<>());
    }

    /**
     * <p>getAutoGeneratorsOWLReader.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsOWLReader(Collection<String> baseDirectories) {
        return createReaderFromBaseDirsAndResource(baseDirectories, "autoGeneratorsOWL.ttl");
    }

    /**
     * <p>getAutoGeneratorsOWLReader.</p>
     *
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsOWLReader() {
        return getAutoGeneratorsOWLReader(new ArrayList<>());
    }

    /**
     * <p>getAutoGeneratorsDSPReader.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsDSPReader(Collection<String> baseDirectories) {
        return createReaderFromBaseDirsAndResource(baseDirectories, "autoGeneratorsDSP.ttl"); // Return a RdfFirstSuccessReader
    }

    /**
     * <p>getAutoGeneratorsDSPReader.</p>
     *
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsDSPReader() {
        return getAutoGeneratorsDSPReader(new ArrayList<>());
    }

    /**
     * <p>getAutoGeneratorsRSReader.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsRSReader(Collection<String> baseDirectories) {
        return createReaderFromBaseDirsAndResource(baseDirectories, "autoGeneratorsRS.ttl");
    }

    /**
     * <p>getAutoGeneratorsRSReader.</p>
     *
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsRSReader() {
        return getAutoGeneratorsRSReader(new ArrayList<>());
    }

    /**
     * @Author Mingda Rui
     *
     * <p>getAutoGeneratorsSHReader.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     * @return a {@link RdfReader} object
     * */
    public static RdfReader getAutoGeneratorsSHReader(Collection<String> baseDirectories) {
        return createReaderFromBaseDirsAndResource(baseDirectories, "autoGeneratorsSH.ttl");
    }

    /**
     * <p>getAutoGeneratorsALLReader.</p>
     *
     * @param baseDirectories a {@link java.util.Collection} object.
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsALLReader(Collection<String> baseDirectories) {
        Collection<RdfReader> readers = Arrays.asList(
                getAutoGeneratorsOWLReader(baseDirectories), //Return a RdfFirstSuccessReader
                getAutoGeneratorsDSPReader(baseDirectories),
                getAutoGeneratorsRSReader(baseDirectories),
                getAutoGeneratorsSHReader(baseDirectories)
        );

        return new RdfMultipleReader(readers);
    }

    /**
     * <p>getAutoGeneratorsALLReader.</p>
     *
     * @return a {@link RdfReader} object.
     */
    public static RdfReader getAutoGeneratorsALLReader() {
        return getAutoGeneratorsALLReader(new ArrayList<>());
    }
}
