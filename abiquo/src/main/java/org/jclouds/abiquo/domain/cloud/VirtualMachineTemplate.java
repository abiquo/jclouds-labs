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
package org.jclouds.abiquo.domain.cloud;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.abiquo.domain.cloud.options.ConversionOptions;
import org.jclouds.abiquo.domain.config.Category;
import org.jclouds.abiquo.domain.config.CostCode;
import org.jclouds.abiquo.domain.enterprise.Enterprise;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.domain.task.ConversionTask;
import org.jclouds.abiquo.domain.task.VirtualMachineTemplateTask;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseXMLWithJAXB;
import org.jclouds.rest.ApiContext;

import com.abiquo.model.enumerator.ConversionState;
import com.abiquo.model.enumerator.DiskControllerType;
import com.abiquo.model.enumerator.EthernetDriverType;
import com.abiquo.model.enumerator.OSType;
import com.abiquo.model.enumerator.VMTemplateState;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.server.core.appslibrary.CategoryDto;
import com.abiquo.server.core.appslibrary.ConversionDto;
import com.abiquo.server.core.appslibrary.ConversionsDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatePersistentDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;
import com.abiquo.server.core.pricing.CostCodeDto;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.TypeLiteral;

/**
 * Adds high level functionality to {@link VirtualMachineTemplateDto}.
 * 
 * @author Ignasi Barrera
 * @author Francesc Montserrat
 * @see API: <a href=
 *      "http://community.abiquo.com/display/ABI20/Virtual+Machine+Template+Resource"
 *      > http://community.abiquo.com/display/ABI20/Virtual+Machine+Template+
 *      Resource </a>
 */
public class VirtualMachineTemplate extends DomainWrapper<VirtualMachineTemplateDto> {
   /**
    * Constructor to be used only by the builder.
    */
   protected VirtualMachineTemplate(final ApiContext<AbiquoApi> context, final VirtualMachineTemplateDto target) {
      super(context, target);
   }

   // Domain operations

   public void delete() {
      context.getApi().getVirtualMachineTemplateApi().deleteVirtualMachineTemplate(target);
      target = null;
   }

   public void update() {
      target = context.getApi().getVirtualMachineTemplateApi().updateVirtualMachineTemplate(target);
   }

   public VirtualMachineTemplateTask makePersistent(final VirtualDatacenter vdc, final Volume volume,
         final String persistentTemplateName) {
      RESTLink storageLink = volume.unwrap().getEditLink();
      storageLink.setRel("volume");
      return makePersistent(vdc, storageLink, persistentTemplateName, null);
   }

   public VirtualMachineTemplateTask makePersistent(final VirtualDatacenter vdc, final Tier tier,
         final String persistentTemplateName, final String persistentVolumeName) {
      // infrastructure
      RESTLink storageLink = tier.unwrap().getEditLink();
      if (storageLink == null) {
         // cloud
         storageLink = tier.unwrap().searchLink("self");
      }
      storageLink.setRel(ParentLinkName.TIER);
      return makePersistent(vdc, storageLink, persistentTemplateName, persistentVolumeName);
   }

   private VirtualMachineTemplateTask makePersistent(final VirtualDatacenter vdc, final RESTLink storageLink,
         final String persistentTemplateName, final String persistentVolumeName) {
      VirtualMachineTemplatePersistentDto persistentData = new VirtualMachineTemplatePersistentDto();
      persistentData.setPersistentTemplateName(persistentTemplateName);
      persistentData.setPersistentVolumeName(persistentVolumeName);
      RESTLink vdcLink = new RESTLink(ParentLinkName.VIRTUAL_DATACENTER, vdc.unwrap().getEditLink().getHref());
      RESTLink templateLink = new RESTLink(ParentLinkName.VIRTUAL_MACHINE_TEMPLATE, target.getEditLink().getHref());

      persistentData.addLink(vdcLink);
      persistentData.addLink(storageLink);
      persistentData.addLink(templateLink);

      // SCG:
      // A simple user should not have permissions to obtain a datacenter
      // repository, but at this
      // point we have the datacenter repository and enterprise ids in the own
      // target uri. So we
      // can obtain the path where do the POST
      // Assumption that to create a new object a user needs to get the parent
      // object cannot be
      // applied in this case
      String editUri = getURI().getPath();
      Pattern p = Pattern.compile("\\d+");
      Matcher m = p.matcher(editUri);
      m.find();
      Integer idEnt = new Integer(m.group());
      m.find();
      Integer idDcRepo = new Integer(m.group());

      AcceptedRequestDto<String> response = context.getApi().getVirtualMachineTemplateApi()
            .createPersistentVirtualMachineTemplate(idEnt, idDcRepo, persistentData);

      return getTask(response).asVirtualMachineTemplateTask();
   }

