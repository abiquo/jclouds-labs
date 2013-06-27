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

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.abiquo.domain.config.Privilege;
import org.jclouds.abiquo.domain.infrastructure.options.StoragePoolOptions;
import org.jclouds.abiquo.reference.ValidationErrors;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseXMLWithJAXB;
import org.jclouds.rest.ApiContext;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.infrastructure.storage.StorageDeviceDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.google.inject.TypeLiteral;

/**
 * Adds high level functionality to {@link StoragePoolDto}. The Storage Pool
 * Resource allows you to perform any administrative task for remote pools.
 * 
 * @author Ignasi Barrera
 * @author Francesc Montserrat
 * @see API: <a
 *      href="http://community.abiquo.com/display/ABI20/StoragePoolResource">
 *      http://community.abiquo.com/display/ABI20/StoragePoolResource</a>
 */
public class StoragePool extends DomainWrapper<StoragePoolDto> {

   /** The datacenter where the storage device is. */
   // Package protected to allow the storage device to be set automatically when
   // discovering the
   // pools in a device.
   StorageDevice storageDevice;

   /**
    * Constructor to be used only by the builder.
    */
   protected StoragePool(final ApiContext<AbiquoApi> context, final StoragePoolDto target) {
      super(context, target);
   }

   // Domain operations

   /**
    * Delete the storage pool.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StoragePoolResource#StoragePoolResource-Deleteastoragepool"
    *      > http://community.abiquo.com/display/ABI20/StoragePoolResource#
    *      StoragePoolResource- Deleteastoragepool</a>
    */
   public void delete() {
      context.getApi().getInfrastructureApi().deleteStoragePool(target);
      target = null;
   }

   /**
    * Create a storage pool. Create a storage pool means registering an existing
    * storage pool obtained from {@link StorageDevice#listRemoteStoragePools}
    * method and saving it. The Storage Pools must be associated with a Tier
    * using {@link #setTier}.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StoragePoolResource#StoragePoolResource-Createastoragepoolwithatierlink"
    *      > http://community.abiquo.com/display/ABI20/StoragePoolResource#
    *      StoragePoolResource- Createastoragepoolwithatierlink</a>
    */
   public void save() {
      target = context.getApi().getInfrastructureApi().createStoragePool(storageDevice.unwrap(), target);
   }

   /**
    * Update pool information in the server with the data from this pool.
    * Storage pool parameters cannot be updated by a user, so the parameters are
    * only a representation of the remote pool. Although the whole storage pool
    * entity is sent to the API in the update call, the only thing a user can
    * change is the tier that the pool belongs to by calling {@link #setTier}.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Storage+Pool+Resource#StoragePoolResource-UpdateaStoragePool"
    *      > http://community.abiquo.com/display/ABI20/Storage+Pool+Resource#
    *      StoragePoolResource- UpdateaStoragePool</a>
    */
   public void update() {
      target = context.getApi().getInfrastructureApi().updateStoragePool(target);
   }

   @Override
   public void refresh() {
      target = context.getApi().getInfrastructureApi()
            .refreshStoragePool(target, StoragePoolOptions.builder().sync(true).build());
   }

   /**
    * Define the tier in which the pool will be added.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StoragePoolResource#StoragePoolResource-Createastoragepoolwithatierlink"
    *      > http://community.abiquo.com/display/ABI20/StoragePoolResource#
    *      StoragePoolResource- Createastoragepoolwithatierlink</a>
    */
   public void setTier(final Tier tier) {
      checkNotNull(tier, ValidationErrors.NULL_RESOURCE + Privilege.class);
      checkNotNull(tier.getId(), ValidationErrors.MISSING_REQUIRED_FIELD + " id in " + Tier.class);

      this.updateLink(target, ParentLinkName.TIER, tier.unwrap(), "edit");
   }

   // Parent access

