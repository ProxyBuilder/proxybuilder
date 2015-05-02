/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.rapidpm.module.se.commons.proxy.generator;

import javax.tools.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Created by Sven Ruppert on 06.01.14.
 */
public class Generator {


  private static final Method defineClassMethod;
  private static final JavaCompiler jc;

  static {
    try {
      defineClassMethod = Proxy.class.getDeclaredMethod( "defineClass0", ClassLoader.class,String.class, byte[].class, int.class, int.class);
      defineClassMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
    jc = ToolProvider.getSystemJavaCompiler();
    if (jc == null) {
      throw new UnsupportedOperationException(
          "Cannot find java compiler! " +
              "Probably only JRE installed.");
    }
  }

  public static Class make(ClassLoader loader, String className, CharSequence javaSource) {
    GeneratedClassFile gcf = new GeneratedClassFile();
    DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
    boolean result = compile(className, javaSource, gcf, dc);
    return processResults(loader, javaSource, gcf, dc, result);
  }

  private static boolean compile( String className, CharSequence javaSource, GeneratedClassFile gcf, DiagnosticCollector<JavaFileObject> dc) {
    GeneratedJavaSourceFile gjsf = new GeneratedJavaSourceFile(className, javaSource);
    GeneratingJavaFileManager fileManager = new GeneratingJavaFileManager( jc.getStandardFileManager(dc, null, null), gcf);
    JavaCompiler.CompilationTask task = jc.getTask( null, fileManager, dc, null, null, Arrays.asList(gjsf));
    return task.call();
  }

  private static Class processResults(
      ClassLoader loader, CharSequence javaSource,
      GeneratedClassFile gcf, DiagnosticCollector<?> dc,
      boolean result) {
    if (result) {
      return createClass(loader, gcf);
    } else {
// use your logging system of choice here
      System.err.println("Compile failed:");
      System.err.println(javaSource);
      for (Diagnostic<?> d : dc.getDiagnostics()) {
        System.err.println(d);
      }
      throw new IllegalArgumentException(
          "Could not create proxy - compile failed");
    }

  }

  private static Class createClass(ClassLoader loader, GeneratedClassFile gcf) {
    try {
      byte[] data = gcf.getClassAsBytes();
      return (Class) defineClassMethod.invoke(null, loader, null, data, 0, data.length);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Proxy problem", e);
    }
  }
}