   /**
    * Creates a new virtual machine template by copy an already existing virtual
    * machine template instance disk files, the new template won't be an
    * instance and its not related to the original virtual machine template (you
    * can delete the instance once the promoted is finished). Conversions are
    * copied as well.
    * 
    * @param promotedName
    *           Desired name for the new virtual machine template
    * @return the task to track the progress for the new virtual machine
    *         template creation process
    */
   public VirtualMachineTemplateTask promoteToMaster(final String promotedName) {

      RESTLink vmtLink = new RESTLink(ParentLinkName.VIRTUAL_MACHINE_TEMPLATE, target.getEditLink().getHref());
      Integer repositoryId = target.getIdFromLink(ParentLinkName.DATACENTER_REPOSITORY);
      Integer enterpriseId = target.getIdFromLink(ParentLinkName.ENTERPRISE);
      checkNotNull(vmtLink, "virtual machine template edit link");
      checkNotNull(enterpriseId, "virtual machine template's enterprise link");
      checkNotNull(repositoryId, "virtual machine template's datacenter repository link");

      VirtualMachineTemplateRequestDto request = new VirtualMachineTemplateRequestDto();
      request.setPromotedName(promotedName);
      request.getLinks().add(vmtLink);

      AcceptedRequestDto<String> response = context.getApi().getVirtualMachineTemplateApi()
            .createVirtualMachineTemplate(enterpriseId, repositoryId, request);

      return getTask(response).asVirtualMachineTemplateTask();
   }

   // Children access

   /**
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Category+Resource#CategoryResource-Retrieveacategory"
    *      > http://community.abiquo.com/display/ABI20/Category+Resource#
    *      CategoryResource- Retrieveacategory</a>
    */
   public Category getCategory() {
      Integer categoryId = target.getIdFromLink(ParentLinkName.CATEGORY);
      CategoryDto category = context.getApi().getConfigApi().getCategory(categoryId);
      return wrap(context, Category.class, category);
   }

   /**
    * @see API: <a href=
    *      "http://community.abiquo.com/display/Abiquo/Volume+Resource#VolumeResource-Retrieveavolume"
    *      > http://community.abiquo.com/display/Abiquo/Volume+Resource#
    *      VolumeResource- Retrieveavolume</a>
    */
   public Volume getVolume() {
      if (this.isPersistent()) {
         HttpResponse rp = checkNotNull(context.getApi().get(target.searchLink("volume")), "volume");

         ParseXMLWithJAXB<VolumeManagementDto> parser = new ParseXMLWithJAXB<VolumeManagementDto>(
               context.utils().xml(), TypeLiteral.get(VolumeManagementDto.class));

         VolumeManagementDto dto = parser.apply(rp);
         return new Volume(context, dto);
      }
      return null;
   }

   public boolean isPersistent() {
      return target.searchLink("volume") != null;
   }

   // Parent access

   /**
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Enterprise+Resource#EnterpriseResource-RetrieveanEnterprise"
    *      > http://community.abiquo.com/display/ABI20/Enterprise+Resource#
    *      EnterpriseResource- RetrieveanEnterprise</a>
    */
   public Enterprise getEnterprise() {
      Integer enterpriseId = target.getIdFromLink(ParentLinkName.ENTERPRISE);
      return wrap(context, Enterprise.class, context.getApi().getEnterpriseApi().getEnterprise(enterpriseId));
   }

   /**
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Datacenter+Resource#DatacenterResource-RetrieveaDatacenter"
    *      > http://community.abiquo.com/display/ABI20/Datacenter+Resource#
    *      DatacenterResource- RetrieveaDatacenter</a>
    */
   public Datacenter getDatacenter() {
      Integer repositoryId = target.getIdFromLink(ParentLinkName.DATACENTER_REPOSITORY);
      return wrap(context, Datacenter.class, context.getApi().getInfrastructureApi().getDatacenter(repositoryId));
   }

   /**
    * List all the conversions for the virtual machine template.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Conversion+Resource#ConversionResource-ListConversions"
    *      > http://community.abiquo.com/display/ABI20/Conversion+Resource#
    *      ConversionResource- ListConversions</a>
    * @return all the conversions of the virtual machine template
    */
   public List<Conversion> listConversions() {
      ConversionsDto convs = context.getApi().getVirtualMachineTemplateApi().listConversions(target);
      return wrap(context, Conversion.class, convs.getCollection());
   }

   /**
    * List all the conversions for the virtual machine template matching the
    * given filter.
    * 
    * @param filter
    *           The filter to apply.
    * @return The list all the conversions for the virtual machine template
    *         matching the given filter.
    */
   public List<Conversion> listConversions(final Predicate<Conversion> filter) {
      return ImmutableList.copyOf(filter(listConversions(), filter));
   }

   /**
    * Gets a single conversion in the virtual machine template matching the
    * given filter.
    * 
    * @param filter
    *           The filter to apply.
    * @return The conversion or <code>null</code> if none matched the given
    *         filter.
    */
   public Conversion findConversion(final Predicate<Conversion> filter) {
      return Iterables.getFirst(filter(listConversions(), filter), null);
   }

