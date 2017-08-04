package org.aksw.rdfunit.utils;

import org.aksw.rdfunit.prefix.LOVEndpoint;
import org.aksw.rdfunit.prefix.SchemaEntry;
import org.aksw.rdfunit.sources.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;

/**
 * <p>RDFUnitUtils class.</p>
 *
 * @author Dimitris Kontokostas
 *         Description
 * @since 9/24/13 11:25 AM
 * @version $Id: $Id
 */
public final class RDFUnitUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDFUnitUtils.class);

    private RDFUnitUtils() {
    }

    /**
     * <p>fillSchemaServiceFromFile.</p>
     *
     * @param additionalCSV a {@link java.lang.String} object.
     */
    public static void fillSchemaServiceFromFile(String additionalCSV) {

        try (InputStream inputStream = new FileInputStream(additionalCSV)) {
            fillSchemaServiceFromFile(inputStream);

        } catch (IOException e) {
            LOGGER.error("File " + additionalCSV + " not fount!", e);
        }
    }

    /**
     * <p>fillSchemaServiceFromFile.</p>
     *
     * @param additionalCSV a {@link java.io.InputStream} object.
     */
    public static void fillSchemaServiceFromFile(InputStream additionalCSV) {

        LOGGER.info("****\n3. RDFUnitUtils.fillSchemaServiceFromFile( InputStream additionalCSV );\n****\n");

        int count = 0;

        if (additionalCSV != null) {

            try (BufferedReader in = new BufferedReader(new InputStreamReader(additionalCSV, "UTF-8"))) {

                // For the structure of "additionalCSV": prefix, URI, URL(if URI does not dereference)

                String line;

                while ((line = in.readLine()) != null) {
                    // skip comments & empty lines
                    if (line.startsWith("#") || line.trim().isEmpty()) {
                        continue;
                    }

                    count++;

                    String[] parts = line.split(",");
                    switch (parts.length) {
                        case 2: // prefix + URI => SchemaService.addSchemaDecl(String prefix, String namespace);
                            SchemaService.addSchemaDecl(parts[0], parts[1]);
                            break;
                        case 3: // prefix + URI + URL => SchemaService.addSchemaDecl(String prefix, String namespace, String definedBy);
                            SchemaService.addSchemaDecl(parts[0], parts[1], parts[2]);
                            break;
                        default:
                            LOGGER.error("Invalid schema declaration in " + additionalCSV + ". Line: " + line);
                            count--;
                            break;
                    }
                }

            } catch (IOException e) {
                LOGGER.debug("IOException reading schemas", e);
                return;
            }

            LOGGER.info("Loaded " + count + " schema declarations from: " + additionalCSV);
        }

        if (additionalCSV != null) {
            try {
                additionalCSV.close();
            } catch (IOException e) {
                LOGGER.debug("IOException: ", e);
            }
        }
    }

    /**
     * <p>fillSchemaServiceFromLOV.</p>
     */
    public static void fillSchemaServiceFromLOV() {

        /*MD*/  LOGGER.info("****\n2. RDFUnitUtils.fillSchemaServiceFromLOV();\n****\n");

        /*MD*/  int count = SchemaService.getSize(); // SchemaService is a static class

        /*MD*/  LOGGER.info( "RDFUnitUtils.fillSchemaServiceFromLOV() -> int count = SchemaService.getSize() == " + count );

        /*MD*/  LOGGER.info("Print all the LOV schema to text file");
        /*MD*/  String aSingleSchemaEntry = null;
        /*MD*/  PrintWriter out = null;
        /*MD*/  try {
        /*MD*/      File file = new File("/mnt/c/Users/Mingda Rui/Desktop/Semantic Web/LogFile/");
        /*MD*/      if (!file.exists()) {
        /*MD*/          file = new File("c:/Users/Mingda Rui/Desktop/Semantic Web/LogFile/");
        /*MD*/      }
        /*MD*/  out = new PrintWriter(file + "/allLOVSchema.txt");
        /*MD*/  out.close();
        /*MD*/  } catch (FileNotFoundException e) {
        /*MD*/      e.printStackTrace();
        /*MD*/  }

        for (SchemaEntry entry : new LOVEndpoint().getAllLOVEntries()) { // Search LOV Endpoint and get all LOV entries (by SPARQL)
            // new LOVEndpoint().getAllLOVEntries() query all the vocabularies in the LOV. (594 totally)
            // For each of the vocabulary, list its Prefix, URI, Namespace and Definedby.
            // Then, use these four elements to generate SchemaEntry, put all the generated SchemaEntry into a List<SchemaEntry>.
            // Finally, return the List<SchemaEntry>.

            SchemaService.addSchemaDecl(entry.getPrefix(), entry.getVocabularyNamespace(), entry.getVocabularyDefinedBy());
            // SchemaService (static class):
            //  private static final  BiMap<String prefix, String namespace> schemata = HashBiMap.create();
            //  private static final Map<String namespace ,String definedBy> definedBy = new HashMap<>();
            // This command adds SchemaEntry information into two Map within SchemaService.

            //TODO Output this to a file

            aSingleSchemaEntry = "No." + SchemaService.getSize() + " ->\n" +
                    "     Prefix: " + entry.getPrefix() + "\n" +
                    "  Namespace: " + entry.getVocabularyNamespace() + "\n" +
                    "        URI: " + entry.getVocabularyURI() + "\n" +
                    "  DefinedBy: " + entry.getVocabularyDefinedBy() + "\n";
            out.println(aSingleSchemaEntry);
        }

        out.close();

        count = SchemaService.getSize() - count;

        LOGGER.info( "RDFUnitUtils.fillSchemaServiceFromLOV() -> count = SchemaService.getSize() - count == " + count );

        LOGGER.info("Loaded " + count + " additional schema declarations from LOV SPARQL Endpoint");
    }



    /**
     * <p>getFirstItemInCollection.</p>
     *
     * @param collection a {@link java.util.Collection} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public static <T> T getFirstItemInCollection(Collection<T> collection) {
        return collection.stream().findFirst().orElseGet(null);
    }
}
