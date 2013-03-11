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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.jclouds.abiquo.compute.functions.VirtualMachineTemplateToImageTest.mockBuilderVirtualMachineTemplate;
import static org.testng.Assert.assertEquals;

import org.jclouds.abiquo.domain.TemplateResources;
import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.compute.domain.OperatingSystem;
import org.testng.annotations.Test;

import com.abiquo.model.enumerator.OSType;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;

/**
 * Unit tests for the {@link VirtualMachineTemplateToOperatingSystem} class.
 */
@Test(groups = "unit", testName = "VirtualMachineTemplateToOperatingSystemTest")
public class VirtualMachineTemplateToOperatingSystemTest {

   public void testVirtualMachineTemplateToOSMacos() {
      OperatingSystem os = testVirtualMachineTemplateToOS(OSType.MACOS, "Lion");
      assertEquals(os.getName().toUpperCase(), OSType.MACOS.name().toUpperCase());
      assertEquals(os.getVersion(), "Lion");
   }

   public void testVirtualMachineTemplateToOSOptionalDescription() {
      OperatingSystem os = testVirtualMachineTemplateToOS(OSType.MACOS, null);
      assertEquals(os.getName().toUpperCase(), OSType.MACOS.name().toUpperCase());
   }

   public void testVirtualMachineTemplateToOSNullables() {
      OperatingSystem os = testVirtualMachineTemplateToOS(null, null);
      assertEquals(os.getName().toUpperCase(), OSType.UNRECOGNIZED.name().toUpperCase());
   }

   private OperatingSystem testVirtualMachineTemplateToOS(final OSType ostype, final String version) {
      VirtualMachineTemplateDto dto = TemplateResources.virtualMachineTemplatePut();
      dto.setOsType(ostype);
      dto.setOsVersion(version);

      VirtualMachineTemplate template = mockBuilderVirtualMachineTemplate(dto).addMockedMethod("getOsType")
            .addMockedMethod("getOsVersion").createMock();
      expect(template.getOsType()).andReturn(dto.getOsType());
      expect(template.getOsVersion()).andReturn(dto.getOsVersion());
      replay(template);
      OperatingSystem os = new VirtualMachineTemplateToOperatingSystem().apply(template);
      verify(template);
      return os;
   }
}
