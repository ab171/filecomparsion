package org.incava.diffj;

import java.io.*;
import java.util.*;
import net.sourceforge.pmd.ast.*;
import org.incava.analysis.*;
import org.incava.ijdk.io.*;
import org.incava.java.*;
import org.incava.qualog.Qualog;
import org.incava.ijdk.util.Arrays;
import org.incava.ijdk.util.TimedEvent;
import org.incava.ijdk.util.TimedEventSet;


/**
 * Compares two Java files.
 */
public class JavaFileDiff {

    private int exitValue;

    public JavaFileDiff(Report report, String fromName, String fromStr, String fromSource, String toName, String toStr, String toSource, boolean flushReport) {
        exitValue = 0;

        ASTCompilationUnit fromCu = compile(fromName, new StringReader(fromStr), fromSource);
        ASTCompilationUnit toCu   = compile(toName,   new StringReader(toStr),   toSource);
        
        report.reset(fromName, fromStr, toName, toStr);
            
        CompilationUnitDiff cud = new CompilationUnitDiff(report, flushReport);
        // chew the cud here ...
        cud.compare(fromCu, toCu);
        
        if (report.getDifferences().size() > 0) {
            exitValue = 1;
        }

        if (flushReport) {
            report.flush();
        }
    }

    public static ASTCompilationUnit compile(Reader rdr, String sourceVersion) throws Exception {
        JavaCharStream jcs = new JavaCharStream(rdr);

        JavaParser parser = new JavaParser(jcs);
        
        if (sourceVersion.equals(Java.SOURCE_1_3)) {
            parser.setJDK13();
        }
        else if (sourceVersion.equals(Java.SOURCE_1_4)) {
            // nothing.
        }
        else if (sourceVersion.equals(Java.SOURCE_1_5) || sourceVersion.equals(Java.SOURCE_1_6)) {
            // no setJDK16 yet in PMD
            parser.setJDK15();
        }
        else {
            //            ("ERROR: source version '" + sourceVersion + "' not recognized");
            return null;
        }
        
        return parser.CompilationUnit();
    }

    protected ASTCompilationUnit compile(String name, Reader rdr, String sourceVersion) {
        try {
            ASTCompilationUnit cu = compile(rdr, sourceVersion);

            if (cu == null) {
                System.err.println("ERROR: source version '" + sourceVersion + "' not recognized");
            }
            
            return cu;
        }
        catch (TokenMgrError tme) {
            System.out.println("Error parsing (tokenizing) " + name + ": " + tme.getMessage());
            exitValue = 1;
            return null;
        }
        catch (ParseException e) {
            System.out.println("Parse error in " + name + ": " + e.getMessage());
            exitValue = -1;
            return null;
        }
        catch (Exception e) {
            System.out.println("Exception in " + name + ": " + e.getMessage());
            exitValue = -1;
            return null;
        }
    }

}
