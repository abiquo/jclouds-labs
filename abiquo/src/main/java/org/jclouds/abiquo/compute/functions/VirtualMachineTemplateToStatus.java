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
package org.jclouds.abiquo.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.compute.domain.Image.Status;

import com.abiquo.model.enumerator.VMTemplateState;
import com.google.common.base.Function;
import com.google.inject.Singleton;

/**
 * Transforms a {@link VirtualMachineTemplate} into an {@link Status}.
 */
@Singleton
public class VirtualMachineTemplateToStatus implements Function<VirtualMachineTemplate, Status> {

   @Override
   public Status apply(final VirtualMachineTemplate virtualMachineTemplate) {

      VMTemplateState vmtemplateState = virtualMachineTemplate.getState();
      checkNotNull(vmtemplateState, "virtual machine template state");

      switch (vmtemplateState) {
         case DONE:
            return Status.AVAILABLE;
         case IN_PROGRESS:
            return Status.PENDING;
         case FAILED:
            return Status.ERROR;
         default:
            return Status.UNRECOGNIZED;
      }
   }
}
