package com.enterprise.generator.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

public class CustomSelectAllElementGenerator extends AbstractXmlElementGenerator {

    public CustomSelectAllElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute("id", introspectedTable.getSelectAllStatementId()));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("SELECT"));

        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("FROM ");

        //不用将所有字段列出来进行单表查询，直接引用定义的所有字段xml即可，以后即使表结构变动，改动量也很小，因为只需要修改引用xml即可
        XmlElement fieldInclude = new XmlElement("include");
        fieldInclude.addAttribute(new Attribute("refid", "all_fields"));
        answer.addElement(fieldInclude);

//        StringBuilder sb = new StringBuilder();
//        sb.append("SELECT ");
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
//
//        if (sb.length() > 0) {
//            answer.addElement(new TextElement(sb.toString()));
//        }
//
//        sb.setLength(0);
//        sb.append("FROM ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        String orderByClause = introspectedTable.getTableConfigurationProperty(PropertyRegistry.TABLE_SELECT_ALL_ORDER_BY_CLAUSE);
        boolean hasOrderBy = StringUtility.stringHasValue(orderByClause);
        if (hasOrderBy) {
            sb.setLength(0);
            sb.append("ORDER BY ");
            sb.append(orderByClause);
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapSelectAllElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
