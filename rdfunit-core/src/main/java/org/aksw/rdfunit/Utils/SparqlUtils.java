package org.aksw.rdfunit.Utils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.rdfunit.tests.results.ResultAnnotation;

import java.util.ArrayList;


/**
 * User: Dimitris Kontokostas
 * Description
 * Created: 1/24/14 6:08 PM
 */
public class SparqlUtils {
    static public java.util.Collection<ResultAnnotation> getResultAnnotations(QueryExecutionFactory queryFactory, String uri) {
        java.util.Collection<ResultAnnotation> annotations = new ArrayList<ResultAnnotation>();
        String sparql = RDFUnitUtils.getAllPrefixes() +
                " SELECT ?annotationProperty ?annotationValue WHERE {" +
                " <" + uri + "> rut:resultAnnotation ?annotation . " +
                " ?annotation a rut:ResultAnnotation ; " +
                "   rut:annotationProperty ?annotationProperty ; " +
                "   rut:annotationValue ?annotationValue . } ";

        QueryExecution qe = null;

        try {
            qe = queryFactory.createQueryExecution(sparql);
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.next();
                String annotationProperty = qs.get("annotationProperty").toString();
                RDFNode annotationValue = qs.get("annotationValue");
                annotations.add(new ResultAnnotation(annotationProperty, annotationValue));
            }

        } catch (Exception e) {

        } finally {
            if (qe != null)
                qe.close();
        }

        return annotations;
    }

    public static boolean checkStatusForTimeout(QueryExceptionHTTP e) {
        int httpCode = e.getResponseCode();

        // 408,504,524 timeout codes from http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        if (httpCode == 408 || httpCode == 504 || httpCode == 524)
            return true;
        else
            return false;
    }
}