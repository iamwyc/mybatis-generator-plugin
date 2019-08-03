package com.github.iamwyc.plugins;

import java.util.ArrayList;
import java.util.List;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 生成service的插件类
 */
public class ServicePlugin extends PluginAdapter {

  private FullyQualifiedJavaType serviceType;
  private FullyQualifiedJavaType daoType;
  private FullyQualifiedJavaType interfaceType;
  private FullyQualifiedJavaType pojoType;
  private FullyQualifiedJavaType autowired;
  private FullyQualifiedJavaType service;
  private FullyQualifiedJavaType returnType;
  private String servicePack;
  private String serviceImplPack;
  private String project;
  private String pojoUrl;
  /**
   * 支持以下语句
   */
  private boolean enableInsertSelective = true;
  private boolean enableDeleteByPrimaryKey = true;

  public ServicePlugin() {
    super();
  }

  @Override
  public boolean validate(List<String> warnings) {

    String enableInsert = properties.getProperty("enableInsert");

    String enableInsertSelective = properties.getProperty("enableInsertSelective");

    String enableDeleteByPrimaryKey = properties.getProperty("enableDeleteByPrimaryKey");

    if (StringUtility.stringHasValue(enableInsertSelective)) {
      this.enableInsertSelective = StringUtility.isTrue(enableInsertSelective);
    }

    if (StringUtility.stringHasValue(enableDeleteByPrimaryKey)) {
      this.enableDeleteByPrimaryKey = StringUtility.isTrue(enableDeleteByPrimaryKey);
    }

    servicePack = properties.getProperty("targetPackage");
    serviceImplPack = properties.getProperty("implementationPackage");
    project = properties.getProperty("targetProject");

    pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();

    autowired = new FullyQualifiedJavaType(
        "org.springframework.beans.factory.annotation.Autowired");
    service = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
    return true;
  }

  @Override
  public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(
      IntrospectedTable introspectedTable) {
    List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
    String table = introspectedTable.getBaseRecordType();
    String tableName = table.replaceAll(this.pojoUrl + ".", "");
    interfaceType = new FullyQualifiedJavaType(servicePack + "." + tableName + "Service");

    // mybatis
    daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());

    // logger.info(toLowerCase(daoType.getShortName()));
    serviceType = new FullyQualifiedJavaType(serviceImplPack + "." + tableName + "ServiceImpl");

    pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);
    Interface interface1 = new Interface(interfaceType);
    TopLevelClass topLevelClass = new TopLevelClass(serviceType);
    // 导入必要的类
    addImport(interface1, topLevelClass);

    // 接口
    addService(topLevelClass, interface1, introspectedTable, tableName, files);
    // 实现类
    addServiceImpl(topLevelClass, introspectedTable, tableName, files);

    return files;
  }

  /**
   * 添加接口
   */
  protected void addService(TopLevelClass topLevelClass, Interface interface1,
      IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {

    interface1.setVisibility(JavaVisibility.PUBLIC);

    //添加缺省方法

    Method method = modifyEntity(topLevelClass, introspectedTable, tableName);
    clearMethodBody(method);
    interface1.addMethod(method);

    method = selectByPrimaryKey(introspectedTable, tableName);
    clearMethodBody(method);
    interface1.addMethod(method);

    if (enableDeleteByPrimaryKey) {
      method = getOtherInteger("removeByPrimaryKey", "deleteByPrimaryKey", introspectedTable,
          tableName);
      clearMethodBody(method);
      interface1.addMethod(method);
    }

    if (enableInsertSelective) {
      method = getOtherInsertboolean("create" + pojoType.getShortName(), "insertSelective",
          introspectedTable,
          tableName);
      clearMethodBody(method);
      interface1.addMethod(method);
    }

    GeneratedJavaFile file = new GeneratedJavaFile(interface1, project,
        context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
        context.getJavaFormatter());
    files.add(file);
  }

  private void clearMethodBody(Method method) {
    if (method != null) {
      List<String> bodyLines = method.getBodyLines();
      bodyLines.clear();
    }
  }


  private Method modifyEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
      String tableName) {
    String field = pojoType.getShortName().toLowerCase();
    Method method = new Method();
    method.setName("update" + pojoType.getShortName());
    method.setReturnType(pojoType);
    method.addParameter(new Parameter(pojoType, field));
    method.setVisibility(JavaVisibility.PUBLIC);
    //更新时间
    List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
    for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
      if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeColumn()) {
        if ("updateTime".equals(introspectedColumn.getJavaProperty())) {
          topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
          method.addBodyLine("record.setUpdateTime(new Date());");
        }
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append("if(this.");
    sb.append(getDaoShort());
    if (introspectedTable.hasBLOBColumns()) {
      sb.append("updateByPrimaryKeySelectiveWithoutBLOBs");
    } else {
      sb.append("updateByPrimaryKeySelective");
    }
    sb.append("(").append(field).append(")==1)");
    method.addBodyLine(sb.toString());
    method.addBodyLine("\treturn " + field + ";");
    method.addBodyLine("return null;");
    return method;
  }


  /**
   * 添加实现类
   */
  protected void addServiceImpl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
      String tableName, List<GeneratedJavaFile> files) {
    topLevelClass.setVisibility(JavaVisibility.PUBLIC);
    // 设置实现的接口
    topLevelClass.addSuperInterface(interfaceType);

    //添加slf4j
    topLevelClass.addImportedType("lombok.extern.slf4j.Slf4j");
    topLevelClass.addAnnotation("@Slf4j");

    topLevelClass.addAnnotation(
        "@Service(\"" + tableName.substring(0, 1).toLowerCase() + tableName.substring(1)
            + "Service\")");
    topLevelClass.addImportedType(service);

    // 添加引用dao
    addField(topLevelClass, tableName);
    // 添加方法
    topLevelClass.addMethod(modifyEntity(topLevelClass, introspectedTable, tableName));
    topLevelClass.addMethod(selectByPrimaryKey(introspectedTable, tableName));

    if (enableDeleteByPrimaryKey) {
      topLevelClass.addMethod(
          getOtherInteger("removeByPrimaryKey", "deleteByPrimaryKey", introspectedTable,
              tableName));
    }
    if (enableInsertSelective) {
      topLevelClass.addMethod(
          getOtherInsertboolean("create" + pojoType.getShortName(), "insertSelective",
              introspectedTable,
              tableName));
    }
    // 生成文件
    GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project,
        context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
        context.getJavaFormatter());
    files.add(file);
  }

  /**
   * 添加字段
   */
  protected void addField(TopLevelClass topLevelClass, String tableName) {
    // 添加 dao
    Field field = new Field();
    field.setName(toLowerCase(daoType.getShortName())); // 设置变量名
    topLevelClass.addImportedType(daoType);
    field.setType(daoType); // 类型
    field.setVisibility(JavaVisibility.PRIVATE);
    field.addAnnotation("@Autowired");
    topLevelClass.addField(field);
  }

  /**
   * 添加方法
   */
  protected Method selectByPrimaryKey(IntrospectedTable introspectedTable, String tableName) {
    Method method = new Method();
    method.setName("selectByPrimaryKey");
    method.setReturnType(pojoType);
    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
      FullyQualifiedJavaType type = new FullyQualifiedJavaType(
          introspectedTable.getPrimaryKeyType());
      method.addParameter(new Parameter(type, "key"));
    } else {
      for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
        method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
      }
    }
    method.setVisibility(JavaVisibility.PUBLIC);
    StringBuilder sb = new StringBuilder();
    // method.addBodyLine("try {");
    sb.append("return this.");
    sb.append(getDaoShort());
    sb.append("selectByPrimaryKey");
    sb.append("(");
    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
      sb.append(introspectedColumn.getJavaProperty());
      sb.append(",");
    }
    sb.setLength(sb.length() - 1);
    sb.append(");");
    method.addBodyLine(sb.toString());
    return method;
  }

  /**
   * 添加方法
   */
  protected Method getOtherInteger(String methodName, String daoName,
      IntrospectedTable introspectedTable, String tableName) {
    Method method = new Method();
    method.setName(methodName);
    method.setReturnType(FullyQualifiedJavaType.getIntInstance());
    String params = addParams(introspectedTable, method);
    method.setVisibility(JavaVisibility.PUBLIC);
    StringBuilder sb = new StringBuilder();
    sb.append("return this.");
    sb.append(getDaoShort());
    if (introspectedTable.hasBLOBColumns() && !"removeByPrimaryKey".equals(methodName)) {
      sb.append(daoName + "WithoutBLOBs");
    } else {
      sb.append(daoName);
    }
    sb.append("(");
    sb.append(params);
    sb.append(");");
    method.addBodyLine(sb.toString());
    return method;
  }

  /**
   * 添加方法
   */
  protected Method getOtherInsertboolean(String methodName, String daoName,
      IntrospectedTable introspectedTable, String tableName) {
    String field = pojoType.getShortName().toLowerCase();
    Method method = new Method();
    method.setName(methodName);
    method.setReturnType(returnType);
    method.addParameter(new Parameter(pojoType, field));
    method.setVisibility(JavaVisibility.PUBLIC);
    StringBuilder sb = new StringBuilder();
    if (returnType == null) {
      sb.append("this.");
    } else {
      sb.append("return this.");
    }
    sb.append(getDaoShort());
    sb.append(daoName);
    sb.append("(");
    sb.append(field);
    sb.append(");");
    method.addBodyLine(sb.toString());
    return method;
  }

  protected String addParams(IntrospectedTable introspectedTable, Method method) {
    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
      FullyQualifiedJavaType type = new FullyQualifiedJavaType(
          introspectedTable.getPrimaryKeyType());
      method.addParameter(new Parameter(type, "key"));
    } else {
      for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
        method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
      }
    }
    StringBuffer sb = new StringBuffer();
    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
      sb.append(introspectedColumn.getJavaProperty());
      sb.append(",");
    }
    sb.setLength(sb.length() - 1);
    return sb.toString();
  }


  protected String toLowerCase(String tableName) {
    StringBuilder sb = new StringBuilder(tableName);
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    return sb.toString();
  }

  /**
   * 导入需要的类
   */
  private void addImport(Interface interfaces, TopLevelClass topLevelClass) {
    interfaces.addImportedType(pojoType);
    topLevelClass.addImportedType(daoType);
    topLevelClass.addImportedType(interfaceType);
    topLevelClass.addImportedType(pojoType);
    topLevelClass.addImportedType(service);
    topLevelClass.addImportedType(autowired);
  }


  private String getDaoShort() {
    return toLowerCase(daoType.getShortName()) + ".";
  }

  public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    returnType = method.getReturnType();
    return true;
  }
}
