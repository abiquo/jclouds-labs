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
package org.jclouds.abiquo.domain.infrastructure;

import java.util.List;
import java.util.Map;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.rest.ApiContext;

import com.abiquo.server.core.cloud.HypervisorTypeDto;
import com.google.common.base.Optional;

/**
 * Provides information about the target hypervisor and the supported
 * operations.
 * 
 * @author Ignasi Barrera
 */
public class HypervisorType extends DomainWrapper<HypervisorTypeDto> {

   /**
    * Constructor to be used only by the builder.
    */
   protected HypervisorType(final ApiContext<AbiquoApi> context, final HypervisorTypeDto target) {
      super(context, target);
   }

   public boolean supportsExtraHardDisks() {
      Optional<String> constraint = Optional.fromNullable(getConstraints().get("extra_hard_disk"));
      return Boolean.parseBoolean(constraint.or("true"));
   }

   public boolean hasEditableDatastores() {
      Optional<String> constraint = Optional.fromNullable(getConstraints().get("datastore_directory_editable"));
      return Boolean.parseBoolean(constraint.or("true"));
   }

   // Delegate methods

   public List<String> getCompatibilityTable() {
      return target.getCompatibilityTable();
   }

   public String getName() {
      return target.getName();
   }

   public String getRealName() {
      return target.getRealName();
   }

   public Map<String, String> getConstraints() {
      return target.getConstraints();
   }

   @Override
   public String toString() {
      return "HypervisorType [name=" + getName() + ", realName=" + getRealName() + "]";
   }

}