   /**
    * Get the device where the pool belongs.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StorageDeviceResource#StorageDeviceResource-Retrieveastoragedevice"
    *      > http://community.abiquo.com/display/ABI20/StorageDeviceResource#
    *      StorageDeviceResource- Retrieveastoragedevice</a>
    */
   public StorageDevice getStorageDevice() {
      RESTLink link = checkNotNull(target.searchLink(ParentLinkName.STORAGE_DEVICE),
            ValidationErrors.MISSING_REQUIRED_LINK + " " + ParentLinkName.STORAGE_DEVICE);

      HttpResponse response = context.getApi().get(link);

      ParseXMLWithJAXB<StorageDeviceDto> parser = new ParseXMLWithJAXB<StorageDeviceDto>(context.utils().xml(),
            TypeLiteral.get(StorageDeviceDto.class));

      return wrap(context, StorageDevice.class, parser.apply(response));
   }

   // Children access

   /**
    * Get the tier assigned to the pool. The storage pool needs to be persisted
    * in Abiquo first.
    * 
    * @return The tier assigned to this storage pool.
    */
   public Tier getTier() {
      RESTLink link = checkNotNull(target.searchLink(ParentLinkName.TIER), ValidationErrors.MISSING_REQUIRED_LINK + " "
            + ParentLinkName.TIER);

      HttpResponse response = context.getApi().get(link);

      ParseXMLWithJAXB<TierDto> parser = new ParseXMLWithJAXB<TierDto>(context.utils().xml(),
            TypeLiteral.get(TierDto.class));

      return wrap(context, Tier.class, parser.apply(response));
   }

   // Builder

   public static Builder builder(final ApiContext<AbiquoApi> context, final StorageDevice storageDevice) {
      return new Builder(context, storageDevice);
   }

   public static class Builder {
      private ApiContext<AbiquoApi> context;
      private StorageDevice storageDevice;
      private String name;
      private String type;
      private Long totalSizeInMb;

      public Builder(final ApiContext<AbiquoApi> context, final StorageDevice storageDevice) {
         super();
         checkNotNull(storageDevice, ValidationErrors.NULL_RESOURCE + StorageDevice.class);
         this.storageDevice = storageDevice;
         this.context = context;
      }

      public Builder storageDevice(final StorageDevice storageDevice) {
         checkNotNull(storageDevice, ValidationErrors.NULL_RESOURCE + StorageDevice.class);
         this.storageDevice = storageDevice;
         return this;
      }

      public Builder name(final String name) {
         this.name = name;
         return this;
      }

      public Builder type(final String type) {
         this.type = type;
         return this;
      }

      public Builder totalSizeInMb(final long totalSizeInMb) {
         this.totalSizeInMb = totalSizeInMb;
         return this;
      }

      public StoragePool build() {
         StoragePoolDto dto = new StoragePoolDto();
         dto.setName(name);
         dto.setType(type);
         dto.setTotalSizeInMb(totalSizeInMb);
         StoragePool storagePool = new StoragePool(context, dto);
         storagePool.storageDevice = storageDevice;
         return storagePool;
      }

      public static Builder fromStoragePool(final StoragePool in) {
         return StoragePool.builder(in.context, in.getStorageDevice()).name(in.getName()).type(in.getType())
               .totalSizeInMb(in.getTotalSizeInMb());
      }
   }

   // Delegate methods

   public String getName() {
      return target.getName();
   }

   public String getType() {
      return target.getType();
   }

   public long getTotalSizeInMb() {
      return target.getTotalSizeInMb();
   }

   public void setName(final String name) {
      target.setName(name);
   }

   public void setType(final String type) {
      target.setType(type);
   }

   public void setTotalSizeInMb(final long totalSizeInMb) {
      target.setTotalSizeInMb(totalSizeInMb);
   }

   // Readonly property
   public String getUUID() {
      return target.getIdStorage();
   }

   @Override
   public String toString() {
      return "StoragePool [name=" + getName() + ", totalSizeInMb=" + getTotalSizeInMb() + ", uuid=" + getUUID() + "]";
   }

}
