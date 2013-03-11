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

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.AbiquoAsyncApi;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.domain.task.VirtualMachineTask;
import org.jclouds.abiquo.reference.ValidationErrors;
import org.jclouds.abiquo.reference.annotations.EnterpriseEdition;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.rest.RestContext;

import com.abiquo.model.enumerator.VolumeState;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;

/**
 * Represents a block device attached to a virtual machine.
 * <p>
 * All data stored in the volume is persistent and will not be lost when the
 * virtual machine is destroyed.
 * 
 * @author Ignasi Barrera
 */
@EnterpriseEdition
public class Volume extends VirtualDisk<VolumeManagementDto> {
   /** The default state for volumes. */
   public static final VolumeState DEFAULT_STATE = VolumeState.DETACHED;

   /** The virtual datacenter where the volume belongs. */
   private VirtualDatacenter virtualDatacenter;

   /** The tier where the volume belongs. */
   private Tier tier;

   /**
    * Constructor to be used only by the builder.
    */
   protected Volume(final RestContext<AbiquoApi, AbiquoAsyncApi> context, final VolumeManagementDto target) {
      super(context, target);
   }

   // Domain operations

   /**
    * Deletes the volume, and the data it contains.
    */
   public void delete() {
      context.getApi().getCloudApi().deleteVolume(target);
      target = null;
   }

   /**
    * Creates the new (and unformatted) block device.
    */
   public void save() {
      target = context.getApi().getCloudApi().createVolume(virtualDatacenter.unwrap(), target);
   }

   /**
    * Updates the current volume.
    * <p>
    * The size of the volume can be increased, but after the resize finishes,
    * the file system may need to be manually expanded in order to see the new
    * size.
    * 
    * @return <code>null</code> if the volume is not attached to a running
    *         virtual machine, or an asynchronous task reference if the volume
    *         is attached to the virtual machine. In the latter case, a
    *         reconfigure operation on the virtual machine will be executed in
    *         order to let it get noticed of the new size of the volume.
    */
   public VirtualMachineTask update() {
      AcceptedRequestDto<String> taskRef = context.getApi().getCloudApi().updateVolume(target);
      return taskRef == null ? null : getTask(taskRef).asVirtualMachineTask();
   }

   // Parent access

   /**
    * Returns the virtual datacenter where the volume resides.
    */
   public VirtualDatacenter getVirtualDatacenter() {
      Integer virtualDatacenterId = target.getIdFromLink(ParentLinkName.VIRTUAL_DATACENTER);
      VirtualDatacenterDto dto = context.getApi().getCloudApi().getVirtualDatacenter(virtualDatacenterId);
      virtualDatacenter = wrap(context, VirtualDatacenter.class, dto);
      return virtualDatacenter;
   }

   /**
    * Returns the storage tier (service level) where the volume resides.
    */
   public Tier getTier() {
      Integer tierId = target.getIdFromLink(ParentLinkName.TIER);
      TierDto dto = context.getApi().getCloudApi().getStorageTier(virtualDatacenter.unwrap(), tierId);
      tier = wrap(context, Tier.class, dto);
      return tier;
   }

   // Actions

   /**
    * Move the volume to the given virtual datacenter.
    * 
    * @param newVirtualDatacenter
    *           The destination virtual datacenter.
    */
   public void moveTo(final VirtualDatacenter newVirtualDatacenter) {
      target = context.getApi().getCloudApi().moveVolume(unwrap(), newVirtualDatacenter.unwrap());
   }

   // Builder

   public static Builder builder(final RestContext<AbiquoApi, AbiquoAsyncApi> context,
         final VirtualDatacenter virtualDatacenter, final Tier tier) {
      return new Builder(context, virtualDatacenter, tier);
   }

   public static class Builder {
      private RestContext<AbiquoApi, AbiquoAsyncApi> context;

      private String name;

      private String description;

      private Long sizeInMb;

      private VirtualDatacenter virtualDatacenter;

      private Tier tier;

      public Builder(final RestContext<AbiquoApi, AbiquoAsyncApi> context, final VirtualDatacenter virtualDatacenter,
            final Tier tier) {
         super();
         checkNotNull(virtualDatacenter, ValidationErrors.NULL_RESOURCE + VirtualDatacenter.class);
         checkNotNull(tier, ValidationErrors.NULL_RESOURCE + Tier.class);
         this.context = context;
         this.virtualDatacenter = virtualDatacenter;
         this.tier = tier;
      }

      public Builder name(final String name) {
         this.name = name;
         return this;
      }

      public Builder description(final String description) {
         this.description = description;
         return this;
      }

      public Builder sizeInMb(final long sizeInMb) {
         this.sizeInMb = sizeInMb;
         return this;
      }

      public Volume build() {
         VolumeManagementDto dto = new VolumeManagementDto();
         dto.setName(name);
         dto.setDescription(description);
         dto.setSizeInMB(sizeInMb);
         dto.setState(DEFAULT_STATE.name());

         RESTLink link = tier.unwrap().searchLink("self");
         checkNotNull(link, ValidationErrors.MISSING_REQUIRED_LINK);
         dto.addLink(new RESTLink("tier", link.getHref()));

         Volume volume = new Volume(context, dto);
         volume.virtualDatacenter = virtualDatacenter;
         volume.tier = tier;

         return volume;
      }
   }

   // Delegate methods

   @Override
   public Integer getId() {
      return target.getId();
   }

   public String getState() {
      return target.getState();
   }

   public String getName() {
      return target.getName();
   }

   public void setName(final String name) {
      target.setName(name);
   }

   @Override
   public Long getSizeInMb() {
      return target.getSizeInMB();
   }

   public void setSizeInMb(final long sizeInMB) {
      target.setSizeInMB(sizeInMB);
   }

   public String getDescription() {
      return target.getDescription();
   }

   public void setDescription(final String description) {
      target.setDescription(description);
   }

   @Override
   public Integer getSequence() {
      return target.getSequence();
   }

   @Override
   public String toString() {
      return "Volume [id=" + getId() + ", state=" + getState() + ", name=" + getName() + ", sizeInMb=" + getSizeInMb()
            + ", description=" + getDescription() + "]";
   }

}
