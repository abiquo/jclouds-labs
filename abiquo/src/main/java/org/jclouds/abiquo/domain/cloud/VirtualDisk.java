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
package org.jclouds.abiquo.domain.cloud;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.AbiquoAsyncApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.rest.RestContext;

import com.abiquo.model.transport.SingleResourceTransportDto;

/**
 * Base class for the virtual disks attached to a {@link VirtualMachine}.
 * 
 * @author Ignasi Barrera
 * @param <T>
 *           The type of the target virtual disk.
 * 
 */
public abstract class VirtualDisk<T extends SingleResourceTransportDto> extends DomainWrapper<T> {

   protected VirtualDisk(RestContext<AbiquoApi, AbiquoAsyncApi> context, T target) {
      super(context, target);
   }

   /**
    * Gets the unique id of the virtual disk.
    */
   public abstract Integer getId();

   /**
    * Get the attachment order of the virtual disk when attached to a virtual
    * machine.
    */
   public abstract Integer getSequence();

   /**
    * Gets the size of the virtual disk in MB.
    */
   public abstract Long getSizeInMb();

}
