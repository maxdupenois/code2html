/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code2html.parsers;

import code2html.Parser;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mpd209
 */
public class JavaParser extends Parser{

    public JavaParser(String inFile, String outFile) throws IOException {
        super(inFile, outFile);
    }


    @Override
    protected void convert() {
        replace("\\&", "&amp;");
        replace("\\<", "&lt;");
        replace("\\>", "&gt;");
        replace("\\n|\\r", "<br/>\n");
        replace("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        replace(" {2}", "&nbsp;&nbsp;");

        doubleQuotesSpan("java_doublequotes");
        singleQuotesSpan("java_singlequotes");

        keywordSpan("class", "java_class");
        keywordSpan("return", "java_return");
        keywordSpan("public", "java_modifier");
        keywordSpan("private", "java_modifier");
        keywordSpan("protected", "java_modifier");
        keywordSpan("void", "java_modifier");
        keywordSpan("static", "java_modifier");
        keywordSpan("super", "java_super");
        keywordSpan("this", "java_this");
        keywordSpan("new", "java_new");
        keywordSpan("throw", "java_throw");
        keywordSpan("throws", "java_throw");
        keywordSpan("if", "java_if");
        keywordSpan("else", "java_if");
        keywordSpan("package", "java_package");
        keywordSpan("import", "java_import");
        keywordSpan("switch", "java_switch");
        keywordSpan("case", "java_switch");
        keywordSpan("break", "java_switch");
        keywordSpan("default", "java_switch");
        keywordSpan("int", "java_primitive");
        keywordSpan("double", "java_primitive");
        keywordSpan("float", "java_primitive");
        keywordSpan("char", "java_primitive");
        keywordSpan("boolean", "java_primitive");
        functionName();
        
        environmentSpan("\\/\\*", "\\*\\/", "java_bigcomment");
        environmentSpan("\\/\\/", "(?=\\n)", "java_linecomment");


    }
    private void functionName(){
        String regex = "(\\w\\w*)(?=\\()";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(new String(original));
        while (m.find()) {
            if(inString(m.start()))continue;
            addInsert(m.start(), "<span class=\"java_functionname\">");
            addInsert(m.end(), "</span>");
        }
    }

}
