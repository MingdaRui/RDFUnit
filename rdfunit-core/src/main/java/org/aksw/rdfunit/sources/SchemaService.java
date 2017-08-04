package org.aksw.rdfunit.sources;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.aksw.rdfunit.exceptions.UndefinedSchemaException;
import org.aksw.rdfunit.utils.UriToPathUtils;

import java.util.*;

/**
 * Holds all the schema definitions
 * It is usually instantiated from LOV on app startup
 *
 * @author Dimitris Kontokostas
 * @since 10/2/13 12:24 PM
 * @version $Id: $Id
 */
public final class SchemaService {
    /**
     * Creates a Bi-Directional map between prefix & namespace
     */
    private static final  BiMap<String, String> schemata = HashBiMap.create(); // prefix and namespace

    /**
     * if namespace is different from the ontology uri, we keep it in this map
     */
    private static final Map<String,String> definedBy = new HashMap<>(); // namespace and definedby

    private SchemaService() {
    }

    /**
     * <p>getSize.</p>
     *
     * @return a int.
     */
    public static int getSize() {return schemata.size();}

    /**
     * <p>addSchemaDecl.</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param uri a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     */
    public static void addSchemaDecl(String prefix, String uri, String url) {
        schemata.forcePut(prefix, uri); // prefix, Namespace
        definedBy.put(uri, url); // Namespace, Definedby
    }

    /**
     * <p>addSchemaDecl.</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param uri a {@link java.lang.String} object.
     */
    public static void addSchemaDecl(String prefix, String uri) {
        schemata.put(prefix, uri);
    }

    /**
     * <p>getSourceFromUri.</p>
     *
     * @param uri a {@link java.lang.String} object.
     * @return a {@link org.aksw.rdfunit.sources.SchemaSource} object.
     */
    public static Optional<SchemaSource> getSourceFromUri(String uri) {
        return getSourceFromUri(null, uri);
    }

    /**
     * <p>getSourceFromUri.</p>
     *
     * @param baseFolder a {@link java.lang.String} object.
     * @param uri a {@link java.lang.String} object.
     * @return a {@link org.aksw.rdfunit.sources.SchemaSource} object.
     */
    public static Optional<SchemaSource> getSourceFromUri(String baseFolder, String uri) {
        String prefix = schemata.inverse().get(uri);
        if (prefix == null) {
            return Optional.empty();
        }

        return getSourceFromPrefix(baseFolder, prefix);
    }

    /**
     * <p>getSourceFromPrefix.</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return a {@link org.aksw.rdfunit.sources.SchemaSource} object.
     */
    public static Optional<SchemaSource> getSourceFromPrefix(String prefix) {
        return getSourceFromPrefix(null, prefix);
    }

    /**
     * <p>getSourceFromPrefix.</p>
     *
     * @param baseFolder a {@link java.lang.String} object.
     * @param prefix a {@link java.lang.String} object.
     * @return a {@link org.aksw.rdfunit.sources.SchemaSource} object.
     */
    public static Optional<SchemaSource> getSourceFromPrefix(String baseFolder, String prefix) {

        //find if prefix & namespace has already stored
        String namespace = schemata.get(prefix);

        // if there is no prefix namespace pair in schemata
        if (namespace == null) {

            // return a SchemaSource, with a SourceConfig(SchemaService.schemata.prefix, SchemaService.schemata.namespace), a String schema(which is SchemaService.definedBy.definedBy), a RdfReader RdfFirstSuccessReader (which reads in the definedBy) initialized
            return getDereferenceSchemaSource(prefix);
        }

        String isDefinedBy = definedBy.get(namespace);
        if (isDefinedBy != null && !isDefinedBy.isEmpty()) {
            return getSchemaSourceWithDefinedBy(baseFolder, prefix, namespace, isDefinedBy);
        } else {
            return getSchemaSourceFromNs(baseFolder, prefix, namespace);
        }
    }

