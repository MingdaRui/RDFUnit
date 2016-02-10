package org.aksw.rdfunit.io.writer;


import org.aksw.rdfunit.io.format.FormatService;
import org.aksw.rdfunit.io.format.SerializationFormat;
import org.aksw.rdfunit.model.interfaces.results.TestExecution;

import java.io.OutputStream;


public final class RdfResultsWriterFactory {
    private RdfResultsWriterFactory() {
    }

    public static RDFWriter createWriterFromFormat(String filenameWithoutExtension, SerializationFormat serializationFormat, TestExecution testExecution) {
        if (serializationFormat.equals(FormatService.getOutputFormat("html"))) {
            return createHtmlWriter(testExecution, filenameWithoutExtension + "." + serializationFormat.getExtension());
        } else if (serializationFormat.equals(FormatService.getOutputFormat("junitxml"))) {
            return createJunitXmlWriter(testExecution, filenameWithoutExtension + "." + serializationFormat.getExtension());
        } else {
            return new RDFFileWriter(filenameWithoutExtension + "." + serializationFormat.getExtension(), serializationFormat.getName());
        }
    }


    public static RDFWriter createWriterFromFormat(OutputStream outputStream, SerializationFormat serializationFormat, TestExecution testExecution) {
        if (serializationFormat.equals(FormatService.getOutputFormat("html"))) {
            return createHtmlWriter(testExecution, outputStream);
        } else if (serializationFormat.equals(FormatService.getOutputFormat("junitxml"))) {
            return createJunitXmlWriter(testExecution, outputStream);
        } else {
            return new RDFStreamWriter(outputStream, serializationFormat.getName());
        }
    }


    public static RdfHtmlResultsWriter createHtmlWriter(TestExecution testExecution, String filename) {
        return createHtmlWriter(testExecution, RDFStreamWriter.getOutputStreamFromFilename(filename));
    }


    public static RdfHtmlResultsWriter createHtmlWriter(TestExecution testExecution, OutputStream outputStream) {
        switch (testExecution.getTestExecutionType()) {
            case statusTestCaseResult:
                return new RdfHtmlResultsStatusWriter(testExecution, outputStream);
            case aggregatedTestCaseResult:
                return new RdfHtmlResultsAggregateWriter(testExecution, outputStream);
            case shaclSimpleTestCaseResult:
                return new RdfHtmlResultsShaclWriter(testExecution, outputStream);
            case shaclFullTestCaseResult:
                // TODO extended not supported ATM, use RLOG instead
                return new RdfHtmlResultsShaclWriter(testExecution, outputStream);
            case rlogTestCaseResult:
                return new RdfHtmlResultsRlogWriter(testExecution, outputStream);
            case extendedTestCaseResult:
                // TODO extended not supported ATM, use RLOG instead
                return new RdfHtmlResultsRlogWriter(testExecution, outputStream);
            default:
                throw new IllegalArgumentException("Unsupported TestExecution in createHTMLWriter");
        }
    }
    
    public static JunitXmlResultsWriter createJunitXmlWriter(TestExecution testExecution, String filename) {
        return createJunitXmlWriter(testExecution, RDFStreamWriter.getOutputStreamFromFilename(filename));
    }
    
    public static JunitXmlResultsWriter createJunitXmlWriter(TestExecution testExecution, OutputStream outputStream) {
        switch (testExecution.getTestExecutionType()) {
        case statusTestCaseResult:
            return new JunitXmlResultsStatusWriter(testExecution, outputStream);
        case aggregatedTestCaseResult:
            return new JunitXmlResultsAggregateWriter(testExecution, outputStream);
    	case shaclFullTestCaseResult:
    		return new JunitXmlResultsShaclWriter(testExecution, outputStream);
    	case shaclSimpleTestCaseResult:
    		return new JunitXmlResultsShaclWriter(testExecution, outputStream);
    	case rlogTestCaseResult:
    		return new JunitXmlResultsRlogWriter(testExecution, outputStream);
    	case extendedTestCaseResult:
    		return new JunitXmlResultsRlogWriter(testExecution, outputStream);
        default:
            throw new IllegalArgumentException("Unsupported TestExecution in JunitXmlResultsWriter");
        }
    }
}
