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
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.abiquo.domain.enterprise.Enterprise;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.domain.task.VirtualMachineTemplateTask;
import org.jclouds.abiquo.reference.ValidationErrors;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.rest.RestContext;

import com.abiquo.model.enumerator.DiskControllerType;
import com.abiquo.model.enumerator.DiskFormatType;
import com.abiquo.model.enumerator.EthernetDriverType;
import com.abiquo.model.enumerator.OSType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.server.core.appslibrary.TemplateDefinitionDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;

/**
 * Template Definitions are a summarized version of the OVF Envelope format.
 * They are available sources of templates that can be installed into a
 * datacenter repository, then referred to as virtual machine templates
 * (template definition in the context of a particular datacenter).
 * 
 * @author sacedo
 */
public class TemplateDefinition extends DomainWrapper<TemplateDefinitionDto> {

   private Enterprise enterprise;

   protected TemplateDefinition(RestContext<AbiquoApi, AbiquoAsyncApi> context, TemplateDefinitionDto target) {
      super(context, target);
   }

   // Domain operations

   public void save() {
      target = context.getApi().getEnterpriseApi().createTemplateDefinition(enterprise.unwrap(), target);
   }

   public void update() {
      target = context.getApi().getEnterpriseApi().updateTemplateDefinition(target);
   }

   public void delete() {
      context.getApi().getEnterpriseApi().deleteTemplateDefinition(target);
      target = null;
   }

   /**
    * Creates a new virtual machine template by downloading a template
    * definition into the given datacenter repository.
    * 
    * @param datacenter
    *           Where the new virtual machine template will be available.
    * @return the task to track the progress for the new virtual machine
    *         template creation process
    */
   public VirtualMachineTemplateTask downloadToRepository(final Datacenter datacenter) {
      checkNotNull(datacenter, "datacenter");
      RESTLink tDefLink = new RESTLink(ParentLinkName.TEMPLATE_DEFINITION, target.getEditLink().getHref());
      checkNotNull(tDefLink, "template definition edit link");
      Integer enterpriseId = target.getIdFromLink(ParentLinkName.ENTERPRISE);
      checkNotNull(enterpriseId, "template definition's enterprise link");

      VirtualMachineTemplateRequestDto request = new VirtualMachineTemplateRequestDto();
      request.getLinks().add(tDefLink);

      AcceptedRequestDto<String> response = context.getApi().getVirtualMachineTemplateApi()
            .createVirtualMachineTemplate(enterpriseId, datacenter.getId(), request);

      return getTask(response).asVirtualMachineTemplateTask();
   }

   // Parent access
   public Enterprise getEnterprise() {
      Integer enterpriseId = target.getIdFromLink(ParentLinkName.ENTERPRISE);
      return wrap(context, Enterprise.class, context.getApi().getEnterpriseApi().getEnterprise(enterpriseId));
   }

   // Builder
   public static Builder builder(final RestContext<AbiquoApi, AbiquoAsyncApi> context, final Enterprise enterprise) {
      return new Builder(context, enterprise);
   }

   public static class Builder {

      private final RestContext<AbiquoApi, AbiquoAsyncApi> context;

      private final Enterprise enterprise;

      private String url;

      private String name;

      private String description;

      private String productName;

      private String productVendor;

      private String productUrl;

      private String productVersion;

      private String iconUrl;

      private String diskFormatType;

      private long diskFileSize;

      private String loginUser;

      private String loginPassword;

      private OSType osType;

      private String osVersion;

      private EthernetDriverType ethernetDriverType;

      private DiskControllerType diskControllerType;

      public Builder(final RestContext<AbiquoApi, AbiquoAsyncApi> context, final Enterprise enterpirse) {
         super();
         this.context = context;
         this.enterprise = enterpirse;
      }

      public Builder url(final String url) {
         this.url = url;
         return this;
      }

      public Builder name(final String name) {
         this.name = name;
         return this;
      }

      public Builder description(final String description) {
         this.description = description;
         return this;
      }

      public Builder productName(final String productName) {
         this.productName = productName;
         return this;
      }

      public Builder productVendor(final String productVendor) {
         this.productVendor = productVendor;
         return this;
      }

      public Builder productUrl(final String productUrl) {
         this.productUrl = productUrl;
         return this;
      }

      public Builder productVersion(final String productVersion) {
         this.productVersion = productVersion;
         return this;
      }

      public Builder diskFormatType(final String diskFormatType) {
         this.diskFormatType = diskFormatType;
         return this;
      }

      public Builder loginUser(final String loginUser) {
         this.loginUser = loginUser;
         return this;
      }

      public Builder loginPassword(final String loginPassword) {
         this.loginPassword = iconUrl;
         return this;
      }

      public Builder iconUrl(final String iconUrl) {
         this.iconUrl = iconUrl;
         return this;
      }

      public Builder diskFileSize(final long diskFileSize) {
         this.diskFileSize = diskFileSize;
         return this;
      }

      public Builder osVersion(final String osVersion) {
         this.osVersion = osVersion;
         return this;
      }

      public Builder diskControllerType(final DiskControllerType diskControllerType) {
         checkNotNull(diskControllerType, ValidationErrors.NULL_RESOURCE + DiskControllerType.class);
         this.diskControllerType = diskControllerType;
         return this;
      }

      public Builder osType(final OSType osType) {
         checkNotNull(osType, ValidationErrors.NULL_RESOURCE + OSType.class);
         this.osType = osType;
         return this;
      }

