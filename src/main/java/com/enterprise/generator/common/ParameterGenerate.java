package com.enterprise.generator.common;

import org.mybatis.generator.api.IntrospectedColumn;

import java.util.regex.Pattern;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class ParameterGenerate {

    private static Pattern Enumerate_Matcher = Pattern.compile(".*@Enum\\((.*)\\).*");

    private static ParameterGenerate parameterGenerate = null;

    public static ParameterGenerate getInstance() {
        if (parameterGenerate == null) {
            return new ParameterGenerate();
        }
        return parameterGenerate;
    }

    /**
     * Gets the parameter clause.
     *
     * @param introspectedColumn the introspected column
     * @param prefix             the prefix
     * @return the parameter clause
     */
    public String getParameterClause(IntrospectedColumn introspectedColumn, String prefix) {
        StringBuilder sb = new StringBuilder();

        sb.append("#{"); //$NON-NLS-1$
//        Matcher matcher = Enumerate_Matcher.matcher(introspectedColumn.getRemarks());
//        if (matcher.find()) {
//            sb.append(introspectedColumn.getJavaProperty(prefix).concat(".type"));
//        } else {
        sb.append(introspectedColumn.getJavaProperty(prefix));
//        }
        sb.append(",jdbcType="); //$NON-NLS-1$
        sb.append(introspectedColumn.getJdbcTypeName());

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append(",typeHandler="); //$NON-NLS-1$
            sb.append(introspectedColumn.getTypeHandler());
        }

        sb.append('}');

        return sb.toString();
    }
}
