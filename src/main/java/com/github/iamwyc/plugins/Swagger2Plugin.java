package com.github.iamwyc.plugins;

import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;


public class Swagger2Plugin extends PluginAdapter {

  public Swagger2Plugin() {
  }

  public boolean validate(List<String> list) {
    return true;
  }


  @Override
  public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    topLevelClass.addImportedType("");
    return true;
  }

  @Override
  public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
      ModelClassType modelClassType) {
    topLevelClass.getImportedTypes()
        .add(new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty"));
    field.addAnnotation("@ApiModelProperty(\"" + introspectedColumn.getRemarks() + "\")");
    switch (introspectedColumn.getJdbcType()) {
      case 1:
      case 12:
        topLevelClass.getImportedTypes()
            .add(new FullyQualifiedJavaType("org.hibernate.validator.constraints.Length"));
        field.addAnnotation("@Length(max = " + introspectedColumn.getLength() + ")");
        break;
      default:
        break;
    }

    return true;
  }
}
