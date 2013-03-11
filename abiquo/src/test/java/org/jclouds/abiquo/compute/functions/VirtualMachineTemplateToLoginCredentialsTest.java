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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.jclouds.abiquo.domain.TemplateResources;
import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.domain.LoginCredentials;
import org.testng.annotations.Test;

import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;

/**
 * Unit tests for the {@link VirtualMachineTemplateToLoginCredentialsTest}
 * class.
 */
@Test(groups = "unit", testName = "VirtualMachineTemplateToLoginCredentialsTest")
public class VirtualMachineTemplateToLoginCredentialsTest {

   public void testVirtualMachineTemplateToLoginCredentials() {
      LoginCredentials login = testVirtualMachineTemplateToLoginCredentials("user", "pass");
      assertEquals(login.getUser(), "user");
      assertEquals(login.getPassword(), "pass");
   }

   public void testVirtualMachineTemplateToLoginCredentialsMissingPass() {
      LoginCredentials login = testVirtualMachineTemplateToLoginCredentials("user", null);
      assertNotNull(login.getUser());
      assertNull(login.getPassword());
   }

   public void testVirtualMachineTemplateToLoginCredentialsMissingUsser() {
      LoginCredentials login = testVirtualMachineTemplateToLoginCredentials(null, "pass");
      assertNull(login.getUser());
      assertNotNull(login.getPassword());
   }

   public void testVirtualMachineTemplateToLoginCredentialsNullable() {
      LoginCredentials login = testVirtualMachineTemplateToLoginCredentials(null, null);
      assertNull(login.getUser());
      assertNull(login.getPassword());
   }

   private LoginCredentials testVirtualMachineTemplateToLoginCredentials(final String user, final String pass) {
      VirtualMachineTemplateDto dto = TemplateResources.virtualMachineTemplatePut();
      dto.setLoginUser(user);
      dto.setLoginPassword(pass);

      VirtualMachineTemplate template = mockBuilderVirtualMachineTemplate(dto).addMockedMethod("getLoginUser")
            .addMockedMethod("getLoginPassword").createMock();
      expect(template.getLoginUser()).andReturn(dto.getLoginUser());
      expect(template.getLoginPassword()).andReturn(dto.getLoginPassword());
      replay(template);
      LoginCredentials login = new VirtualMachineTemplateToLoginCredentials().apply(template);
      verify(template);
      return login;
   }
}
