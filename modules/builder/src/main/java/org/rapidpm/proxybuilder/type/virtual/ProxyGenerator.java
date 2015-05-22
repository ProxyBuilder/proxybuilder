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

package org.rapidpm.proxybuilder.type.virtual;


import org.rapidpm.proxybuilder.generator.Generator;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Sven Ruppert on 14.01.14.
 */
public class ProxyGenerator {


  private ProxyGenerator() {
  }

  private static final WeakHashMap CACHE = new WeakHashMap();

  public static <T> T make(Class<T> subject, Class<? extends T> realClass, Concurrency concurrency, ProxyType type) {
    final T make = make(subject.getClassLoader(), subject, realClass, concurrency, type);
    //hier ref auf realSubject einfuegen ??
    return make;
  }

  public static <T> T make(Class<T> subject, Class<? extends T> realClass, Concurrency concurrency) {
    return make(subject, realClass, concurrency, ProxyType.STATIC);
  }

  public static <T> T make(ClassLoader loader,
                           Class<T> subject,
                           Class<? extends T> realClass,
                           Concurrency concurrency,
                           ProxyType type) {
    Object proxy = null;
    if (type == ProxyType.STATIC) {
      proxy = createStaticProxy(loader, subject, realClass, concurrency);
    } else if (type == ProxyType.DYNAMIC) {
      proxy = createDynamicProxy(loader, subject, realClass, concurrency);
    } else if (type == ProxyType.OnExistingObject) {
      //Hier den OnExistingObject Proxy erzeugen!
      proxy = createStaticProxy(loader, subject, realClass, Concurrency.OnExistingObject);
//            proxy.setS
    }
    return subject.cast(proxy);
  }

  private static Object createStaticProxy(ClassLoader loader, Class subject, Class realClass, Concurrency concurrency) {
    Map clcache;
    synchronized (CACHE) {
      clcache = (Map) CACHE.get(loader);
      if (clcache == null) {
        CACHE.put(loader, clcache = new HashMap());
      }
    }
    try {
      Class clazz;
      CacheKey key = new CacheKey(subject, concurrency);
      synchronized (clcache) {
        clazz = (Class) clcache.get(key);
        if (clazz == null) {
          VirtualProxySourceGenerator vpsg = create(subject, realClass, concurrency);
          clazz = Generator.make(loader, vpsg.getProxyName(), vpsg.getCharSequence());
          clcache.put(key, clazz);
        }
      }
      return clazz.newInstance(); //proxy erzeugt
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static VirtualProxySourceGenerator create(Class subject, Class realClass, Concurrency concurrency) {
    switch (concurrency) {
      case NONE:
        return new VirtualProxySourceGeneratorNotThreadsafe(subject, realClass);
      case SOME_DUPLICATES:
        return new VirtualProxySourceGeneratorSomeDuplicates(subject, realClass);
      case NO_DUPLICATES:
        return new VirtualProxySourceGeneratorNoDuplicates(subject, realClass);
      case OnExistingObject:
        return new VirtualProxySourceGeneratorOnExistingObject(subject, realClass);
      default:
        throw new IllegalArgumentException(
            "Unsupported Concurrency: " + concurrency);
    }
  }

  private static Object createDynamicProxy(ClassLoader loader, Class subject, Class realClass, Concurrency concurrency) {
    if (concurrency != Concurrency.NONE) {
      throw new IllegalArgumentException("Unsupported Concurrency: " + concurrency);
    }
    return Proxy.newProxyInstance(
        loader,
        new Class<?>[]{subject},
        new VirtualDynamicProxyNotThreadSafe(realClass));
  }

  private static class CacheKey {
    private final Class subject;
    private final Concurrency concurrency;

    private CacheKey(Class subject, Concurrency concurrency) {
      this.subject = subject;
      this.concurrency = concurrency;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CacheKey that = (CacheKey) o;
      if (concurrency != that.concurrency) return false;
      return subject.equals(that.subject);
    }

    public int hashCode() {
      return 31 * subject.hashCode() + concurrency.hashCode();
    }
  }
}
