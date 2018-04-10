package com.enterprise.generator.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

/**
 * @author tommy
 */
public class CustomSelectByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    public CustomSelectByPrimaryKeyElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute("id", introspectedTable.getSelectByPrimaryKeyStatementId()));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        String parameterType;
        // PK fields are in the base class. If more than on PK
        // field, then they are coming in a map.
        if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
            parameterType = "map";
        } else {
            parameterType = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().toString();
        }

        answer.addAttribute(new Attribute("parameterType", parameterType));

        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("SELECT"));

        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("FROM ");

        //不用将所有字段列出来进行单表查询，直接引用定义的所有字段xml即可，以后即使表结构变动，改动量也很小，因为只需要修改引用xml即可
        XmlElement fieldInclude = new XmlElement("include");
        fieldInclude.addAttribute(new Attribute("refid", "all_fields"));
        answer.addElement(fieldInclude);

//        if (stringHasValue(introspectedTable.getSelectByPrimaryKeyQueryId())) {
//            sb.append('\'');
//            sb.append(introspectedTable.getSelectByPrimaryKeyQueryId());
//            sb.append("' as QUERYID,");
//        }
//
//        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
//        while (iter.hasNext()) {
//            sb.append(MyBatis3FormattingUtilities.getSelectListPhrase(iter.next()));
//
//            if (iter.hasNext()) {
//                sb.append(", ");
//            }
//
//            if (sb.length() > 80) {
//                answer.addElement(new TextElement(sb.toString()));
//                sb.setLength(0);
//            }
//        }

//        if (sb.length() > 0) {
//            answer.addElement(new TextElement(sb.toString()));
//        }

//        sb.setLength(0);
//        sb.append("FROM ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  AND ");
            } else {
                sb.append("WHERE ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapSelectByPrimaryKeyElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
