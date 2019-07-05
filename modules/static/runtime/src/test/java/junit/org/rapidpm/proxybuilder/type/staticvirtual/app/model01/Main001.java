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
package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model01;


import org.rapidpm.proxybuilder.type.staticruntime.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.staticruntime.virtual.StaticProxyGenerator;

public class Main001 {

  public static void main(String[] args) {

    final Service proxy01 = StaticProxyGenerator.make(Service.class, ServiceImpl.class, CreationStrategy.NO_DUPLICATES);
    final String result01 = proxy01.doWork("proxy01");

    final Service proxy02 = StaticProxyGenerator.make(Service.class, ServiceImpl.class, CreationStrategy.SOME_DUPLICATES);
    final String result02 = proxy01.doWork("proxy02");

  }
}
