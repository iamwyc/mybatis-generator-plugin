package com.github.iamwyc.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.internal.DefaultCommentGenerator;

/**
 * @author : iamwyc
 * @version : 1.0
 * @date : 2019/8/3 19:19
 */
public class FiledCommentGenerator  extends DefaultCommentGenerator {

  private boolean suppressAllComments;
  public FiledCommentGenerator() {
    super();
    suppressAllComments = false;
  }


  public void addFieldComment(Field field, IntrospectedTable introspectedTable,
      IntrospectedColumn introspectedColumn) {
    if (suppressAllComments) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    field.addJavaDocLine("/**");
    sb.append(" * ");
    sb.append(introspectedColumn.getRemarks());
    field.addJavaDocLine(sb.toString().replace("\n", " "));
    field.addJavaDocLine(" */");
  }
}
