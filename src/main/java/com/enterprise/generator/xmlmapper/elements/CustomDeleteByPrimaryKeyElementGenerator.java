package com.enterprise.generator.xmlmapper.elements;

import com.enterprise.generator.common.ParameterGenerate;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

public class CustomDeleteByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    private boolean isSimple;

    public CustomDeleteByPrimaryKeyElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("delete");

        answer.addAttribute(new Attribute("id", introspectedTable.getDeleteByPrimaryKeyStatementId()));
        String parameterClass;
        if (!isSimple && introspectedTable.getRules().generatePrimaryKeyClass()) {
            parameterClass = introspectedTable.getPrimaryKeyType();
        } else {
            // PK fields are in the base class. If more than on PK
            // field, then they are coming in a map.
            if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
                parameterClass = "map";
            } else {
                parameterClass = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().toString();
            }
        }
        answer.addAttribute(new Attribute("parameterType", parameterClass));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
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

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(ParameterGenerate.getInstance().getParameterClause(introspectedColumn,null));
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapDeleteByPrimaryKeyElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
