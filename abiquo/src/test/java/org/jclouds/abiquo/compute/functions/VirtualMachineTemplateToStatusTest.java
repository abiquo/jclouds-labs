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
import org.jclouds.compute.domain.Image.Status;
import org.testng.annotations.Test;

import com.abiquo.model.enumerator.VMTemplateState;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;

/**
 * Unit tests for the {@link VirtualMachineTemplateToStatus} class.
 */
@Test(groups = "unit", testName = "VirtualMachineTemplateToStatusTest")
public class VirtualMachineTemplateToStatusTest {

   public void testVirtualMachineTemplateToStatusAvailable() {
      testVirtualMachineTemplateToStatus(Status.AVAILABLE, VMTemplateState.DONE);
   }

   public void testVirtualMachineTemplateToStatusPending() {
      testVirtualMachineTemplateToStatus(Status.PENDING, VMTemplateState.IN_PROGRESS);
   }

   public void testVirtualMachineTemplateToStatusError() {
      testVirtualMachineTemplateToStatus(Status.ERROR, VMTemplateState.FAILED);
   }

   public void testVirtualMachineTemplateToStatusAllVMTemplateState() {
      assertEquals(VMTemplateState.values().length, 3); // we have 3 tests
   }

   private void testVirtualMachineTemplateToStatus(final Status status, final VMTemplateState vmttemplateState) {
      VirtualMachineTemplateDto dto = TemplateResources.virtualMachineTemplatePut();
      dto.setState(vmttemplateState);

      VirtualMachineTemplate template = mockBuilderVirtualMachineTemplate(dto).addMockedMethod("getState").createMock();
      expect(template.getState()).andReturn(dto.getState());
      replay(template);
      Status statusRes = new VirtualMachineTemplateToStatus().apply(template);
      verify(template);

      assertEquals(statusRes, status);
   }
}
