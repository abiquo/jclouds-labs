/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jclouds.abiquo.predicates.cloud;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.jclouds.abiquo.domain.cloud.TemplateDefinition;
import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;

import com.abiquo.model.enumerator.DiskFormatType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Container for {@link VirtualMachineTemplate} filters.
 * 
 * @author Francesc Montserrat
 */
public class VirtualMachineTemplatePredicates {
   public static Predicate<VirtualMachineTemplate> id(final Integer... ids) {
      checkNotNull(ids, "ids must be defined");

      return new Predicate<VirtualMachineTemplate>() {
         @Override
         public boolean apply(final VirtualMachineTemplate template) {
            return Arrays.asList(ids).contains(template.getId());
         }
      };
   }

   public static Predicate<VirtualMachineTemplate> name(final String... names) {
      checkNotNull(names, "names must be defined");

      return new Predicate<VirtualMachineTemplate>() {
         @Override
         public boolean apply(final VirtualMachineTemplate template) {
            return Arrays.asList(names).contains(template.getName());
         }
      };
   }

   public static Predicate<VirtualMachineTemplate> diskFormat(final DiskFormatType... formats) {
      checkNotNull(formats, "formats must be defined");

      return new Predicate<VirtualMachineTemplate>() {
         @Override
         public boolean apply(final VirtualMachineTemplate template) {
            return Arrays.asList(formats).contains(template.getDiskFormatType());
         }
      };
   }

   public static Predicate<VirtualMachineTemplate> isShared() {
      return new Predicate<VirtualMachineTemplate>() {
         @Override
         public boolean apply(final VirtualMachineTemplate input) {
            return input.unwrap().isShared();
         }
      };
   }

   public static Predicate<VirtualMachineTemplate> isInstance() {
      return new Predicate<VirtualMachineTemplate>() {
         @Override
         public boolean apply(final VirtualMachineTemplate input) {
            return input.unwrap().searchLink("master") != null;
         }
      };
   }

   public static Predicate<VirtualMachineTemplate> templateDefinition(final TemplateDefinition... definitions) {
      checkNotNull(definitions, "definitions must be defined");

      final Collection<String> urls = Collections2.transform(Arrays.asList(definitions),
            new Function<TemplateDefinition, String>() {
               @Override
               public String apply(TemplateDefinition def) {
                  return def.getUrl();
               }
            });

      return new Predicate<VirtualMachineTemplate>() {
         @Override
         public boolean apply(final VirtualMachineTemplate template) {
            return template.getUrl().isPresent() ? urls.contains(template.getUrl().get()) : false;
         }
      };
   }
}
