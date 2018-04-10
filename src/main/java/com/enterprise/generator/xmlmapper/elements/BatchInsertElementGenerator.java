package com.enterprise.generator.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.GeneratedKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author tommy
 */
public class BatchInsertElementGenerator extends AbstractXmlElementGenerator {

    private boolean isSimple;

    public BatchInsertElementGenerator(boolean isSimple) {
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("insert");
        answer.addAttribute(new Attribute("id", "batchInsert"));

        answer.addAttribute(new Attribute("parameterType", "java.util.List"));
        this.context.getCommentGenerator().addComment(answer);
        GeneratedKey gk = this.introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn insertClause = this.introspectedTable.getColumn(gk.getColumn());
            if (insertClause != null) {
                if (gk.isJdbcStandard()) {
                    answer.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    answer.addAttribute(new Attribute("keyProperty", insertClause.getJavaProperty()));
                } else {
                    //只有表主键字段映射成java类型是Integer或者Long，并且是自增列的时候才会生成自增xml；如果表自由一个id字段，那么在insert里面则什么都不生成
                    List<String> needGeneratedKeyJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
                    if (needGeneratedKeyJavaTypeList.contains(insertClause.getFullyQualifiedJavaType().getFullyQualifiedName()) && insertClause.isAutoIncrement()) {
                        answer.addElement(this.getSelectKey(insertClause, gk));
                    }
                }
            }
        }

        StringBuilder insertClause1 = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();
        insertClause1.append("INSERT INTO ");
        insertClause1.append(this.introspectedTable.getFullyQualifiedTableNameAtRuntime());
        insertClause1.append(" (");

        //add foreach element
        XmlElement forEach = new XmlElement("foreach");
        forEach.addAttribute(new Attribute("collection", "list"));
        forEach.addAttribute(new Attribute("item", "item"));
        forEach.addAttribute(new Attribute("index", "index"));
        forEach.addAttribute(new Attribute("separator", ","));


        valuesClause.append("(");
        ArrayList valuesClauses = new ArrayList();
        Iterator iter = this.introspectedTable.getAllColumns().iterator();

        while (iter.hasNext()) {
            IntrospectedColumn next = (IntrospectedColumn) iter.next();
            //如果主键是String就需要将id拼到insert语句中；如果主键是Integer并且是自增就不需要拼到insert语句中
            List<String> needSpellIdIntoInsertJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});

            if (!(next.isIdentity() && next.isAutoIncrement() && needSpellIdIntoInsertJavaTypeList.contains(next.getFullyQualifiedJavaType().getFullyQualifiedName()))) {
                insertClause1.append(MyBatis3FormattingUtilities.getEscapedColumnName(next));
                valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(next, "item."));
                if (iter.hasNext()) {
                    insertClause1.append(", ");
                    valuesClause.append(", ");
                }

                if (valuesClause.length() > 80) {
                    answer.addElement(new TextElement(insertClause1.toString()));
                    insertClause1.setLength(0);
                    OutputUtilities.xmlIndent(insertClause1, 1);
                    valuesClauses.add(valuesClause.toString());
                    valuesClause.setLength(0);
                    OutputUtilities.xmlIndent(valuesClause, 1);
                }
            }
        }

        insertClause1.append(") VALUES");
        answer.addElement(new TextElement(insertClause1.toString()));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());
        Iterator i$1 = valuesClauses.iterator();
        while (i$1.hasNext()) {
            String clause = (String) i$1.next();
            forEach.addElement(new TextElement(clause));
        }
        answer.addElement(forEach);


        if (this.context.getPlugins().sqlMapInsertElementGenerated(answer, this.introspectedTable)) {
            parentElement.addElement(answer);
        }

    }
}
