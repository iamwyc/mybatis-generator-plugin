# mybatis-generator-plugin
## mybatis-generator使用（idea）
1. 创建一个maven项目
2. 在pom文件引入以下插件
```
  <build>
    <plugins>
      <plugin>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-maven-plugin</artifactId>
        <version>1.3.2</version>
        <configuration>
          <verbose>true</verbose>
          <overwrite>true</overwrite>
          <!--配置文件的路径-->
          <configurationFile>src/main/resources/generatorConfig.xml</configurationFile>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <version>1.3.2</version>
          </dependency>
          <!--插件使用的mysql驱动-->
          <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.45</version>
          </dependency>
          <!--引入自定义的插件包（自行打包）-->
          <dependency>
            <groupId>com.github.iamwyc</groupId>
            <artifactId>mybatis-generator-plugin</artifactId>
            <version>1.0.0</version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
  
```

3. generatorConfig.xml

```

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
  PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
  "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
  
<generatorConfiguration>
  <!--导入属性配置-->
  <!--<properties resource="db.properties"/>-->

  <!--指定特定数据库的jdbc驱动jar包的位置 可以通过pom配置依赖-->
  <!--<classPathEntry location="${jdbc.driverLocation}"/>-->

  <context id="default" targetRuntime="MyBatis3">
    <!-- 为生成的Java模型类添加序列化接口  -->
    <plugin type="org.mybatis.generator.plugins.SerializablePlugin" />
    <!-- 自定义插件  lombok插件  -->
    <plugin type="com.github.iamwyc.plugins.LombokPlugin" />

    <plugin type="com.github.iamwyc.plugins.ServicePlugin" >
      <property name="targetProject" value="src/main/java" />
      <property name="targetPackage" value="com.github.iamwyc.service" />
      <property name="implementationPackage" value="com.github.iamwyc.service.impl" />
    </plugin>
    <!-- optional，旨在创建class时，对注释进行控制。可以不定义type-->
    <commentGenerator  type="com.github.iamwyc.plugins.FiledCommentGenerator">
      <property name="suppressDate" value="true"/>
      <property name="suppressAllComments" value="true"/>
    </commentGenerator>

    <!--jdbc的数据库连接 -->
    <jdbcConnection
      connectionURL="jdbc:mysql://localhost:3306/test?characterEncoding=utf8"
      driverClass="com.mysql.jdbc.Driver"
      userId="root"
      password="a26842123">
    </jdbcConnection>


    <!-- 非必需，类型处理器，在数据库类型和java类型之间的转换控制-->
    <javaTypeResolver>
      <property name="forceBigDecimals" value="false"/>
    </javaTypeResolver>


    <!-- Model模型生成器,用来生成含有主键key的类，记录类 以及查询Example类
        targetPackage     指定生成的model生成所在的包名
        targetProject     指定在该项目下所在的路径
    -->
    <javaModelGenerator targetPackage="com.github.iamwyc.model.po"
      targetProject="src/main/java">

      <!-- 是否允许子包，即targetPackage.schemaName.tableName -->
      <property name="enableSubPackages" value="false"/>
      <!-- 是否对model添加 构造函数 -->
      <property name="constructorBased" value="false"/>
      <!-- 是否对类CHAR类型的列的数据进行trim操作 -->
      <property name="trimStrings" value="true"/>
      <!-- 建立的Model对象是否 不可改变  即生成的Model对象不会有 setter方法，只有构造方法 -->
      <property name="immutable" value="false"/>
    </javaModelGenerator>

    <!--Mapper映射文件生成所在的目录 为每一个数据库的表生成对应的SqlMap文件 -->
    <sqlMapGenerator targetPackage="mapper"
      targetProject="src/main/resources">
      <property name="enableSubPackages" value="true"/>
    </sqlMapGenerator>

    <!-- 客户端代码，生成易于使用的针对Model对象和XML配置文件 的代码
            type="ANNOTATEDMAPPER",生成Java Model 和基于注解的Mapper对象
            type="MIXEDMAPPER",生成基于注解的Java Model 和相应的Mapper对象
            type="XMLMAPPER",生成SQLMap XML文件和独立的Mapper接口
    -->
    <javaClientGenerator targetPackage="com.github.iamwyc.mapper"
      implementationPackage="com.github.iamwyc.mapper.provider"
      targetProject="src/main/java" type="ANNOTATEDMAPPER">
      <property name="enableSubPackages" value="true"/>
    </javaClientGenerator>


    <!-- 需要生成的表名以及实体类命名 -->
    <table tableName="user" domainObjectName="User"
      enableCountByExample="false" enableUpdateByExample="false"
      enableDeleteByExample="false" enableSelectByExample="false"
      selectByExampleQueryId="false">
    </table>
  </context>
</generatorConfiguration>
```
4. maven生成

## 注意
在配置文件内添加插件即可
```
    <!-- 自定义插件  lombok插件  -->
    <plugin type="com.github.iamwyc.plugins.LombokPlugin" />
    <!-- 自定义插件  生成service层插件  -->
    <plugin type="com.github.iamwyc.plugins.ServicePlugin" >
      <property name="targetProject" value="src/main/java" />
      <property name="targetPackage" value="com.github.iamwyc.service" />
      <property name="implementationPackage" value="com.github.iamwyc.service.impl" />
    </plugin>
```
