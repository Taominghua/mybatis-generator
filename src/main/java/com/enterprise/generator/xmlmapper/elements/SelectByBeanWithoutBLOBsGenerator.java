package com.enterprise.generator.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author tommy
 */
public class SelectByBeanWithoutBLOBsGenerator extends AbstractXmlElementGenerator {
    @Override
    public void addElements(XmlElement parentElement) {

        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute("id", "selectByEntityWhere"));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        answer.addAttribute(new Attribute("parameterType", parameterType.getFullyQualifiedName()));

        context.getCommentGenerator().addComment(answer);
        answer.addElement(new TextElement("SELECT"));

        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("FROM ");

        XmlElement fieldInclude = new XmlElement("include");
        fieldInclude.addAttribute(new Attribute("refid", "all_fields"));
        answer.addElement(fieldInclude);

        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        sb.append(" \n\tWHERE 1=1 ");
        answer.addElement((new TextElement(sb.toString())));

        sb.setLength(0);
        Iterator<IntrospectedColumn> iter = introspectedTable.getNonBLOBColumns().iterator();
        XmlElement ifElement = null;
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            ifElement = new XmlElement("if");

            //判断是否需要判断!=''
            List<String> needSpellEmptyStringJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
            if (needSpellEmptyStringJavaTypeList.contains(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
                ifElement.addAttribute(new Attribute("test ", introspectedColumn.getJavaProperty().concat(" != null")));
            } else if ("java.lang.String".equals(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
                ifElement.addAttribute(new Attribute("test ", introspectedColumn.getJavaProperty().concat(" != null AND " + introspectedColumn.getJavaProperty() + " != ''")));
            } else {
                ifElement.addAttribute(new Attribute("test ", introspectedColumn.getJavaProperty().concat(" != null")));
            }
            sb.append(" AND ");
            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, ""));

            ifElement.addElement(new TextElement(sb.toString()));
            answer.addElement(ifElement);

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

//        ifElement = new XmlElement("if"); //$NON-NLS-1$
//        ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
//        ifElement.addElement(new TextElement("order by ${orderByClause}")); //$NON-NLS-1$
//        answer.addElement(ifElement);

        if (context.getPlugins().sqlMapSelectByExampleWithoutBLOBsElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
