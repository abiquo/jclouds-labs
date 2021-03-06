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
package org.jclouds.abiquo.domain.enterprise;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWithLimitsWrapper;
import org.jclouds.abiquo.domain.builder.LimitsBuilder;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.predicates.LinkPredicates;
import org.jclouds.abiquo.reference.ValidationErrors;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.abiquo.strategy.enterprise.ListAllowedTiers;
import org.jclouds.rest.ApiContext;
import org.jclouds.rest.annotations.SinceApiVersion;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Adds high level functionality to {@link DatacenterLimitsDto}.
 * 
 * @author Ignasi Barrera
 * @author Francesc Montserrat
 * @see API: <a
 *      href="http://community.abiquo.com/display/ABI20/Datacenter+Limits+Resource"
 *      >
 *      http://community.abiquo.com/display/ABI20/Datacenter+Limits+Resource</a>
 */
public class Limits extends DomainWithLimitsWrapper<DatacenterLimitsDto> {
   /**
    * Constructor to be used only by the builder.
    */
   protected Limits(final ApiContext<AbiquoApi> context, final DatacenterLimitsDto target) {
      super(context, target);
   }

   // Domain operations

   /**
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Datacenter+Limits+Resource#DatacenterLimitsResource-UpdatesanexistingLimitforanenterpriseinadatacenter"
    *      > http://community.abiquo.com/display/ABI20/Datacenter+Limits+
    *      Resource #DatacenterLimitsResource
    *      -UpdatesanexistingLimitforanenterpriseinadatacenter</a>
    */
   public void update() {
      target = context.getApi().getEnterpriseApi().updateLimits(target);
   }

   /**
    * Allows a list of tiers to be used by an enterprise
    * 
    * @param tiers
    *           The list of tiers to be allowed
    */
   @SinceApiVersion("2.4")
   public void setAllowedTiers(List<Tier> tiers) {
      checkNotNull(tiers, ValidationErrors.NULL_RESOURCE + List.class + " of " + Tier.class);

      Iterables.removeIf(target.getLinks(), LinkPredicates.rel(ParentLinkName.TIER));

      for (Tier tier : tiers) {
         checkNotNull(tier.unwrap().getEditLink(), ValidationErrors.MISSING_REQUIRED_LINK + "edit");
         RESTLink link = new RESTLink(ParentLinkName.TIER, tier.unwrap().getEditLink().getHref());
         target.addLink(link);
      }

      context.getApi().getEnterpriseApi().updateLimits(target);
   }

   /**
    * Retrieve a list of all allowed tiers
    * 
    * @return a list of all allowed tiers
    */
   @SinceApiVersion("2.4")
   public List<Tier> getAllowedTiers() {
      ListAllowedTiers strategy = context.utils().injector().getInstance(ListAllowedTiers.class);
      return ImmutableList.copyOf(strategy.execute(this));
   }

   // ParentAccess

   public Enterprise getEnterprise() {
      Integer enterpriseId = target.getIdFromLink(ParentLinkName.ENTERPRISE);
      checkNotNull(enterpriseId, ValidationErrors.MISSING_REQUIRED_LINK);
      EnterpriseDto dto = context.getApi().getEnterpriseApi().getEnterprise(enterpriseId);
      return wrap(context, Enterprise.class, dto);
   }

   public Datacenter getDatacenter() {
      Integer datacenterId = target.getIdFromLink(ParentLinkName.DATACENTER);
      checkNotNull(datacenterId, ValidationErrors.MISSING_REQUIRED_LINK);
      DatacenterDto dto = context.getApi().getInfrastructureApi().getDatacenter(datacenterId);
      return wrap(context, Datacenter.class, dto);
   }

   // Builder

   public static Builder builder(final ApiContext<AbiquoApi> context, Datacenter datacenter) {
      return new Builder(context, datacenter);
   }

