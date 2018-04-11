package com.enterprise.generator.javamapper;

import com.enterprise.generator.xmlmapper.SimpleWithSqlMapperGenerator;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author tommy
 */
public class EmptyJavaClientGenerator extends AbstractJavaClientGenerator {

    /**
     * constructor
     */
    public EmptyJavaClientGenerator() {
        super(true);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        progressCallback.startTask(getString("Progress.17", introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);

        String importPackage = "com.enterprise.data.mapper.CrudMapper";
        FullyQualifiedJavaType javaType = new FullyQualifiedJavaType(importPackage.substring(importPackage.lastIndexOf(".") + 1, importPackage.length()));
        interfaze.addImportedType(new FullyQualifiedJavaType(importPackage));

        String importDomainPackage = introspectedTable.getBaseRecordType();
        interfaze.addImportedType(new FullyQualifiedJavaType(importDomainPackage));


        javaType.addTypeArgument(new FullyQualifiedJavaType(importDomainPackage.substring(importDomainPackage.lastIndexOf(".") + 1, importDomainPackage.length())));

        // 根据数据库主键字段类型判断接口类操作的java类型,因为不同的表主键可能是不同的jdbcType,所以需要根据自己的需求进行扩展
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();

        if (primaryKeyColumns != null && !primaryKeyColumns.isEmpty()) {
            for (int i = 0; i < primaryKeyColumns.size(); i++) {
                IntrospectedColumn key = primaryKeyColumns.get(i);
                if ("id".equals(key.getActualColumnName())) {
                    if ("VARCHAR".equals(key.getJdbcTypeName())) {
                        javaType.addTypeArgument(new FullyQualifiedJavaType("String"));
                    } else if ("INTEGER".equals(key.getJdbcTypeName())) {
                        javaType.addTypeArgument(new FullyQualifiedJavaType("Integer"));
                    } else if ("BIGINT".equals(key.getJdbcTypeName())) {
                        javaType.addTypeArgument(new FullyQualifiedJavaType("Long"));
                    }
                }
            }
        }

        interfaze.addSuperInterface(javaType);
        commentGenerator.addJavaFileComment(interfaze);

        String rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (!stringHasValue(rootInterface)) {
            rootInterface = context.getJavaClientGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }

        if (stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        }

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().clientGenerated(interfaze, null, introspectedTable)) {
            answer.add(interfaze);
        }

        List<CompilationUnit> extraCompilationUnits = getExtraCompilationUnits();
        if (extraCompilationUnits != null) {
            answer.addAll(extraCompilationUnits);
        }

        return answer;
    }

    public List<CompilationUnit> getExtraCompilationUnits() {
        return null;
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return new SimpleWithSqlMapperGenerator();
    }
}
