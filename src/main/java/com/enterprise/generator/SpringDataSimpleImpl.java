package com.enterprise.generator;

import com.enterprise.generator.javamapper.EmptyJavaClientGenerator;
import com.enterprise.generator.model.SpringDataModelGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.SimpleXMLMapperGenerator;

import java.util.List;


/**
 * @author tommy
 */
public class SpringDataSimpleImpl extends IntrospectedTableMyBatis3Impl {
    public SpringDataSimpleImpl() {
        super();
    }

    private void setSpringDataCommon() {
        setSelectAllStatementId("list");
        setDeleteByPrimaryKeyStatementId("deleteById");
        setSelectByPrimaryKeyStatementId("getById");
        setUpdateByPrimaryKeyStatementId("updateById");
    }


//    protected void calculateXmlAttributes() {
//        this.setIbatis2SqlMapPackage(this.calculateSqlMapPackage());
//        this.setIbatis2SqlMapFileName(this.calculateIbatis2SqlMapFileName());
//        this.setMyBatis3XmlMapperFileName(this.calculateMyBatis3XmlMapperFileName());
//        this.setMyBatis3XmlMapperPackage(this.calculateSqlMapPackage());
//        this.setIbatis2SqlMapNamespace(this.calculateIbatis2SqlMapNamespace());
//        this.setMyBatis3FallbackSqlMapNamespace(this.calculateMyBatis3FallbackSqlMapNamespace());
//        this.setSqlMapFullyQualifiedRuntimeTableName(this.calculateSqlMapFullyQualifiedRuntimeTableName());
//        this.setSqlMapAliasedFullyQualifiedRuntimeTableName(this.calculateSqlMapAliasedFullyQualifiedRuntimeTableName());
//        this.setCountByExampleStatementId("countByExample");
//        this.setDeleteByExampleStatementId("deleteByExample");
//        this.setDeleteByPrimaryKeyStatementId("deleteByPrimaryKey");
//        this.setInsertStatementId("insert");
//        this.setInsertSelectiveStatementId("insertSelective");
//        this.setSelectAllStatementId("selectAll");
//        this.setSelectByExampleStatementId("selectByExample");
//        this.setSelectByExampleWithBLOBsStatementId("selectByExampleWithBLOBs");
//        this.setSelectByPrimaryKeyStatementId("selectByPrimaryKey");
//        this.setUpdateByExampleStatementId("updateByExample");
//        this.setUpdateByExampleSelectiveStatementId("updateByExampleSelective");
//        this.setUpdateByExampleWithBLOBsStatementId("updateByExampleWithBLOBs");
//        this.setUpdateByPrimaryKeyStatementId("updateByPrimaryKey");
//        this.setUpdateByPrimaryKeySelectiveStatementId("updateByPrimaryKeySelective");
//        this.setUpdateByPrimaryKeyWithBLOBsStatementId("updateByPrimaryKeyWithBLOBs");
//        this.setBaseResultMapId("BaseResultMap");
//        this.setResultMapWithBLOBsId("ResultMapWithBLOBs");
//        this.setExampleWhereClauseId("Example_Where_Clause");
//        this.setBaseColumnListId("Base_Column_List");
//        this.setBlobColumnListId("Blob_Column_List");
//        this.setMyBatis3UpdateByExampleWhereClauseId("Update_By_Example_Where_Clause");
//    }

    @Override
    protected void calculateXmlMapperGenerator(AbstractJavaClientGenerator javaClientGenerator, List<String> warnings, ProgressCallback progressCallback) {
        setSpringDataCommon();

        if (javaClientGenerator == null) {
            if (context.getSqlMapGeneratorConfiguration() != null) {
                xmlMapperGenerator = new SimpleXMLMapperGenerator();
            }
        } else {
            xmlMapperGenerator = javaClientGenerator.getMatchedXMLGenerator();
        }

        initializeAbstractGenerator(xmlMapperGenerator, warnings, progressCallback);
    }

    @Override
    protected AbstractJavaClientGenerator createJavaClientGenerator() {
        if (context.getJavaClientGeneratorConfiguration() == null) {
            return null;
        }

        return new EmptyJavaClientGenerator();
    }

    @Override
    protected void calculateJavaModelGenerators(List<String> warnings, ProgressCallback progressCallback) {
        AbstractJavaGenerator javaGenerator = new SpringDataModelGenerator();
        initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
        javaModelGenerators.add(javaGenerator);
    }
}
