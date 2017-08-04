package org.aksw.rdfunit.model.readers;

import org.aksw.rdfunit.model.impl.PatternImpl;
import org.aksw.rdfunit.model.interfaces.Pattern;
import org.aksw.rdfunit.model.interfaces.PatternParameter;
import org.aksw.rdfunit.model.interfaces.ResultAnnotation;
import org.aksw.rdfunit.vocabulary.RDFUNITv;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCTerms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads an argument
 *
 * @author Dimitris Kontokostas
 * @since 6/17/15 5:07 PM
 * @version $Id: $Id
 */
public final class PatternReader implements ElementReader<Pattern> {

    private PatternReader(){}

    /**
     * <p>create.</p>
     *
     * @return a {@link org.aksw.rdfunit.model.readers.PatternReader} object.
     */
    public static PatternReader create() { return new PatternReader();}

    /** {@inheritDoc} */
    @Override
    public Pattern read(Resource resource) {
        // For this version of annotation, the resource comes from BatchPatternReader.java <- RDFUnit.java (during RDFUnit.init()),
        // RDFUnit.init() generate a model, and BatchPatternReader take all "Pattern" in model, and the Resource is one of those Pattern.

        checkNotNull(resource);

        PatternImpl.Builder patternBuilder = new PatternImpl.Builder();

        // resource in PatternImpl.Builder() is called Resource element.
        // store pattern (as the form of resource) in the PatternImpl.Builder.Resource element
        patternBuilder.setElement(resource);

        int count; // used to count duplicates

        /**

         private final String description;
         private final String sparqlWherePattern;
         private final String sparqlPatternPrevalence;
         private final Collection<PatternParameter> parameters;
         private final Collection<ResultAnnotation> annotations;
         */

        // get ID
        count = 0;
        for (Statement smt : resource.listProperties(DCTerms.identifier).toList()) {
            // resource.listProperties(DCTerms.identifier) return a StmtIterator, and this method lists all the values of the property p.
            // Returns an iterator over all the statements in the associated model whose subject is this resource and whose predicate is p.

            checkArgument(++count == 1, "Cannot have more than one identifier in Pattern %s", resource.getURI());
            patternBuilder.setId(smt.getObject().asLiteral().getLexicalForm());
        }

        // description
        count = 0;
        for (Statement smt : resource.listProperties(DCTerms.description).toList()) {
            checkArgument(++count == 1, "Cannot have more than one description in Pattern %s", resource.getURI());
            patternBuilder.setDescription(smt.getObject().asLiteral().getLexicalForm());
        }

        // SPARQL where
        count = 0;
        for (Statement smt : resource.listProperties(RDFUNITv.sparqlWherePattern).toList()) {
            checkArgument(++count == 1, "Cannot have more than one SPARQL query in Pattern %s", resource.getURI());
            patternBuilder.setSparqlWherePattern(smt.getObject().asLiteral().getLexicalForm());
        }

        // SPARQL prevalence
        count = 0;
        for (Statement smt : resource.listProperties(RDFUNITv.sparqlPrevalencePattern).toList()) {
            checkArgument(++count == 1, "Cannot have more than one prevalence query in Pattern %s", resource.getURI());
            patternBuilder.setSparqlPatternPrevalence(smt.getObject().asLiteral().getLexicalForm());
        }

        //parameters
        Collection<PatternParameter> patternParameters = resource.listProperties(RDFUNITv.parameter).toList().stream()
                // resource.listProperties(p) returns an iterator over all the statements in the associated model whose subject is this resource and whose predicate is p.

                .map(smt -> PatternParameterReader.create().read(smt.getResource())) // smt.getResource() return the object of the statement, in this case get the parameter.
                .collect(Collectors.toCollection(ArrayList::new));
        patternBuilder.setParameters(patternParameters);

        //annotations
        Collection<ResultAnnotation> patternAnnotations = resource.listProperties(RDFUNITv.resultAnnotation).toList().stream()
                .map(smt -> ResultAnnotationReader.create().read(smt.getResource())) // smt.getResource() return the object of the statement
                .collect(Collectors.toList());
        patternBuilder.setAnnotations(patternAnnotations);

        return patternBuilder.build(); /*MD*/   // return new PatternImpl(this);
    }
}
