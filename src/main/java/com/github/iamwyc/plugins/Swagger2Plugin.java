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
      IntrospectedColumn column, IntrospectedTable introspectedTable,
      ModelClassType modelClassType) {
    topLevelClass.getImportedTypes()
        .add(new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty"));
    field.addAnnotation("@ApiModelProperty(\"" + column.getRemarks() + "\")");
    int jdbcType = column.getJdbcType();
    int length = column.getLength();
    switch (jdbcType) {
      case -6:
      case -7:
        //tinyint
        tinyint(topLevelClass, field, length);
        break;
      case 1:
        //char
      case 12:
        //varchar
        length(topLevelClass, field, length);
        break;
      case 91:
        //date
        datetime(topLevelClass, field, "YYYY-MM-dd");
        break;
      case 93:
        //datetime
        datetime(topLevelClass, field, "YYYY-MM-dd hh:mm:ss");
      default:
        break;
    }
    return true;
  }

  private void tinyint(TopLevelClass topLevelClass, Field field, int length) {
    int max = (int) Math.pow(10, length);
    int min = max == 1 ? 0 : -128;
    max = max > 127 ? 127 : max;
    range(topLevelClass, field, min, max);
  }

  private void range(TopLevelClass topLevelClass, Field field, int min, int max) {
    topLevelClass.getImportedTypes()
        .add(new FullyQualifiedJavaType("org.hibernate.validator.constraints.Range"
        ));
    field.addAnnotation("@Range(min = " + min + ", max =" + max + ")");
  }

  private void datetime(TopLevelClass topLevelClass, Field field, String pattern) {
    topLevelClass.getImportedTypes()
        .add(new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonFormat"
        ));
    field.addAnnotation("@JsonFormat(pattern = \"" + pattern + "\")");
  }

  private void length(TopLevelClass topLevelClass, Field field, int length) {
    topLevelClass.getImportedTypes()
        .add(new FullyQualifiedJavaType("org.hibernate.validator.constraints.Length"));
    field.addAnnotation("@Length(max = " + length + ")");
  }
}
