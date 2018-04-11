package com.enterprise.generator.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Arrays;
import java.util.List;

/**
 * @author tommy
 */
public class CustomUpdateBySelectiveElementGenerator extends AbstractXmlElementGenerator {

    public CustomUpdateBySelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update");

        answer.addAttribute(new Attribute("id", "updateBySelective"));

        String parameterType;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }

        answer.addAttribute(new Attribute("parameterType", parameterType));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("SET");
        answer.addElement(dynamicElement);

        for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())) {
            sb.setLength(0);
//            sb.append(introspectedColumn.getJavaProperty());
//            sb.append(" != null");

            //判断是否需要判断!=''
            List<String> needSpellEmptyStringJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
            if (needSpellEmptyStringJavaTypeList.contains(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
                sb.append(introspectedColumn.getJavaProperty().concat(" != null"));
            } else if ("java.lang.String".equals(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
                sb.append(introspectedColumn.getJavaProperty().concat(" != null AND " + introspectedColumn.getJavaProperty() + " != ''"));
            } else {
                sb.append(introspectedColumn.getJavaProperty().concat(" != null"));
            }

            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  AND ");
            } else {
                sb.append("WHERE ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapUpdateByPrimaryKeySelectiveElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }


//
//    public CustomUpdateBySelectiveElementGenerator() {
//        super();
//    }
//
//    @Override
//    public void addElements(XmlElement parentElement) {
//        XmlElement answer = new XmlElement("update");
//
//        answer.addAttribute(new Attribute("id", "updateBySelective"));
//
//        answer.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
//
//        context.getCommentGenerator().addComment(answer);
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("UPDATE ");
//        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
//        answer.addElement(new TextElement(sb.toString()));
//
//        XmlElement dynamicElement = new XmlElement("SET");
//        answer.addElement(dynamicElement);
//
//        for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
//            sb.setLength(0);
//
//            //判断是否需要判断!=''
//            List<String> needSpellEmptyStringJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
//            if (needSpellEmptyStringJavaTypeList.contains(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
//                sb.append(introspectedColumn.getJavaProperty().concat(" != null"));
//            } else if ("java.lang.String".equals(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
//                sb.append(introspectedColumn.getJavaProperty().concat(" != null AND " + introspectedColumn.getJavaProperty() + " != ''"));
//            } else {
//                sb.append(introspectedColumn.getJavaProperty().concat(" != null"));
//            }
//
////            sb.append(introspectedColumn.getJavaProperty("record."));
////            sb.append(" != null");
//            XmlElement isNotNullElement = new XmlElement("if");
//            isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
//            dynamicElement.addElement(isNotNullElement);
//
//            sb.setLength(0);
//            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
//            sb.append(" = ");
//            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
//            sb.append(',');
//
//            isNotNullElement.addElement(new TextElement(sb.toString()));
//        }
//
//        answer.addElement(getUpdateByExampleIncludeElement());
//
//        if (context.getPlugins().sqlMapUpdateByExampleSelectiveElementGenerated(answer, introspectedTable)) {
//            parentElement.addElement(answer);
//        }
//    }
}
