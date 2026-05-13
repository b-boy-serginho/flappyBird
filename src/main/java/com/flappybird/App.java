package com.flappybird;

/**
 * Hello world!
 *
 */



public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }


    /* <?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.flappybird</groupId>
  <artifactId>flappybird</artifactId>
  <version>1.0-SNAPSHOT</version>

  <name>flappybird</name>
  <!-- FIXME change it to the project's website -->
  <url>http://www.example.com</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>

    <!--  Versión de LWJGL (Lightweight Java Game Library); usamos 3.3.3 en todas las dependencias -->
    <lwjgl.version>3.3.3</lwjgl.version>

    <!--
      Clase que contiene public static void main(String[] args).
      Al ejecutar "mvn exec:exec" se lanza esta clase.
      Puedes cambiar la clase desde la terminal sin tocar el POM:
        mvn exec:exec -DmainClass=com.flappybird.OtraClase
    -->
    <mainClass>com.flappybird.App</mainClass>
  </properties>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>

    <!--
      LWJGL = Lightweight Java Game Library.
      Son los "bindings" de Java para OpenGL y GLFW: permiten llamar a OpenGL y GLFW desde Java.
      Tres módulos principales (sin "natives"):
        - lwjgl        → Núcleo y carga de bibliotecas nativas.
        - lwjgl-glfw   → Crear ventanas, teclado, ratón (GLFW desde Java).
        - lwjgl-opengl → Llamadas a OpenGL (dibujar, shaders, buffers, etc.).
    -->
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl</artifactId>
      <version>${lwjgl.version}</version>
    </dependency>
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl-glfw</artifactId>
      <version>${lwjgl.version}</version>
    </dependency>
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl-opengl</artifactId>
      <version>${lwjgl.version}</version>
    </dependency>

    <!-- Dependencias nativas para Windows 64-bit -->
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl</artifactId>
      <version>${lwjgl.version}</version>
      <classifier>natives-windows</classifier>
    </dependency>
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl-glfw</artifactId>
      <version>${lwjgl.version}</version>
      <classifier>natives-windows</classifier>
    </dependency>
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl-opengl</artifactId>
      <version>${lwjgl.version}</version>
      <classifier>natives-windows</classifier>
    </dependency>

  </dependencies>

  <build>
    <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
      <plugins>
        <!-- clean lifecycle, see https://maven.apache.org/ref/current/maven-core/lifecycles.html#clean_Lifecycle -->
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.1.0</version>
        </plugin>
        <!-- default lifecycle, jar packaging: see https://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_jar_packaging -->
        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.8.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>2.22.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-install-plugin</artifactId>
          <version>2.5.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>2.8.2</version>
        </plugin>
        <!-- site lifecycle, see https://maven.apache.org/ref/current/maven-core/lifecycles.html#site_Lifecycle -->
        <plugin>
          <artifactId>maven-site-plugin</artifactId>
          <version>3.7.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-project-info-reports-plugin</artifactId>
          <version>3.0.0</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
 */
}
