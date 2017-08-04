package org.aksw.rdfunit.sources;

import org.aksw.rdfunit.enums.TestAppliesTo;
import org.aksw.rdfunit.io.reader.RdfReader;
import org.aksw.rdfunit.io.reader.RdfReaderFactory;
import org.aksw.rdfunit.utils.StringUtils;
import org.aksw.rdfunit.utils.UriToPathUtils;

/**
 * <p>SourceFactory class.</p>
 *
 * @author Dimitris Kontokostas
 *         Source factory
 * @since 10/3/13 6:45 PM
 * @version $Id: $Id
 */
public final class SchemaSourceFactory {

    private SchemaSourceFactory() {
    }

    public static SchemaSource createSchemaSourceFromCache(String baseFolder, String prefix, String uri) { // String uri is the String namespace
        return createSchemaSourceFromCache(baseFolder, prefix, uri, uri);
    }
    public static SchemaSource createSchemaSourceFromCache(String baseFolder, String prefix, String uri, String schema) { // (String testFolder, String prefix, String namespace, String isDefinedBy)
        String cacheFile = CacheUtils.getSchemaSourceCacheFilename(baseFolder, TestAppliesTo.Schema, prefix, uri); // The cache file address + file name
        RdfReader reader = RdfReaderFactory.createFileOrDereferenceReader(cacheFile, schema);
        // create a RdfStreamReader(cacheFile), create a RdfDereferenceReader( schema(uri) )
        // add these two readers into a RdfFirstSuccessReader(readers)
        // create a RdfWriter w = new RdfFileWriter(filename, true);
        // return a RdfReadAndCacheReader(rdfFirstSuccessReader, RdfWriter);

        return createSchemaSourceSimple(prefix, uri, schema, reader);
    }

    public static SchemaSource createSchemaSourceDereference(String prefix, String uri) { // (String prefix, String namespace)
        // prefix is processed uri (uri remove strange symbol). For some situations that user type namespace after -d
        return createSchemaSourceDereference(prefix, uri, uri);
    }
    public static SchemaSource createSchemaSourceDereference(String prefix, String uri, String schema) { // (String prefix, String namespace, String isDefinedBy)
        // In this case, schema = uri => that is, isDefinedBy = namespace

        return createSchemaSourceSimple(prefix, uri, schema, RdfReaderFactory.createDereferenceReader(schema)); // (String prefix, String namespace, String isDefinedBy)
        // RdfReaderFactory.createDereferenceReader(schema) return a RdfFirstSuccessReader(Collection<RdfReader> readers),
        // RdfFirstSuccessReader either contains a RdfDeferenceReader which has a uri (when uri does not exist)
        // or contains two RdfStreamReader each of which contains (InputStream, String format()) according to uri(called "filename" in RdfStreamReader()) (when uri does exist)
    }

    public static EnrichedSchemaSource createEnrichedSchemaSourceFromCache(String baseFolder, String prefix, String uri) {
        String cacheFile = CacheUtils.getSchemaSourceCacheFilename(baseFolder, TestAppliesTo.EnrichedSchema, prefix, uri);
        RdfReader reader = RdfReaderFactory.createFileOrDereferenceReader(cacheFile, uri);
        return new EnrichedSchemaSource(new SourceConfig(prefix, uri), reader);
    }

    public static SchemaSource createSchemaSourceFromText(String namespace, String text, String format) {

        String uri = namespace + StringUtils.getHashFromString(text);
        String prefix = UriToPathUtils.getAutoPrefixForURI(uri);

        return createSchemaSourceSimple(prefix, uri, RdfReaderFactory.createReaderFromText(text, format));
    }


    public static SchemaSource createSchemaSourceSimple(String uri) {

        return createSchemaSourceSimple(uri, RdfReaderFactory.createResourceOrFileOrDereferenceReader(uri));
    }


    public static SchemaSource createSchemaSourceSimple(String uri, RdfReader reader) {

        return createSchemaSourceSimple(UriToPathUtils.getAutoPrefixForURI(uri), uri, uri, reader);
    }

    public static SchemaSource createSchemaSourceSimple(String prefix, String uri, RdfReader reader) {

        return createSchemaSourceSimple(prefix, uri, uri, reader);
    }

    public static SchemaSource createSchemaSourceSimple(String prefix, String uri, String schema, RdfReader reader) { // String schema is URI or just prefix
                                                                                                                      // (String prefix, String namespace, String isDefinedBy, RdfReader reader)

        // SourceConfig stores prefix and uris => /* what exactly are them? */
        return new SchemaSource(new SourceConfig(prefix, uri), schema, reader);
    }

    public static SchemaSource copySchemaSource(SchemaSource schemaSource) {

        return new SchemaSource(schemaSource);
    }
}