   public static class Builder extends LimitsBuilder<Builder> {
      private ApiContext<AbiquoApi> context;

      protected Long repositorySoft = Long.valueOf(DEFAULT_LIMITS);

      protected Long repositoryHard = Long.valueOf(DEFAULT_LIMITS);

      protected Datacenter datacenter;

      public Builder(final ApiContext<AbiquoApi> context, Datacenter datacenter) {
         super();
         this.context = context;
         this.datacenter = checkNotNull(datacenter, "datacenter");
      }

      public Builder repositoryLimits(final long soft, final long hard) {
         this.repositorySoft = soft;
         this.repositoryHard = hard;
         return this;
      }

      public Limits build() {
         DatacenterLimitsDto dto = new DatacenterLimitsDto();
         dto.setRamLimitsInMb(ramSoftLimitInMb, ramHardLimitInMb);
         dto.setCpuCountLimits(cpuCountSoftLimit, cpuCountHardLimit);
         dto.setHdLimitsInMb(hdSoftLimitInMb, hdHardLimitInMb);
         dto.setStorageLimits(storageSoft, storageHard);
         dto.setVlansLimits(vlansSoft, vlansHard);
         dto.setPublicIPLimits(publicIpsSoft, publicIpsHard);
         dto.setRepositoryHard(repositoryHard);
         dto.setRepositorySoft(repositorySoft);

         dto.addLink(new RESTLink(ParentLinkName.DATACENTER, checkNotNull(datacenter.unwrap().getEditLink(),
               "missing edit link").getHref()));

         Limits limits = new Limits(context, dto);

         return limits;
      }

      public static Builder fromLimits(final Limits in) {
         return Limits.builder(in.context, in.getDatacenter())
               .ramLimits(in.getRamSoftLimitInMb(), in.getRamHardLimitInMb())
               .cpuCountLimits(in.getCpuCountSoftLimit(), in.getCpuCountHardLimit())
               .hdLimitsInMb(in.getHdSoftLimitInMb(), in.getHdHardLimitInMb())
               .storageLimits(in.getStorageSoft(), in.getStorageHard())
               .vlansLimits(in.getVlansSoft(), in.getVlansHard())
               .publicIpsLimits(in.getPublicIpsSoft(), in.getPublicIpsHard())
               .repositoryLimits(in.getRepositorySoft(), in.getRepositoryHard());
      }
   }

   // Delegate methods

   public Integer getId() {
      return target.getId();
   }

   public long getRepositoryHard() {
      return target.getRepositoryHard();
   }

   public long getRepositorySoft() {
      return target.getRepositorySoft();
   }

   public void setRepositoryHard(final long repositoryHard) {
      target.setRepositoryHard(repositoryHard);
   }

   public void setRepositoryLimits(final long soft, final long hard) {
      target.setRepositoryHard(hard);
      target.setRepositorySoft(soft);
   }

   public void setRepositorySoft(final long repositorySoft) {
      target.setRepositorySoft(repositorySoft);
   }

   @Override
   public String toString() {
      return "Limits [id=" + getId() + ", repositoryHard=" + getRepositoryHard() + ", repositorySoft="
            + getRepositorySoft() + ", cpuCountHard=" + getCpuCountHardLimit() + ", cpuCountSoft="
            + getCpuCountSoftLimit() + ", hdHardInMB=" + getHdHardLimitInMb() + ", hdSoftInMB=" + getHdSoftLimitInMb()
            + ", publicIPsHard=" + getPublicIpsHard() + ", publicIpsSoft=" + getPublicIpsSoft() + ", ramHardInMB="
            + getRamHardLimitInMb() + ", ramSoftInMB=" + getRamSoftLimitInMb() + ", storageHard=" + getStorageHard()
            + ", storageSoft=" + getStorageSoft() + ", vlansHard=" + getVlansHard() + ", vlansSoft=" + getVlansSoft()
            + "]";
   }

}