   /**
    * List conversions for a virtual machine template.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Conversion+Resource#ConversionResource-ListConversions"
    *      > http://community.abiquo.com/display/ABI20/Conversion+Resource#
    *      ConversionResource- ListConversions</a>
    * @param hypervisor
    *           , Optionally filter conversions compatible with the provided
    *           hypervisor
    * @param state
    *           , Optionally filter conversions with the desired state
    * @return all the conversions of the virtual machine template applying the
    *         constrains
    */
   public List<Conversion> listConversions(final String hypervisor, final ConversionState state) {
      ConversionsDto convs = context
            .getApi()
            .getVirtualMachineTemplateApi()
            .listConversions(target,
                  ConversionOptions.builder().hypervisorType(hypervisor).conversionState(state).build());
      return wrap(context, Conversion.class, convs.getCollection());
   }

   /**
    * Starts a new conversion for a virtual machine template.
    * 
    * @see API: <a href=
    *      "http://community.abiquo.com/display/ABI20/Conversion+Resource#ConversionResource-RequestConversion"
    *      > http://community.abiquo.com/display/ABI20/Conversion+Resource#
    *      ConversionResource- RequestConversion</a>
    * @param diskFormat
    *           , desired target format for the request template
    * @return The task reference to track its progress
    */
   public ConversionTask requestConversion(final String diskFormat) {
      ConversionDto request = new ConversionDto();
      request.setTargetFormat(diskFormat);

      AcceptedRequestDto<String> taskRef = context.getApi().getVirtualMachineTemplateApi()
            .requestConversion(target, diskFormat, request);

      return taskRef == null ? null : getTask(taskRef).asConversionTask();
   }

   public CostCode getCostCode() {
      Integer costcodeId = target.getIdFromLink(ParentLinkName.COST_CODE);
      CostCodeDto costcode = context.getApi().getPricingApi().getCostCode(costcodeId);
      return wrap(context, CostCode.class, costcode);
   }

   // Delegate methods

   public Integer getCpuRequired() {
      return target.getCpuRequired();
   }

   public Date getCreationDate() {
      return target.getCreationDate();
   }

   public String getCreationUser() {
      return target.getCreationUser();
   }

   public String getDescription() {
      return target.getDescription();
   }

   public Long getDiskFileSize() {
      return target.getDiskFileSize();
   }

   public String getDiskFormatType() {
      return target.getDiskFormatType();
   }

   public Long getHdRequired() {
      return target.getHdRequired();
   }

   public String getName() {
      return target.getName();
   }

   public String getPath() {
      return target.getPath();
   }

   public Integer getRamRequired() {
      return target.getRamRequired();
   }

   public boolean isChefEnabled() {
      return target.isChefEnabled();
   }

   public void setChefEnabled(final boolean chefEnabled) {
      target.setChefEnabled(chefEnabled);
   }

   public void setName(final String name) {
      target.setName(name);
   }

   public String getLoginUser() {
      return target.getLoginUser();
   }

   public void setLoginUser(final String loginUser) {
      target.setLoginUser(loginUser);
   }

   public String getLoginPassword() {
      return target.getLoginPassword();
   }

   public void setLoginPassword(final String loginPassword) {
      target.setLoginPassword(loginPassword);
   }

   public OSType getOsType() {
      return target.getOsType();
   }

   public void setOsType(final OSType osType) {
      target.setOsType(osType);
   }

   public String getOsVersion() {
      return target.getOsVersion();
   }

   public void setOsVersion(final String osVersion) {
      target.setOsVersion(osVersion);
   }

   public DiskControllerType getDiskControllerType() {
      return target.getDiskControllerType();
   }

   public void setDiskControllerType(final DiskControllerType diskControllerType) {
      target.setDiskControllerType(diskControllerType);
   }

   public EthernetDriverType getEthernetDriverType() {
      return target.getEthernetDriverType();
   }

   public void setEthernetDriverType(final EthernetDriverType ethernetDriverType) {
      target.setEthernetDriverType(ethernetDriverType);
   }

   public Integer getId() {
      return target.getId();
   }

   public String getIconUrl() {
      return target.getIconUrl();
   }

   public VMTemplateState getState() {
      return target.getState();
   }

   /**
    * Only downloaded virtual machine templates can request the source
    * {@link TemplateDefinition} source URL
    * 
    * @return the source OVF Document URL, present in
    *         {@link TemplateDefinition#getUrl()}
    */
   public Optional<String> getUrl() {
      Optional<RESTLink> anylink = Optional.fromNullable(target.searchLink("templatedefinition"));
      return Optional.fromNullable(anylink.isPresent() ? anylink.get().getHref() : null);
   }

   @Override
   public String toString() {
      return "VirtualMachineTemplate [id=" + getId() + ", cpuRequired=" + getCpuRequired() + ", creationDate="
            + getCreationDate() + ", creationUser=" + getCreationUser() + ", description=" + getDescription()
            + ", diskFileSize=" + getDiskFileSize() + ", diskFormatType=" + getDiskFormatType() + ", hdRequired="
            + getHdRequired() + ", name=" + getName() + ", path=" + getPath() + ", ramRequired=" + getRamRequired()
            + ", chefEnabled=" + isChefEnabled() + ", state=" + getState() + ", ostype=" + getOsType() + "]";
   }

}