      public Builder ethernetDriverType(final EthernetDriverType ethernetDriverType) {
         checkNotNull(osType, ValidationErrors.NULL_RESOURCE + EthernetDriverType.class);
         this.ethernetDriverType = ethernetDriverType;
         return this;
      }

      public TemplateDefinition build() {
         TemplateDefinitionDto dto = new TemplateDefinitionDto();
         dto.setUrl(url);
         dto.setName(name);
         dto.setDescription(description);
         dto.setLoginUser(loginUser);
         dto.setLoginPassword(loginPassword);
         dto.setProductName(productName);
         dto.setProductVendor(productVendor);
         dto.setProductVersion(productVersion);
         dto.setProductUrl(productUrl);
         dto.setIconUrl(iconUrl);
         dto.setDiskFileSize(diskFileSize);
         dto.setDiskFormatType(diskFormatType);
         dto.setEthernetDriverType(ethernetDriverType);
         dto.setOsVersion(osVersion);

         TemplateDefinition templateDefinition = new TemplateDefinition(context, dto);
         templateDefinition.setDiskControllerType(diskControllerType);
         templateDefinition.setOsType(osType);
         templateDefinition.setEthernetDriverType(ethernetDriverType);
         templateDefinition.enterprise = enterprise;

         return templateDefinition;
      }

      public static Builder fromTemplateDefinition(final TemplateDefinition in) {
         Builder builder = TemplateDefinition.builder(in.context, in.enterprise).name(in.getName())
               .description(in.getDescription()).url(in.getUrl()).loginUser(in.getLoginUser())
               .loginPassword(in.getLoginPassword()).productName(in.getProductName())
               .productVendor(in.getProductVendor()).productUrl(in.getProductUrl())
               .productVersion(in.getProductVersion()).diskControllerType(in.getDiskControllerType())
               .osType(in.getOsType()).diskFormatType(in.getDiskFormatType().name())
               .ethernetDriverType(in.getEthernetDriverType()).diskFileSize(in.getDiskFileSize())
               .diskControllerType(in.getDiskControllerType()).osVersion(in.getOsVersion());
         return builder;
      }
   }

   // Delegate methods

   public Integer getId() {
      return target.getId();
   }

   public String getUrl() {
      return target.getUrl();
   }

   public void setUrl(String url) {
      target.setUrl(url);
   }

   public String getName() {
      return target.getName();
   }

   public void setName(String name) {
      target.setName(name);
   }

   public String getDescription() {
      return target.getDescription();
   }

   public void setDescription(String description) {
      target.setDescription(description);
   }

   public String getProductName() {
      return target.getProductName();
   }

   public void setProductName(String productName) {
      target.setProductName(productName);
   }

   public String getProductVendor() {
      return target.getProductVendor();
   }

   public void setProductVendor(String productVendor) {
      target.setProductVendor(productVendor);
   }

   public String getProductUrl() {
      return target.getProductUrl();
   }

   public void setProductUrl(String productUrl) {
      target.setProductUrl(productUrl);
   }

   public String getProductVersion() {
      return target.getProductVersion();
   }

   public void setProductVersion(String productVersion) {
      target.setProductVersion(productVersion);
   }

   public String getIconUrl() {
      return target.getIconUrl();
   }

   public void setIconUrl(String iconUrl) {
      target.setIconUrl(iconUrl);
   }

   public DiskFormatType getDiskFormatType() {
      return DiskFormatType.valueOf(target.getDiskFormatType());
   }

   public void setDiskFormatType(String diskFormatType) {
      target.setDiskFormatType(diskFormatType);
   }

   public long getDiskFileSize() {
      return target.getDiskFileSize();
   }

   public void setDiskFileSize(long diskFileSize) {
      target.setDiskFileSize(diskFileSize);
   }

   public String getLoginUser() {
      return target.getLoginUser();
   }

   public void setLoginUser(String loginUser) {
      target.setLoginUser(loginUser);
   }

   public String getLoginPassword() {
      return target.getLoginPassword();
   }

   public void setLoginPassword(String loginPassword) {
      target.setLoginPassword(loginPassword);
   }

   public OSType getOsType() {
      return target.getOsType();
   }

   public void setOsType(OSType osType) {
      target.setOsType(osType);
   }

   public String getOsVersion() {
      return target.getOsVersion();
   }

   public void setOsVersion(String osVersion) {
      target.setOsVersion(osVersion);
   }

   public EthernetDriverType getEthernetDriverType() {
      return target.getEthernetDriverType();
   }

   public void setEthernetDriverType(EthernetDriverType ethernetDriverType) {
      target.setEthernetDriverType(ethernetDriverType);
   }

   public DiskControllerType getDiskControllerType() {
      return target.getDiskControllerType();
   }

   public void setDiskControllerType(DiskControllerType diskControllerType) {
      target.setDiskControllerType(diskControllerType);
   }

   @Override
   public String toString() {
      return "TemplateDefinition [id=" + getId() + ", url=" + getUrl() + ", name=" + getName() + ", description="
            + getDescription() + ", productName=" + getProductName() + ", productVendor=" + getProductVendor()
            + ", productUrl=" + getProductUrl() + ", productVersion=" + getProductVersion() + ", iconUrl="
            + getIconUrl() + ", diskFormatType=" + getDiskFormatType() + ", diskFileSize=" + getDiskFileSize()
            + ", loginUser=" + getLoginUser() + ", loginPassword=" + getLoginPassword() + ", OsType=" + getOsType()
            + ", OsVersion=" + getOsVersion() + ", EthernetDriverType=" + getEthernetDriverType()
            + ", diskControllerType=" + getDiskControllerType() + "]";
   }

}
