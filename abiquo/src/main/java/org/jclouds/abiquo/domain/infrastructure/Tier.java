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
import static com.google.common.collect.Iterables.filter;

import java.util.List;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.abiquo.domain.enterprise.Enterprise;
import org.jclouds.abiquo.domain.enterprise.options.EnterpriseOptions;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.rest.ApiContext;
import org.jclouds.rest.annotations.SinceApiVersion;

import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolsDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Adds high level functionality to {@link TierDto}.
 * <p>
 * The Tier Resource offers the functionality of managing the logic of QoS
 * volume management. These are only logical levels of QoS and the real QoS
 * (networking speed, volume replication, availability) must be configured
 * manually in the infrastructure.
 * 
 * @author Ignasi Barrera
 * @author Francesc Montserrat
 * @see API: <a href="http://community.abiquo.com/display/ABI20/TierResource">
 *      http://community.abiquo.com/display/ABI20/TierResource</a>
 */
public class Tier extends DomainWrapper<TierDto> {
   /** The datacenter where the tier belongs. */
   private Datacenter datacenter;

   /**
    * Constructor to be used only by the builder.
    */
   protected Tier(final ApiContext<AbiquoApi> context, final TierDto target) {
      super(context, target);
   }

   // Domain operations

   /**
    * Update tier information in the server with the data from this tier.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/TierResource#TierResource-Updateatier"
    *      >
    *      http://community.abiquo.com/display/ABI20/TierResource#TierResource-
    *      Updateatier</a>
    */
   public void update() {
      target = context.getApi().getInfrastructureApi().updateTier(target);
   }

   /**
    * Retrieve the list of storage pools in this tier.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StoragePoolResource#StoragePoolResource-Retrievestoragepools"
    *      > http://community.abiquo.com/display/ABI20/StoragePoolResource#
    *      StoragePoolResource- Retrievestoragepools</a>
    * @return List of storage pools in this tier.
    */
   public List<StoragePool> listStoragePools() {
      StoragePoolsDto storagePools = context.getApi().getInfrastructureApi().listStoragePools(target);
      return wrap(context, StoragePool.class, storagePools.getCollection());
   }

   /**
    * Retrieve a filtered list of storage pools in this tier.
    * 
    * @param filter
    *           Filter to be applied to the list.
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StoragePoolResource#StoragePoolResource-Retrievestoragepools"
    *      > http://community.abiquo.com/display/ABI20/StoragePoolResource#
    *      StoragePoolResource- Retrievestoragepools</a>
    * @return Filtered list of storage pools in this tier.
    */
   public List<StoragePool> listStoragePools(final Predicate<StoragePool> filter) {
      return ImmutableList.copyOf(filter(listStoragePools(), filter));
   }

   /**
    * Retrieve the first storage pool matching the filter within the list of
    * pools in this tier.
    * 
    * @param filter
    *           Filter to be applied to the list.
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/StoragePoolResource#StoragePoolResource-Retrievestoragepools"
    *      > http://community.abiquo.com/display/ABI20/StoragePoolResource#
    *      StoragePoolResource- Retrievestoragepools</a>
    * @return First storage pool matching the filter or <code>null</code> if
    *         there is none.
    */
   public StoragePool findStoragePool(final Predicate<StoragePool> filter) {
      return Iterables.getFirst(filter(listStoragePools(), filter), null);
   }

   /**
    * Allow the tier to be used to all enterprises.
    */
   @SinceApiVersion("2.4")
   public void allowTierToAllEnterprises() {
      context.getApi().getInfrastructureApi().allowTierToAllEnterprises(target);
   }

   /**
    * Restrict the tier to all enterprises.
    * 
    * @param force
    *           Boolean indicating if the force the operation must succeed even
    *           if not all enterprises could be updated
    */
   @SinceApiVersion("2.4")
   public void restrictTierToAllEnterprises(boolean force) {
      context.getApi().getInfrastructureApi().restrictTierToAllEnterprises(target, force);
   }

   /**
    * Retrieve a list of enterprises with access to the tier.
    */
   @SinceApiVersion("2.4")
   public List<Enterprise> listAllowedEnterprises() {
      EnterprisesDto enterprises = context.getApi().getInfrastructureApi().listAllowedEnterprisesForTier(target);
      return wrap(context, Enterprise.class, enterprises.getCollection());
   }

   /**
    * Retrieve a list of enterprises with access to the tier.
    */
   @SinceApiVersion("2.4")
   public List<Enterprise> listAllowedEnterprises(EnterpriseOptions options) {
      EnterprisesDto enterprises = context.getApi().getInfrastructureApi()
            .listAllowedEnterprisesForTier(target, checkNotNull(options, "options"));
      return wrap(context, Enterprise.class, enterprises.getCollection());
   }

   // Parent access

   /**
    * Retrieve the datacenter where this tier is.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/DatacenterResource#DatacenterResource-Retrieveadatacenter"
    *      > http://community.abiquo.com/display/ABI20/DatacenterResource#
    *      DatacenterResource- Retrieveadatacenter</a>
    */
   public Datacenter getDatacenter() {
      Integer datacenterId = target.getIdFromLink(ParentLinkName.DATACENTER);
      DatacenterDto dto = context.getApi().getInfrastructureApi().getDatacenter(datacenterId);
      datacenter = wrap(context, Datacenter.class, dto);
      return datacenter;
   }

   // Delegate methods

   public String getDescription() {
      return target.getDescription();
   }

   public boolean getEnabled() {
      return target.getEnabled();
   }

   public Integer getId() {
      return target.getId();
   }

   public String getName() {
      return target.getName();
   }

   public void setDescription(final String description) {
      target.setDescription(description);
   }

   public void setEnabled(final boolean enabled) {
      target.setEnabled(enabled);
   }

   public void setName(final String name) {
      target.setName(name);
   }

   public boolean isAllowedByDefault() {
      return target.isDefaultAllowed();
   }

   public void setAllowedByDefault(final boolean allowedByDefault) {
      target.setDefaultAllowed(allowedByDefault);
   }

   @Override
   public String toString() {
      return "Tier [id=" + getId() + ", description=" + getDescription() + ", enabled=" + getEnabled() + ", name="
            + getName() + ", allowedByDefault=" + isAllowedByDefault() + "]";
   }
}
