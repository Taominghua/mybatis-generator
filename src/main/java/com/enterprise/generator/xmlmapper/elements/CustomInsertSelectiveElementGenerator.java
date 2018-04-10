package com.enterprise.generator.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.GeneratedKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomInsertSelectiveElementGenerator extends AbstractXmlElementGenerator {


    public CustomInsertSelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("insert");

        answer.addAttribute(new Attribute("id", introspectedTable.getInsertSelectiveStatementId()));

        FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();

        answer.addAttribute(new Attribute("parameterType", parameterType.getFullyQualifiedName()));

        context.getCommentGenerator().addComment(answer);

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    answer.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    answer.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty()));
                    answer.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
                } else {
                    //只有表主键字段映射成java类型是Integer或者Long，并且是自增列的时候才会生成自增xml；如果表只有一个id字段，那么在insert里面则什么都不生成
                    List<String> needGeneratedKeyJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
                    if (needGeneratedKeyJavaTypeList.contains(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName()) && introspectedColumn.isAutoIncrement()) {
                        answer.addElement(this.getSelectKey(introspectedColumn, gk));
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("trim");
        insertTrimElement.addAttribute(new Attribute("prefix", "("));
        insertTrimElement.addAttribute(new Attribute("suffix", ")"));
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        answer.addElement(insertTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim");
        valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
        valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        answer.addElement(valuesTrimElement);


        for (IntrospectedColumn introspectedColumn : removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {

            if (introspectedColumn.isSequenceColumn() || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                // if it is a sequence column, it is not optional
                // This is required for MyBatis3 because MyBatis3 parses
                // and calculates the SQL before executing the selectKey

                // if it is primitive, we cannot do a null check
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertTrimElement.addElement(new TextElement(sb.toString()));

                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
                sb.append(',');
                valuesTrimElement.addElement(new TextElement(sb.toString()));

                continue;
            }

            sb.setLength(0);
//            sb.append(introspectedColumn.getJavaProperty());
//            sb.append(" != null");
            needSpellEmptyString(sb, introspectedColumn);

            XmlElement insertNotNullElement = new XmlElement("if");
            insertNotNullElement.addAttribute(new Attribute("test", sb.toString()));

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(',');
            insertNotNullElement.addElement(new TextElement(sb.toString()));
            insertTrimElement.addElement(insertNotNullElement);

            sb.setLength(0);
//            sb.append(introspectedColumn.getJavaProperty());
//            sb.append(" != null");
            needSpellEmptyString(sb, introspectedColumn);

            XmlElement valuesNotNullElement = new XmlElement("if");
            valuesNotNullElement.addAttribute(new Attribute("test", sb.toString()));

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');
            valuesNotNullElement.addElement(new TextElement(sb.toString()));
            valuesTrimElement.addElement(valuesNotNullElement);
        }

        if (context.getPlugins().sqlMapInsertSelectiveElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }

    private void needSpellEmptyString(StringBuilder sb, IntrospectedColumn introspectedColumn) {
        //判断是否需要判断!=''
        List<String> needSpellEmptyStringJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
        if (needSpellEmptyStringJavaTypeList.contains(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
            sb.append(introspectedColumn.getJavaProperty()).append(" != null");
        } else if ("java.lang.String".equals(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
            sb.append(introspectedColumn.getJavaProperty().concat(" != null AND " + introspectedColumn.getJavaProperty() + " != ''"));
        } else {
            sb.append(introspectedColumn.getJavaProperty()).append(" != null");
        }
    }

    private static List<IntrospectedColumn> removeIdentityAndGeneratedAlwaysColumns(List<IntrospectedColumn> columns) {
        List<IntrospectedColumn> filteredList = new ArrayList<IntrospectedColumn>();
        for (IntrospectedColumn ic : columns) {
            //如果主键是String就需要将id拼到insert语句中；如果主键是Integer并且是自增就不需要拼到insert语句中
            List<String> needSpellIdIntoInsertJavaTypeList = Arrays.asList(new String[]{"java.lang.Integer", "java.lang.Long"});
            if (!(ic.isIdentity() && ic.isAutoIncrement() && needSpellIdIntoInsertJavaTypeList.contains(ic.getFullyQualifiedJavaType().getFullyQualifiedName()))) {
                filteredList.add(ic);
            }
        }
        return filteredList;
    }
}
