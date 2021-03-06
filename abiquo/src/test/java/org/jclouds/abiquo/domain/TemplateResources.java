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
package org.jclouds.abiquo.domain;

import static org.jclouds.abiquo.domain.DomainUtils.link;

import com.abiquo.model.enumerator.ConversionState;
import com.abiquo.model.enumerator.OSType;
import com.abiquo.model.enumerator.VMTemplateState;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.appslibrary.ConversionDto;
import com.abiquo.server.core.appslibrary.DatacenterRepositoryDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatePersistentDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;

/**
 * VM template domain utilities.
 * 
 * @author Ignasi Barrera
 * @author Francesc Montserrat
 */
public class TemplateResources {
   public static DatacenterRepositoryDto datacenterRepositoryPut() {
      DatacenterRepositoryDto dcRepository = new DatacenterRepositoryDto();
      dcRepository.setName("Datacenter Repo");
      dcRepository.setRepositoryCapacityMb(0l);
      dcRepository.setRepositoryLocation("10.60.1.104:/volume1/nfs-devel");
      dcRepository.setRepositoryRemainingMb(0l);
      dcRepository.addLink(new RESTLink("applianceManagerRepositoryUri", "http://localhost/am/erepos/1"));
      dcRepository.addLink(new RESTLink("datacenter", "http://localhost/api/admin/datacenters/1"));
      dcRepository.addLink(new RESTLink("edit", "http://localhost/api/admin/enterprises/1/datacenterrepositories/1"));
      dcRepository.addLink(new RESTLink("enterprise", "http://localhost/api/admin/enterprises/1"));
      dcRepository.addLink(new RESTLink("refresh",
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/actions/refresh"));
      dcRepository.addLink(new RESTLink("virtualmachinetemplates",
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates"));

      return dcRepository;
   }

   public static VirtualMachineTemplateDto virtualMachineTemplatePut() {
      VirtualMachineTemplateDto template = new VirtualMachineTemplateDto();
      template.setId(1);
      template.setName("Template");
      template.setDescription("Description");
      template.addLink(new RESTLink("edit",
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1"));
      template.addLink(new RESTLink("enterprise", "http://localhost/api/admin/enterprises/1"));
      template.addLink(new RESTLink("datacenter", "http://localhost/api/datacenters/1"));
      template.addLink(new RESTLink("conversions", "http://localhost/api/admin/enterprises/1"
            + "/datacenterrepositories/1/virtualmachinetemplates/1/conversions"));
      template.addLink(new RESTLink("tasks", "http://localhost/api/admin/enterprises/1"
            + "/datacenterrepositories/1/virtualmachinetemplates/1/tasks"));
      template.addLink(new RESTLink("diskfile", "http://somewher.com/file.vmdk"));
      template.setDiskFormatType("RAW");
      template.setOsType(OSType.MACOS);
      template.setLoginUser("myuser");
      template.setLoginPassword("mypass");
      template.setState(VMTemplateState.DONE);
      template.setCpuRequired(1);
      template.setRamRequired(1);
      template.setHdRequired(20l);
      template.setDiskFileSize(30l);

      template.setCostCode(0);
      template.setDiskFormatType("RAW");
      template.setOsType(OSType.MACOS);
      template.setLoginUser("myuser");
      template.setLoginPassword("mypass");
      template.setState(VMTemplateState.DONE);

      return template;
   }

   public static String virtualMachineTemplatePutPayload() {
      StringBuilder buffer = new StringBuilder();
      buffer.append("<virtualMachineTemplate>");
      buffer.append(link("/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1", "edit"));
      buffer.append(link("/admin/enterprises/1", "enterprise"));
      buffer.append(link("/datacenters/1", "datacenter"));
      buffer.append(link("/admin/enterprises/1" + "/datacenterrepositories/1/virtualmachinetemplates/1/conversions",
            "conversions"));
      buffer.append(link("/admin/enterprises/1" + "/datacenterrepositories/1/virtualmachinetemplates/1/tasks", "tasks"));
      buffer.append(link(new RESTLink("diskfile", "http://somewher.com/file.vmdk")));
      buffer.append("<id>1</id>");
      buffer.append("<name>Template</name>");
      buffer.append("<description>Description</description>");
      buffer.append("<diskFormatType>RAW</diskFormatType>");
      buffer.append("<osType>MACOS</osType>");
      buffer.append("<loginUser>myuser</loginUser>");
      buffer.append("<loginPassword>mypass</loginPassword>");
      buffer.append("<state>DONE</state>");

      buffer.append("<diskFileSize>30</diskFileSize>");
      buffer.append("<cpuRequired>1</cpuRequired>");
      buffer.append("<ramRequired>1</ramRequired>");
      buffer.append("<hdRequired>20</hdRequired>");
      buffer.append("<shared>false</shared>");
      buffer.append("<costCode>0</costCode>");
      buffer.append("<chefEnabled>false</chefEnabled>");
      buffer.append("</virtualMachineTemplate>");
      return buffer.toString();
   }

   public static VirtualMachineTemplatePersistentDto persistentData() {
      VirtualMachineTemplatePersistentDto dto = new VirtualMachineTemplatePersistentDto();
      dto.setPersistentTemplateName("New persistent template name");
      dto.setPersistentVolumeName("New persistent volume name");
      dto.addLink(new RESTLink("tier", "http://localhost/api/cloud/virtualdatacenters/1/tiers/1"));
      dto.addLink(new RESTLink("virtualdatacenter", "http://localhost/api/cloud/virtualdatacenters/1"));
      dto.addLink(new RESTLink("virtualmachinetemplate",
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1"));
      return dto;
   }

   public static String persistentPayload() {
      StringBuilder buffer = new StringBuilder();
      buffer.append("<virtualmachinetemplatepersistent>");
      buffer.append(link("/cloud/virtualdatacenters/1/tiers/1", "tier"));
      buffer.append(link("/cloud/virtualdatacenters/1", "virtualdatacenter"));
      buffer.append(link("/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1",
            "virtualmachinetemplate"));
      buffer.append("<persistentTemplateName>New persistent template name</persistentTemplateName>");
      buffer.append("<persistentVolumeName>New persistent volume name</persistentVolumeName>");
      buffer.append("</virtualmachinetemplatepersistent>");
      return buffer.toString();
   }

   public static VirtualMachineTemplateRequestDto templateRequestDownloadData() {
      VirtualMachineTemplateRequestDto templateRequest = new VirtualMachineTemplateRequestDto();
      templateRequest.addLink(new RESTLink("templatedefinition",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"));
      return templateRequest;
   }

   public static String templateRequestDownloadPlayload() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("<virtualmachinetemplaterequest>");
      buffer.append(link("/admin/enterprises/1/appslib/templateDefinitions/1", "templatedefinition"));
      buffer.append("</virtualmachinetemplaterequest>");
      return buffer.toString();
   }

   public static VirtualMachineTemplateRequestDto templateRequestPromoteData() {
      VirtualMachineTemplateRequestDto templateRequest = new VirtualMachineTemplateRequestDto();
      templateRequest.addLink(new RESTLink("virtualmachinetemplate",
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1"));
      templateRequest.setPromotedName("myname");
      return templateRequest;
   }

   public static String templateRequestPromotePlayload() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("<virtualmachinetemplaterequest>");
      buffer.append(link("/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1",
            "virtualmachinetemplate"));
      buffer.append("<promotedName>myname</promotedName>");
      buffer.append("</virtualmachinetemplaterequest>");
      return buffer.toString();
   }

   public static ConversionDto conversionPut() {
      ConversionDto conversion = new ConversionDto();
      conversion.setState(ConversionState.ENQUEUED);
      conversion.setSourceFormat("VMDK_STREAM_OPTIMIZED");
      conversion.setSourcePath("source/path.vmkd");
      conversion.setTargetFormat("RAW");
      conversion.setTargetPath("target/path.raw");
      conversion.setTargetSizeInBytes(1000000l);
      conversion
            .addLink(new RESTLink("edit",
                  "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/conversions/RAW"));
      conversion
            .addLink(new RESTLink("tasks",
                  "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/conversions/RAW/tasks"));

      return conversion;
   }

   public static String conversionPutPlayload() {
      StringBuilder buffer = new StringBuilder();
      buffer.append("<conversion>");
      buffer.append(link("/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/conversions/RAW",
            "edit"));
      buffer.append(link(
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/conversions/RAW/tasks", "tasks"));

      buffer.append("<state>ENQUEUED</state>");
      buffer.append("<sourceFormat>VMDK_STREAM_OPTIMIZED</sourceFormat>");
      buffer.append("<sourcePath>source/path.vmkd</sourcePath>");
      buffer.append("<targetFormat>RAW</targetFormat>");
      buffer.append("<targetPath>target/path.raw</targetPath>");
      buffer.append("<targetSizeInBytes>1000000</targetSizeInBytes>");
      buffer.append("</conversion>");
      return buffer.toString();
   }
}
