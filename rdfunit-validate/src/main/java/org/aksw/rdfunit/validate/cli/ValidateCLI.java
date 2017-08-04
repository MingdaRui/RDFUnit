package org.aksw.rdfunit.validate.cli;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.rdfunit.RDFUnit;
import org.aksw.rdfunit.RDFUnitConfiguration;
import org.aksw.rdfunit.coverage.TestCoverageEvaluator;
import org.aksw.rdfunit.io.IOUtils;
import org.aksw.rdfunit.io.reader.RdfReaderException;
import org.aksw.rdfunit.io.writer.RdfMultipleWriter;
import org.aksw.rdfunit.io.writer.RdfResultsWriterFactory;
import org.aksw.rdfunit.io.writer.RdfWriter;
import org.aksw.rdfunit.io.writer.RdfWriterException;
import org.aksw.rdfunit.model.interfaces.TestCase;
import org.aksw.rdfunit.model.interfaces.TestSuite;
import org.aksw.rdfunit.model.interfaces.results.TestExecution;
import org.aksw.rdfunit.model.writers.TestCaseWriter;
import org.aksw.rdfunit.model.writers.results.TestExecutionWriter;
import org.aksw.rdfunit.services.PrefixNSService;
//import org.aksw.rdfunit.sources.SchemaService;
import org.aksw.rdfunit.sources.TestSource;
import org.aksw.rdfunit.tests.executors.TestExecutor;
import org.aksw.rdfunit.tests.executors.TestExecutorFactory;
import org.aksw.rdfunit.tests.executors.monitors.SimpleTestExecutorMonitor;
import org.aksw.rdfunit.tests.generators.TestGeneratorExecutor;
import org.aksw.rdfunit.utils.RDFUnitUtils;
import org.aksw.rdfunit.validate.ParameterException;
import org.aksw.rdfunit.validate.utils.ValidateUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>ValidateCLI class.</p>
 *
 * @author Dimitris Kontokostas
 *         Description
 * @since 11/19/13 10:49 AM
 * @version $Id: $Id
 */
