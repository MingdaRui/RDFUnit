package org.aksw.rdfunit.validate.utils;

import org.aksw.rdfunit.RDFUnitConfiguration;
import org.aksw.rdfunit.enums.TestCaseExecutionType;
import org.aksw.rdfunit.exceptions.UndefinedSchemaException;
import org.aksw.rdfunit.exceptions.UndefinedSerializationException;
import org.aksw.rdfunit.validate.ParameterException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>ValidateUtils class.</p>
 *
 * @author Dimitris Kontokostas
 *         Description
 * @since 6/13/14 4:35 PM
 * @version $Id: $Id
 */
public final class ValidateUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateUtils.class);

    private static final Options cliOptions = generateCLIOptions();

    private ValidateUtils(){}

    /**
     * <p>Getter for the field <code>cliOptions</code>.</p>
     *
     * @return a {@link org.apache.commons.cli.Options} object.
     */
    public static Options getCliOptions() {
        return cliOptions;
    }

    /**
     * <p>parseArguments.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @return a {@link org.apache.commons.cli.CommandLine} object.
     * @throws org.apache.commons.cli.ParseException if any.
     */
    public static CommandLine parseArguments(String[] args) throws ParseException {

        LOGGER.info("****\n1. CommandLine commandLine = ValidateUtils.parseArguments(args);\n****\n");

        CommandLineParser cliParser = new DefaultParser();
        return cliParser.parse(getCliOptions(), args);
    }

    private static Options generateCLIOptions() {
        Options cliOptions = new Options();

        cliOptions.addOption("h", "help", false, "show this help message");
        cliOptions.addOption("d", "dataset-uri", true,
                "the URI of the dataset (required)");
        cliOptions.addOption("e", "endpoint", true,
                "the endpoint to run the tests on (If no endpoint is provided RDFUnit will try to dereference the dataset-uri)");
        cliOptions.addOption("g", "graph", true, "the graphs to use (separate multiple graphs with ',' (no whitespaces) (defaults to '')");
        cliOptions.addOption("u", "uri", true, "the uri to use for dereferencing if not the same with `dataset`");
        cliOptions.addOption("s", "schemas", true,
                "the schemas used in the chosen graph " +
                        "(comma separated prefixes without whitespaces according to http://lov.okfn.org/). If this option is missing RDFUnit will try to guess them automatically"
        );
        cliOptions.addOption("o", "output-format", true, "the output format of the validation results: html (default), turtle, n3, ntriples, json-ld, rdf-json, rdfxml, rdfxml-abbrev");
        cliOptions.addOption("p", "enriched-prefix", true,
                "the prefix of this dataset used for caching the schema enrichment, e.g. dbo");
        cliOptions.addOption("C", "no-test-cache", false, "Do not load cached automatically generated test cases, regenerate them (Cached test cases are loaded by default)");
        cliOptions.addOption("M", "no-manual-tests", false, "Do not load any manually defined test cases (Manual test cases are loaded by default)");
        cliOptions.addOption("A", "no-auto-tests", false, "Do not load any schema / automatically generated test cases (Automatically generated test cases are loaded by default)");
        cliOptions.addOption("r", "result-level", true, "Specify the result level for the error reporting. One of status, aggregate, rlog, extended (default is aggregate).");
        cliOptions.addOption("l", "logging-level", true, "Not supported at the moment! will filter test cases based on logging level (notice, warn, error, etc).");
        cliOptions.addOption("T", "query-cache-ttl", true, "Specify the SPARQL Endpoint cache Time-To-Live (TTL) of the cache (in minutes) or '0' to disable it.");
        cliOptions.addOption("D", "query-delay", true, "Specify the delay between consecutive queries cache against an Endpoint or '0' to disable delay.");
        cliOptions.addOption("L", "query-limit", true, "Specify the maximum results from a SPARQL test query or '0' to disable limits.");
        cliOptions.addOption("P", "query-pagination", true, "Specify a pagination option for retrieving big results or '0' to disable it.");
        cliOptions.addOption("c", "test-coverage", false, "Calculate test-coverage scores");
        cliOptions.addOption("f", "data-folder", true, "the location of the data folder (defaults to '../data/' or '~/.rdfunit). " +
                "This is where the results and the caches are stored. " +
                "If none exists, bundled versions will be loaded.'");
        cliOptions.addOption("v", "no-LOV", false, "Do not use the LOV service");

        return cliOptions;
    }

    /**
     * <p>getConfigurationFromArguments.</p>
     *
     * @param commandLine a {@link org.apache.commons.cli.CommandLine} object.
     * @return a {@link org.aksw.rdfunit.RDFUnitConfiguration} object.
     * @throws org.aksw.rdfunit.validate.ParameterException if any.
     */
    public static RDFUnitConfiguration getConfigurationFromArguments(CommandLine commandLine) throws ParameterException {

        LOGGER.info("****\n4. ValidateUtils.getConfigurationFromArguments(commandLine);\n****\n");

        checkIfRequiredParametersMissing(commandLine);

        RDFUnitConfiguration configuration = readDatasetUriAndInitConfiguration(commandLine);

        setDumpOrSparqlEndpoint(commandLine, configuration);


        setSchemas(commandLine, configuration);
        setEnrichedSchemas(commandLine, configuration);


        setTestExecutionType(commandLine, configuration);
        setOutputFormats(commandLine, configuration);

        setTestAutogetCacheManual(commandLine, configuration);

        setQueryTtlCachePaginationLimit(commandLine, configuration);


        setCoverageCalculation(commandLine, configuration);

        return configuration;
    }

    // 0
    private static void checkIfRequiredParametersMissing(CommandLine commandLine) throws ParameterException {
        if (!commandLine.hasOption("d")) {
            throw new ParameterException("Error: Required arguments are missing.");

        }
        if (commandLine.hasOption("e") && commandLine.hasOption("u")) {
            throw new ParameterException("Error: You have to select either an Endpoint or a Dump URI.");
        }

        if (commandLine.hasOption("l")) {
            throw new ParameterException("Option -l was changed to -r, -l is reserved for --logging-level (notice, warn, error)");
        }
    }

    // 1
    private static RDFUnitConfiguration readDatasetUriAndInitConfiguration(CommandLine commandLine) {
        RDFUnitConfiguration configuration;
        String dataFolder = commandLine.getOptionValue("f", "data/");

        //Dataset URI, important & required (used to associate manual dataset test cases)
        String datasetURI = commandLine.getOptionValue("d", "");
        if (!datasetURI.isEmpty() && datasetURI.endsWith("/")) {
            datasetURI = datasetURI.substring(0, datasetURI.length() - 1);
        }

        // in RDFUnitConfiguration:
        // set datasetURI = datasetURI - "/"(at the end of String),
        // set dataFolder,
        // set testFolder = dataFolder + "tests/",
        // set prefix = datasetURI - "http(s)://" - "[?\"'\\/<>*|:#,&]"
        configuration = new RDFUnitConfiguration(datasetURI, dataFolder);
        return configuration;
    }

    // 2
    private static void setDumpOrSparqlEndpoint(CommandLine commandLine, RDFUnitConfiguration configuration) {

        // Dump location for dump dereferencing (defaults to dataset uri)
        String customDereferenceURI = commandLine.getOptionValue("u");
        if (customDereferenceURI != null && !customDereferenceURI.isEmpty()) {

            /*MD*/  LOGGER.info("Goes into setDumpOrSparqlEndpoint(CommandLine commandLine, RDFUnitConfiguration configuration) {}");

            // What this operation does:
            // 1. RDFUnitConfiguration.testSourceBuilder.setImMemFromUri(customDereferenceURI);
            //  0). kind of init a RDFUnitConfiguration.TestSourceBuilder
            //  1). set the TestSourceType -> InMenSingle
            //  2). set up QueryingConfig
            //  3). init (RdfReader) TestSourceBuilder.inMemReader = RdfFirstSuccessReader(readers);
            // 2. RDFUnitConfiguration.customDereferenceURI = customDereferenceURI;
            configuration.setCustomDereferenceURI(customDereferenceURI);
        }

        //TODO write annotation for this code block
        //Endpoint initialization
        if (commandLine.hasOption('e')) {
            String endpointURI = commandLine.getOptionValue("e");
            Collection<String> endpointGraphs = getUriStrs(commandLine.getOptionValue("g", ""));
            // -g (graphies) are read in as a single String which contains a few ",".
            // We need to use getUriStrs() to process and separate the Sting by ",".

            configuration.setEndpointConfiguration(endpointURI, endpointGraphs);
        }
    }

    // 3
    // Get parameters from -s command (called prefix within this method), and process them and add them into a RDFUnitConfiguration
    // All -s schema are stored in the RDFUnitConfiguration.schemas, (Collection<SchemaSource> schemas)
    // Every SchemaSource is a single prefix info (the full detailed information for each schema)
    private static void setSchemas(CommandLine commandLine, RDFUnitConfiguration configuration) throws ParameterException {

        if (commandLine.hasOption("s")) {

            /*MD*/  LOGGER.info("Goes into setSchemas(CommandLine commandLine, RDFUnitConfiguration configuration) throws ParameterException {}");

            try {
                // Get schema list
                // process the -s parameter, separate them by ",", make them a Collection<String>.
                Collection<String> schemaUriPrefixes = getUriStrs(commandLine.getOptionValue("s"));

                // set RDFUnitConfiguration.schemas = Collcetion<SchemaSource>
                // every SchemaSource in the Collection<SchemaSource>, contains a SourceConfig(SchemaService.schemata.prefix, SchemaService.schemata.namespace), a String schema(which is SchemaService.definedBy.definedBy), a RdfReader RdfFirstSuccessReader (which reads in the definedBy) initialized, and a Model
                // RDFUnitConfiguration.schema store the full details of -s schema
                configuration.setSchemataFromPrefixes(schemaUriPrefixes);
            } catch (UndefinedSchemaException e) {
                throw new ParameterException(e.getMessage(), e);
            }
        }
        // try to guess schemas automatically
        else {
            LOGGER.info("Searching for used schemata in dataset");

            // configuration.getTestSource() returns a DumpTestSource
            configuration.setAutoSchemataFromQEF(configuration.getTestSource().getExecutionFactory());
        }
    }

    // 4
    private static void setEnrichedSchemas(CommandLine commandLine, RDFUnitConfiguration configuration) {
        //Get enriched schema
        String enrichedDatasetPrefix = commandLine.getOptionValue("p");
        configuration.setEnrichedSchema(enrichedDatasetPrefix);
    }

    // 5
    private static void setTestExecutionType(CommandLine commandLine, RDFUnitConfiguration configuration) {
        TestCaseExecutionType resultLevel = TestCaseExecutionType.aggregatedTestCaseResult;
        if (commandLine.hasOption("r")) {
            String rl = commandLine.getOptionValue("r", "aggregate");
            switch (rl.toLowerCase()) {
                case "status":
                    resultLevel = TestCaseExecutionType.statusTestCaseResult;
                    break;
                case "aggregate":
                    resultLevel = TestCaseExecutionType.aggregatedTestCaseResult;
                    break;
                case "shacl-lite":
                    resultLevel = TestCaseExecutionType.shaclSimpleTestCaseResult;
                    break;
                case "shacllite":
                    resultLevel = TestCaseExecutionType.shaclSimpleTestCaseResult;
                    break;
                case "shacl":
                    resultLevel = TestCaseExecutionType.shaclFullTestCaseResult;
                    break;
                case "rlog":
                    resultLevel = TestCaseExecutionType.rlogTestCaseResult;
                    break;
                case "extended":
                    resultLevel = TestCaseExecutionType.extendedTestCaseResult;
                    break;
                default:
                    LOGGER.warn("Option --result-level defined but not recognised. Using 'aggregate' by default.");
                    break;
            }
        }
        configuration.setTestCaseExecutionType(resultLevel);
    }

    // 6
    private static void setOutputFormats(CommandLine commandLine, RDFUnitConfiguration configuration) throws ParameterException {
        // Get output formats (with HTML as default)
        Collection<String> outputFormats = getUriStrs(commandLine.getOptionValue("o", "html"));
        try {
            configuration.setOutputFormatTypes(outputFormats);
        } catch (UndefinedSerializationException e) {
            throw new ParameterException(e.getMessage(), e);
        }
    }

    // 7
    private static void setTestAutogetCacheManual(CommandLine commandLine, RDFUnitConfiguration configuration) throws ParameterException {
        // for automatically generated test cases
        boolean testCacheEnabled = !commandLine.hasOption("C");
        configuration.setTestCacheEnabled(testCacheEnabled);

        //Do not use manual tests
        boolean manualTestsEnabled = !commandLine.hasOption("M");
        configuration.setManualTestsEnabled(manualTestsEnabled);

        //Do not use automatic tests
        boolean autoTestsEnabled = !commandLine.hasOption("A");
        configuration.setAutoTestsEnabled(autoTestsEnabled);

        if (!configuration.isManualTestsEnabled() && !configuration.isAutoTestsEnabled()) {
            throw new ParameterException("both -M & -A does not make sense");
        }

        if (!configuration.isAutoTestsEnabled() && configuration.isTestCacheEnabled()) {
            throw new ParameterException("both -A & -C does not make sense");
        }
    }

    // 8
    private static void setQueryTtlCachePaginationLimit(CommandLine commandLine, RDFUnitConfiguration configuration) throws ParameterException {
        // Get query time to live cache option
        String ttlStr = commandLine.getOptionValue("T");
        if (ttlStr != null) {
            try {
                // we set the cache in minutes
                long ttl = 60L * 1000L * Long.parseLong(ttlStr);
                configuration.setEndpointQueryCacheTTL(ttl);

            } catch (NumberFormatException e) {
                throw new ParameterException("-T not a valid number", e);
            }
        }

        // Get query delay option
        String delayStr = commandLine.getOptionValue("D");
        if (delayStr != null) {
            try {
                long delay = Long.parseLong(delayStr);
                configuration.setEndpointQueryDelayMS(delay);

            } catch (NumberFormatException e) {
                throw new ParameterException("-D not a valid number", e);
            }
        }


        // Get query pagination option
        String paginationStr = commandLine.getOptionValue("P");
        if (paginationStr != null) {
            try {
                long pagination = Long.parseLong(paginationStr);
                configuration.setEndpointQueryPagination(pagination);

            } catch (NumberFormatException e) {
                throw new ParameterException("-P not a valid number", e);
            }
        }

        // Get query Limit option
        String limitStr = commandLine.getOptionValue("L");
        if (limitStr != null) {
            try {
                long limit = Long.parseLong(limitStr);
                configuration.setEndpointQueryLimit(limit);

            } catch (NumberFormatException e) {
                throw new ParameterException("-L not a valid number", e);
            }
        }
    }

    // 9
    private static void setCoverageCalculation(CommandLine commandLine, RDFUnitConfiguration configuration) {
        boolean calculateCoverage = commandLine.hasOption("c");
        configuration.setCalculateCoverageEnabled(calculateCoverage);
    }

    // Separate String by ","
    private static Collection<String> getUriStrs(String parameterStr) {
        Collection<String> uriStrs = new ArrayList<>();
        if (parameterStr == null) {
            return uriStrs;
        }

        for (String uriStr : parameterStr.split(",")) {
            if (!uriStr.trim().isEmpty()) {
                uriStrs.add(uriStr.trim());
            }
        }

        return uriStrs;
    }

}
