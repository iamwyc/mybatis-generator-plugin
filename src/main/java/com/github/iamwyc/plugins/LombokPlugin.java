package com.github.iamwyc.plugins;

import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;


public class LombokPlugin extends PluginAdapter {

  public LombokPlugin() {
  }

  public boolean validate(List<String> list) {
    return true;
  }


  /**
   * model类添加lombok注解
   */
  public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    topLevelClass.addImportedType("lombok.AllArgsConstructor");
    topLevelClass.addImportedType("lombok.Builder");
    topLevelClass.addImportedType("lombok.Data");
    topLevelClass.addImportedType("lombok.NoArgsConstructor");
    topLevelClass.addAnnotation("@AllArgsConstructor");
    topLevelClass.addAnnotation("@Builder");
    topLevelClass.addAnnotation("@Data");
    topLevelClass.addAnnotation("@NoArgsConstructor");
    return true;
  }

  /**
   * model不生成getter方法
   */
  @Override
  public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
      ModelClassType modelClassType) {
    return false;
  }

  /**
   * model不生成setter方法
   */
  @Override
  public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
      ModelClassType modelClassType) {
    return false;
  }

  /**
   * mapper添加@Repository注解
   */
  @Override
  public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    FullyQualifiedJavaType type = new FullyQualifiedJavaType(
        "org.springframework.stereotype.Repository");
    interfaze.addImportedType(type);
    interfaze.addAnnotation("@Repository");
    return true;
  }
}