public final class ValidateCLI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateCLI.class);

    private ValidateCLI() {}

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception {

        LOGGER.info("Mingda Start");

        CommandLine commandLine = ValidateUtils.parseArguments(args);
        // Using CommandLine to store the processed command line arguments & inputs

        /*MD*/  File logFileFolder = new File("/mnt/c/Users/Mingda Rui/Desktop/Semantic Web/LogFile/" );
        /*MD*/  if ( !logFileFolder.exists() ) {
        /*MD*/      logFileFolder = new File( "c:/Users/Mingda Rui/Desktop/Semantic Web/LogFile/" );
        /*MD*/  }
        /*MD*/  FileUtils.cleanDirectory( logFileFolder );
//        /*MD*/  File file = new File( "/mnt/c/Users/Mingda Rui/Desktop/Semantic Web/LogFile/commandline.getOptions.txt");
//        /*MD*/  if ( !file.exists() ) {
//        /*MD*/      file = new File( "c:/Users/Mingda Rui/Desktop/Semantic Web/LogFile/commandline.getOptions.txt");
//        /*MD*/  }
        /*MD*/  PrintWriter pw = new PrintWriter( new FileOutputStream( logFileFolder + "/commandline.getOptions.txt",true ) );
//        /*MD*/  LOGGER.info( logFileFolder + "commandline.getOptions.txt" );
        /*MD*/  for ( Option o : commandLine.getOptions()) {
        /*MD*/      pw.println( "ArgName: " + o.getArgName() + ", Value: " + o.getValue() + ", Description: " + o.getDescription() );
//        /*MD*/      pw.println( "ArgName: " + o.getArgName() + ", Value: " + o.getValue() );
        /*MD*/      pw.flush();
        /*MD*/  }
        /*MD*/
        /*MD*/  pw.close();


        if (commandLine.hasOption("h")) {
            // Estimate if it is the "help" option
            displayHelpAndExit();
        }

        if (!commandLine.hasOption('v')) { // explicitly do not use LOV
            RDFUnitUtils.fillSchemaServiceFromLOV();
            // Get all SchemaEntry from LOV by SPARQL query. This time, SchemaEntry is just a temp variable
            // LOV's uri, graph and query command are stored in LOVEndpoint.java (stored as URL and Query command)
            // Along with this process, RDFUnit add SchemaService(HashBiMap<Prefix, Namespace> schemata, HasgMap<Namespace, Definedby> definedBy )
            // That is, store prefix, namespace, definedby in SchemaService (static class).
        }
        //TODO hack until we fix this, configuration tries to load schemas so they must be initialized before
        RDFUnitUtils.fillSchemaServiceFromFile(ValidateCLI.class.getResourceAsStream("/org/aksw/rdfunit/configuration/schemaDecl.csv")); // File is under rdfunit-model directory
        // equals to ValidateCLI.getClass().getResourceAsStream(...);
        // ValidateCLI.class.getResourceAsStream( "/org/aksw/rdfunit/configuration/schemaDecl.csv" ); return a InputStream by getResourceAsStream() method.

        //RDFUnitUtils.fillSchemaServiceFromFile(configuration.getDataFolder() + "schemaDecl.csv");

        /* ***************************************************** */
        /* So far, we load all schemas in LOV and in schemaDecl.csv, and store them in the static class SchemaService, as SchemaService(HashBiMap<Prefix, Namespace> schemata, HasgMap<Namespace, Definedby> definedBy ) */
        /* ***************************************************** */


//        /*******************************************************/
//        // Dada Code Strat
//
//        LOGGER.info("Print all the schema loaded from LOV and schemaDecl.csv");
//        int count = 0;
//        String aSingleMapEntry;
//        PrintWriter out = new PrintWriter("/mnt/c/Users/Mingda Rui/Desktop/RDFUnitLog.txt");
//
//        for (Map.Entry entry : SchemaService.mdGetAllMapsInSchemaService().entrySet()) {
//            count++;
////            LOGGER.info("No." + count + " Entry:");
////            LOGGER.info("Key:   " + entry.getKey());
////            LOGGER.info("Value: " + entry.getValue());
//            aSingleMapEntry = "No." + count + " Entry:\n" +
//                    "Key:   " + entry.getKey() + "\n" +
//                    "Value: " + entry.getValue();
//            out.println(aSingleMapEntry);
//        }
//
//        out.close();
//
//        // Dada Code Finish
//        /*******************************************************/


        RDFUnitConfiguration configuration = null;
        try {
            configuration = ValidateUtils.getConfigurationFromArguments(commandLine);
        } catch (ParameterException e) {
            String message = e.getMessage();
            if (message != null) {
                displayHelpAndExit(message, e);
            } else {
                displayHelpAndExit();
            }
        }
        checkNotNull (configuration );

        LOGGER.info( "ValidateCLI.java -> configuration.getDataFolder(): " + configuration.getDataFolder() );

        if (!IOUtils.isFile(configuration.getDataFolder())) {
            LOGGER.error("Path : " + configuration.getDataFolder() + " does not exists, use -f argument");
            System.exit(1);
        }

        /*MD*/  // Initialize RDFUnit by give it the data folder, and store in RDFUnit as a singleton String collection.
        RDFUnit rdfunit = new RDFUnit(configuration.getDataFolder());
        try {
            rdfunit.init(); // has Pattern
            // What really happens in RDFUnit.init():
            //  1. create a Model
            //  2. read prefix.ttl into the Model (Namespce & prefix map)
            //  3. read Patterns.ttl into the Model
            //  4. read all the autoGenerators.ttl into the Model
            //  5. store model in RDFUnit.PatternQueryFactory
            //  6. Get patterns from Patterns.ttl, convert them into Pattern object.
            //  7. Store pattern Map in the PatternService, final private static Map<String, Pattern> idPatterns = new ConcurrentHashMap<>();  => Map(identifier, pattern);
            //                                              final private static Map<String, Pattern> iriPatterns = new ConcurrentHashMap<>(); => Map(IRI, pattern); IRI is the URI of a pattern resource that generates the pattern object.

        } catch (RdfReaderException e) {
            displayHelpAndExit("Cannot read patterns and/or pattern generators", e);
        }
         /*
        // Generates all tests from LOV
        for ( s: SchemaService.getSourceListAll()) {
            s.setBaseCacheFolder("../data/tests/");
            File f = new File(s.getTestFile());
            if (!f.exists()) {
                List<TestCase> testsAuto = TestUtils.instantiateTestsFromAG(rdfunit.getAutoGenerators(), s);
                TestUtils.writeTestsToFile(testsAuto,  s.getTestFile());
            }
        }
        // */


        final TestSource dataset = configuration.getTestSource();
        /*MD*/  // 1. Get the RDFUnitConfiguration.TestSourceBuilder testSourceBuilder,
        /*MD*/  // 2. Assign RDFUnitConfiguration.prefix, RDFUnitConfiguration.datasetURI to RDFUnitConfiguration.TestSourceBuilder.SourceConfig(prefix, uri)
        /*MD*/  // 3. Assign Collection<SchemaSource> RDFUnitConfiguration.schema to Collection<SchemaSource> RDFUnitConfiguration.TestSourceBuilder.referenceSchemata
        /*MD*/  // 4. Assign a RdfFirstSuccessReader holding -u/-d parameter (one RdfDereferenceReader or two RdfStreamReader), to RDFUnitConfiguration.TestSourceBuilder.inMemReader.
        /*MD*/  // 5. Set up a bunch of TestSourceBuilder configuration
        /*MD*/  // 6. return new DumpTestSource(sourceConfig, queryingConfig, referenceSchemata(-s in the form of SchemaSource), inMemReader(-d in RDF/XML?));
        /*MD*/  //                              sourceConfig, queryingConfig, referenceSchemata,                                 dumpReader,                  and a new ModelCom dumpModel
        /* </cliStuff> */

        TestGeneratorExecutor testGeneratorExecutor = new TestGeneratorExecutor(
                configuration.isAutoTestsEnabled(),
                configuration.isTestCacheEnabled(),
                configuration.isManualTestsEnabled());

        TestSuite testSuite = testGeneratorExecutor.generateTestSuite(configuration.getTestFolder(),
                dataset /*DumpTestSource (which contains the full details of -s + -d)*/,
                rdfunit.getAutoGenerators() /*Collection<TestGenerator> TestGeneratorImpl those three autoTest*/);
        /*MD*/  // q. RDFUnit.getAutoGenerators() return Collection<TestGenerator> autoGenerators = Collection<TestGenerator> TestGeneratorImpl(Resource element, String description, String query, Pattern pattern, Collection<ResultAnnotation> generatorAnnotations)


        TestExecutor testExecutor = TestExecutorFactory.createTestExecutor(configuration.getTestCaseExecutionType());
        if (testExecutor == null) {
            displayHelpAndExit("Cannot initialize test executor. Exiting", null);
        }
        SimpleTestExecutorMonitor testExecutorMonitor = new SimpleTestExecutorMonitor();
        testExecutorMonitor.setExecutionType(configuration.getTestCaseExecutionType());
        checkNotNull(testExecutor);
        testExecutor.addTestExecutorMonitor(testExecutorMonitor);

        // warning, caches intermediate results
        testExecutor.execute(dataset, testSuite);
        TestExecution testExecution = testExecutorMonitor.getTestExecution();


        // Write results to RDFWriter ()
        String resultsFolder = configuration.getDataFolder() + "results/";
        String filename = resultsFolder + dataset.getPrefix() + "." + configuration.getTestCaseExecutionType().toString();

        if (!(new File(resultsFolder).exists())) {
            LOGGER.warn("Results folder ({}) does not exist, creating it...", resultsFolder);
            File resultsFileFolder = new File(resultsFolder);
            boolean dirsCreated = resultsFileFolder.mkdirs();
            if (!dirsCreated) {
                LOGGER.error("could not create folder {}", resultsFileFolder.getAbsolutePath());
            }
        }

        List<RdfWriter> outputWriters = configuration.getOutputFormats().stream()
                .map(serializationFormat ->
                        RdfResultsWriterFactory.createWriterFromFormat(filename, serializationFormat, testExecution))
                .collect(Collectors.toList());

        RdfWriter resultWriter = new RdfMultipleWriter(outputWriters);
        try {
            Model model = ModelFactory.createDefaultModel();            TestExecutionWriter.create(testExecution).write(model);

            resultWriter.write(model);
            LOGGER.info("Results stored in: " + filename + ".*");
        } catch (RdfWriterException e) {
            LOGGER.error("Cannot write tests to file", e);
        }


        // Calculate coverage
        if (configuration.isCalculateCoverageEnabled()) {
            Model testSuiteModel = ModelFactory.createDefaultModel();
            PrefixNSService.setNSPrefixesInModel(testSuiteModel);
            for (TestCase ut : testSuite.getTestCases()) {
                TestCaseWriter.create(ut).write(testSuiteModel);
            }

            TestCoverageEvaluator tce = new TestCoverageEvaluator();
            tce.calculateCoverage(new QueryExecutionFactoryModel(testSuiteModel), configuration.getTestSource().getExecutionFactory());
        }
    }

    private static void displayHelpAndExit(String errorMessage, Exception e) {
        LOGGER.error(errorMessage, e);
        displayHelpAndExit();
    }

    private static void displayHelpAndExit() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rdfunit", ValidateUtils.getCliOptions());
        System.exit(1);
    }
}
