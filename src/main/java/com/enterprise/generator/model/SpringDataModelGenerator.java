package com.enterprise.generator.model;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author tommy
 */
public class SpringDataModelGenerator extends AbstractJavaGenerator {

    private static Pattern Number_Range_Matcher = Pattern.compile(".*@Valid\\((.*),(.*)\\).*");

    private static Pattern Boolean_Matcher = Pattern.compile(".*@Boolean.*");

    private static Pattern Enumerate_Matcher = Pattern.compile(".*@Enum\\((.*)\\).*");

    public SpringDataModelGenerator() {
        super();
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString()));
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);

        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }

        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass);

            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }

        // add lombok
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.Data"));
        topLevelClass.addAnnotation("@Data");

        // add java.io.Serializable
        FullyQualifiedJavaType serializableType = new FullyQualifiedJavaType("java.io.Serializable");
        topLevelClass.addImportedType(serializableType);
        topLevelClass.addSuperInterface(serializableType);

        String rootClass = getRootClass();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
                continue;
            }

            Field field = getJavaBeansField(introspectedColumn);
            switch (field.getType().getShortName()) {
                case "Integer":
                    field.setType(new FullyQualifiedJavaType("Integer"));
                    addNumberValidate(topLevelClass, field, introspectedColumn);
                    break;
                case "String":
                    topLevelClass.addImportedType("org.hibernate.validator.constraints.Length");
                    field.addAnnotation(String.format("@Length(max = %d)", introspectedColumn.getLength()));
                    break;
                case "Long":
                    field.setType(new FullyQualifiedJavaType("Long"));
                    addNumberValidate(topLevelClass, field, introspectedColumn);
                    break;
                case "Date":
                    //如果对于Date操作工具类够强大的话，建议使用原声的Date
                    field.setType(new FullyQualifiedJavaType("java.util.Date"));
                    break;
                default:
            }

            String remarks = introspectedColumn.getRemarks();
            if (Boolean_Matcher.matcher(remarks).find()) {
                field.setType(new FullyQualifiedJavaType("Boolean"));
            } else {
                Matcher matcher = Enumerate_Matcher.matcher(remarks);
                if (matcher.find()) {
                    TopLevelEnumeration enumeration = addEnumerateClass(answer, matcher.group(1), field, topLevelClass);
                    if (enumeration != null) {
                        field.setType(enumeration.getType());
                    }
                }
            }

            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }
        }


        if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        return answer;
    }

    private TopLevelEnumeration addEnumerateClass(List<CompilationUnit> answer, String enumString, Field field, TopLevelClass topLevelClass) {
        FullyQualifiedJavaType topEnumType = getTopEnumType(enumString, field, topLevelClass);
        if (topEnumType == null) {
            return null;
        }

        TopLevelEnumeration enumClass = new TopLevelEnumeration(topEnumType);
        enumClass.setVisibility(JavaVisibility.PUBLIC);


        String enumStr = enumString;
        if (enumStr.indexOf(":") > 0) {
            // no enum, return immediate
            if (enumString.split(":").length == 1) {
                return enumClass;
            } else {
                enumStr = enumString.split(":")[1].trim();
            }
        }

        // add enum item
        Arrays.asList(enumStr.split(";")).forEach(x -> {
            String[] splitEnum = x.split(",");
            if (splitEnum.length == 3) {
                enumClass.addEnumConstant(String.format("\n    /**\n     * %s\n     */\n    %s(%s, \"%s\")", splitEnum[2].trim(), splitEnum[1].toUpperCase().trim(), splitEnum[0].trim(), splitEnum[2].trim()));
            }
        });

        // add implement class
        FullyQualifiedJavaType dbEnum = new FullyQualifiedJavaType("com.enterprise.data.type.DBEnum");
        enumClass.addImportedType(dbEnum);
        enumClass.addSuperInterface(dbEnum);

        // add private int value
        Field enumType = new Field("type", FullyQualifiedJavaType.getIntInstance());
        enumType.setVisibility(JavaVisibility.PRIVATE);
        enumClass.addField(enumType);

        // add private String name
        Field nameField = new Field("name", FullyQualifiedJavaType.getStringInstance());
        nameField.setVisibility(JavaVisibility.PRIVATE);
        enumClass.addField(nameField);

        // add constructor method
        String enumClassName = getEnumClassName(enumString, field).trim();
        if (enumClassName.indexOf(".") >= 0) {
            enumClassName = enumClassName.substring(enumClassName.lastIndexOf(".") + 1, enumClassName.length());
        }
        Method constructor = new Method(enumClassName);
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "type"));
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
        constructor.setConstructor(true);
        constructor.addBodyLine("this.type = type;");
        constructor.addBodyLine("this.name = name;");
        enumClass.addMethod(constructor);

        // add method getIntValue
        Method getIntValue = new Method("getTypeValue");
        getIntValue.addAnnotation("@Override");
        getIntValue.setVisibility(JavaVisibility.PUBLIC);
        getIntValue.setReturnType(FullyQualifiedJavaType.getIntInstance());
        getIntValue.addBodyLine("return type;");
        enumClass.addMethod(getIntValue);

        // add method getName
        Method getName = new Method("getName");
        getName.setVisibility(JavaVisibility.PUBLIC);
        getName.setReturnType(FullyQualifiedJavaType.getStringInstance());
        getName.addBodyLine("return name;");
        enumClass.addMethod(getName);

        answer.add(enumClass);
        return enumClass;
    }

    protected FullyQualifiedJavaType getTopEnumType(String enumString, Field field, TopLevelClass topLevelClass) {
        String enumClassName = getEnumClassName(enumString, field).trim();
        String packageName = topLevelClass.getType().getPackageName();

        String packageClassName;

        if (enumClassName.indexOf(".") > 0) {
            String[] enumClassNameArr = enumClassName.split("\\.");
            String[] packageNameArr = packageName.split("\\.");
            if (enumClassNameArr.length >= packageNameArr.length) {
                packageClassName = enumClassName;
            } else {
                List<String> packageClassList = new ArrayList<>();
                packageClassList.addAll(Arrays.asList(packageNameArr).subList(0, packageNameArr.length - enumClassNameArr.length + 1));
                packageClassList.addAll(Arrays.asList(enumClassNameArr));
                packageClassName = packageClassList.stream().collect(Collectors.joining("."));
            }
        } else {
            packageClassName = packageName + "." + enumClassName;
        }

        return new FullyQualifiedJavaType(packageClassName);
    }

    private String getEnumClassName(String enumString, Field field) {
        String name;
        if (enumString.indexOf(":") > 0) {
            name = enumString.split(":")[0];
        } else {
            name = field.getName();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;
    }

    private void addNumberValidate(TopLevelClass topLevelClass, Field field, IntrospectedColumn introspectedColumn) {
        String remark = introspectedColumn.getRemarks();
        Matcher matcher = Number_Range_Matcher.matcher(remark);

        if (matcher.find()) {
            Long min = Long.valueOf(matcher.group(1));
            Long max = Long.valueOf(matcher.group(2));

            topLevelClass.addImportedType("org.hibernate.validator.constraints.Range");
            field.addAnnotation(String.format("@Range(min = %dL, max = %dL)", min, max));

            if (min < Integer.MIN_VALUE || max > Integer.MAX_VALUE) {
                field.setType(new FullyQualifiedJavaType("long"));
            }
        }
    }

    private FullyQualifiedJavaType getSuperClass() {
        FullyQualifiedJavaType superClass;
        String rootClass = getRootClass();
        if (rootClass != null) {
            superClass = new FullyQualifiedJavaType(rootClass);
        } else {
            superClass = null;
        }

        return superClass;
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.setName(topLevelClass.getType().getShortName());
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        List<IntrospectedColumn> constructorColumns = introspectedTable.getAllColumns();

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty()));
        }

        StringBuilder sb = new StringBuilder();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super(");
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", ");
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
            }
            sb.append(");");
            method.addBodyLine(sb.toString());
        }

        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            sb.setLength(0);
            sb.append("this.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" = ");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        topLevelClass.addMethod(method);
    }

    private Field getJavaBeansField(IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(fqjt);
        field.setName(property);
        this.context.getCommentGenerator().addFieldComment(field, this.introspectedTable, introspectedColumn);
        return field;
    }
}