    private static Optional<SchemaSource> getDereferenceSchemaSource(String prefix) { // prefix is one of the -s parameter
        // If not a prefix, try to dereference it
        if (prefix.contains("/") || prefix.contains("\\")) {
            return  Optional.of(
                    SchemaSourceFactory
                            .createSchemaSourceDereference(UriToPathUtils.getAutoPrefixForURI(prefix), prefix));
            // UriToPathUtils.getAutoPrefixForURI(prefix) => Get rid of "https://" and replace "[?\"'\\/<>*|:#,&]" with "_"
            // Actually, it is: SchemaSourceFactory.createSchemaSourceDereference(String prefix, String uri); or (String prefix, String namespace)?
            // Eventually, SchemaSourceFactory will return a SchemaSource, with a SourceConfig(String prefix, String uri), a String schema (a uri or a prefix depend on what user inputs), a RdfReader schemaReader
            // (and a Model which initialed in somewhere else)
        }


        return Optional.empty();

    }

    private static Optional<SchemaSource> getSchemaSourceWithDefinedBy(String baseFolder, String prefix, String namespace, String isDefinedBy) {
        if (baseFolder != null) { //
            return Optional.of(SchemaSourceFactory.createSchemaSourceFromCache(baseFolder, prefix, namespace, isDefinedBy));
            // Actually, SchemaSourceFactory.createSchemaSourceFromCache(String baseFolder, String prefix, String uri, String schema);

        } else {
            return Optional.of(SchemaSourceFactory.createSchemaSourceDereference(prefix, namespace, isDefinedBy));
        }
    }

    private static Optional<SchemaSource> getSchemaSourceFromNs(String baseFolder, String prefix, String namespace) {
        if (baseFolder != null) { // Or we can say testFolder
            return Optional.of(SchemaSourceFactory.createSchemaSourceFromCache(baseFolder, prefix, namespace));
        } else {
            return Optional.of(SchemaSourceFactory.createSchemaSourceDereference(prefix, namespace));
        }
    }

    /**
     * <p>getSourceList.</p>
     *
     * @param baseFolder a {@link java.lang.String} object.
     * @param prefixes a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     * @throws org.aksw.rdfunit.exceptions.UndefinedSchemaException if any.
     */

    // For first time, it is getSourceList(String RDFUnitConfiguration.testFolder, Collection<String> "-s"SchemaPrefix)
    public static Collection<SchemaSource> getSourceList(String baseFolder, Collection<String> prefixes) throws UndefinedSchemaException {
        Collection<SchemaSource> sources = new ArrayList<>();
        for (String id : prefixes) {

            // return a SchemaSource, contains a SourceConfig(SchemaService.schemata.prefix, SchemaService.schemata.namespace), a String schema(which is SchemaService.definedBy.definedBy), a RdfReader RdfFirstSuccessReader (which reads in the definedBy) initialized
            Optional<SchemaSource> src = getSourceFromPrefix(baseFolder, id.trim());
            if (src.isPresent()) {
                sources.add(src.get());
            } else {
                throw new UndefinedSchemaException(id);
            }
        }
        return sources;
    }

    /**
     * <p>getSourceListAll.</p>
     *
     * @param fileCache a boolean.
     * @param baseFolder a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     * @throws org.aksw.rdfunit.exceptions.UndefinedSchemaException if any.
     */
    public static Collection<SchemaSource> getSourceListAll(boolean fileCache, String baseFolder) throws UndefinedSchemaException {
        Collection<String> prefixes = new ArrayList<>();
        prefixes.addAll(schemata.keySet());

        if (fileCache) {
            return getSourceList(baseFolder, prefixes);
        } else {
            return getSourceList(null, prefixes);
        }
    }


    /*******************************************************/
    // Dada Code Strat

    public static Map<String, String> mdGetAllMapsInSchemaService() {

        Map<String, String> mdAllMapsInSchemaService = new HashMap<>();

        mdAllMapsInSchemaService.putAll(schemata);
//        mdAllMapsInSchemaService.putAll(definedBy);

        return mdAllMapsInSchemaService;

    }

    // Data Code Finish
    /*******************************************************/


}

