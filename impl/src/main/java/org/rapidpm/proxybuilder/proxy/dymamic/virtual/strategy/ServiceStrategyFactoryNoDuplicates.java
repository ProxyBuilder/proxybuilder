/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.rapidpm.proxybuilder.proxy.dymamic.virtual.strategy;


import org.rapidpm.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceStrategyFactoryNoDuplicates<T> implements ServiceStrategyFactory<T> {

  private final Lock initializationLock = new ReentrantLock();
  private volatile T realSubject;

  @Override
  public T realSubject(ServiceFactory<T> factory) {
    T result = realSubject;
    if (result == null) {
      initializationLock.lock();
      try {
        result = realSubject;
        if (result == null) {
          result = realSubject = factory.createInstance();
        }
      } finally {
        initializationLock.unlock();
      }
    }
    return result;
  }
}
