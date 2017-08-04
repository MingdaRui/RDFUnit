package org.aksw.rdfunit.model.readers;

import org.aksw.rdfunit.model.interfaces.Pattern;
import org.aksw.rdfunit.vocabulary.RDFUNITv;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reader a set of Patterns
 *
 * @author Dimitris Kontokostas
 * @since 9/26/15 12:33 PM
 * @version $Id: $Id
 */
public final class BatchPatternReader {

    private BatchPatternReader(){}

    /**
     * <p>create.</p>
     *
     * @return a {@link BatchPatternReader} object.
     */
    public static BatchPatternReader create() { return new BatchPatternReader();}

    /**
     * <p>getPatternsFromModel.</p>
     *
     * @param model a {@link org.apache.jena.rdf.model.Model} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Pattern> getPatternsFromModel(Model model) {
        return getPatternsFromResourceList(
                model.listResourcesWithProperty(RDF.type, RDFUNITv.Pattern).toList()
                // ResIterator Model.listResourcesWithProperty(Property p, RDFNode o)
                // Answer an iterator [with no duplicates] over all the resources in this model that have property p with value o.
                // iterator.toList() returns a List.
        );
    }

    /**
     * <p>getPatternsFromResourceList.</p>
     *
     * @param resources a {@link java.util.List} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Pattern> getPatternsFromResourceList(List<Resource> resources) {
        return resources.stream() //This resources is all the "Pattern" in the model
                .map(resource -> PatternReader.create().read(resource))
                .collect(Collectors.toList());
        // 1. Java 8 new feature
        //    x.stream().map(y -> y.foo()).collect(Collectors.toList()); x is a list, convert every element in the x from y to y.foo().
        //    x is the original list, y is one of elements in x, y.foo() is one of elements in the new list. This operation return ths new list.
        // 2. PatternReader.create() returns a new PatternReader();
        //    PatternReader.read(resource) read information about pattern from resource, store those information in a Pattern object, and return the Pattern object.
        // 3. Finally, replace the original List<Resource> with Collection<Pattern>.
        //    To sum up, this method takes pattern resources and covert them to Pattern Object (actually it is PatternImpl)

    }
}
