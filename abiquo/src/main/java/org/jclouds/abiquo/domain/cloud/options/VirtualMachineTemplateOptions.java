/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.abiquo.domain.cloud.options;

import org.jclouds.abiquo.domain.config.Category;
import org.jclouds.abiquo.domain.options.search.FilterOptions.BaseFilterOptionsBuilder;
import org.jclouds.http.options.BaseHttpRequestOptions;

import com.abiquo.model.enumerator.OSType;
import com.abiquo.model.enumerator.StatefulInclusion;

/**
 * Available options to query virtual machine templates.
 * 
 * @author Ignasi Barrera
 */
public class VirtualMachineTemplateOptions extends BaseHttpRequestOptions {
   public static Builder builder() {
      return new Builder();
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      VirtualMachineTemplateOptions options = new VirtualMachineTemplateOptions();
      options.queryParameters.putAll(queryParameters);
      return options;
   }

   public static class Builder extends BaseFilterOptionsBuilder<Builder> {
      private StatefulInclusion persistent;

      private String hypervisorType;

      private Category category;

      private String categoryName;

      private Integer idTemplate;

      private OSType osType;

      private Boolean is64bits;

      public Builder persistent(final StatefulInclusion persistent) {
         this.persistent = persistent;
         return this;
      }

      public Builder hypervisorType(final String hypervisorType) {
         this.hypervisorType = hypervisorType;
         return this;
      }

      public Builder category(final Category category) {
         this.category = category;
         return this;
      }

      public Builder categoryName(final String categoryName) {
         this.categoryName = categoryName;
         return this;
      }

      public Builder idTemplate(final Integer idTemplate) {
         this.idTemplate = idTemplate;
         return this;
      }

      public Builder osType(final OSType osType) {
         this.osType = osType;
         return this;
      }

      public Builder is64bits(final Boolean is64bits) {
         this.is64bits = is64bits;
         return this;
      }

      public VirtualMachineTemplateOptions build() {
         VirtualMachineTemplateOptions options = new VirtualMachineTemplateOptions();

         if (persistent != null) {
            options.queryParameters.put("stateful", persistent.name());
         }
         if (hypervisorType != null) {
            options.queryParameters.put("hypervisorTypeName", hypervisorType);
         }
         if (category != null) {
            options.queryParameters.put("categoryName", category.getName());
         }

         if (category == null && categoryName != null) {
            options.queryParameters.put("categoryName", categoryName);
         }

         if (idTemplate != null) {
            options.queryParameters.put("idTemplate", String.valueOf(idTemplate));
         }
         if (osType != null) {
            options.queryParameters.put("ostype", osType.name());
         }

         if (is64bits != null) {
            options.queryParameters.put("64bits", is64bits.toString());
         }

         return addFilterOptions(options);
      }
   }
}
