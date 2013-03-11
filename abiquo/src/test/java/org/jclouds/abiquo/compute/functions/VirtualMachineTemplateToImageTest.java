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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.jclouds.abiquo.domain.DomainWrapper.wrap;
import static org.jclouds.abiquo.domain.TemplateResources.virtualMachineTemplatePut;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.AbiquoAsyncApi;
import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.Image.Status;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.rest.RestContext;
import org.testng.annotations.Test;

import com.abiquo.model.enumerator.VMTemplateState;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * Unit tests for the {@link VirtualMachineTemplateToImage} class.
 * 
 * @author Ignasi Barrera
 */
@Test(groups = "unit", testName = "VirtualMachineTemplateToImageTest")
public class VirtualMachineTemplateToImageTest {

   public void testVirtualMachineTemplateToImage() {

      VirtualMachineTemplateDto dto = virtualMachineTemplatePut();
      dto.setState(VMTemplateState.DONE);

      Image image = toImage(dto);

      assertEquals(image.getId(), dto.getId().toString());
      assertEquals(image.getName(), dto.getName());
      assertEquals(image.getDescription(), dto.getDescription());
      assertEquals(image.getUri(), URI.create(dto.searchLink("diskfile").getHref()));

      assertEquals(image.getStatus(), Status.AVAILABLE);

      assertEquals(image.getOperatingSystem().is64Bit(), dto.getOsType().is64Bit());
      assertEquals(image.getOperatingSystem().getName(), dto.getOsType().name());

      assertEquals(image.getDefaultCredentials().getUser(), dto.getLoginUser());
      assertEquals(image.getDefaultCredentials().getPassword(), dto.getLoginPassword());
      assertEquals(image.getDefaultCredentials().shouldAuthenticateSudo(), false);
   }

   public void testConvertWithoutDownloadLink() {
      VirtualMachineTemplateDto dto = virtualMachineTemplatePut();
      dto.setLinks(Collections.singletonList(dto.searchLink("datacenter"))); // required

      Image image = toImage(dto);
      assertNull(image.getUri());
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testConvertWithoutId() {
      toImage(new VirtualMachineTemplateDto());
   }

   private static Image toImage(VirtualMachineTemplateDto dto) {
      @SuppressWarnings("unchecked")
      RestContext<AbiquoApi, AbiquoAsyncApi> context = EasyMock.createMock(RestContext.class);
      return new MockVirtualMachineTemplate().apply(wrap(context, VirtualMachineTemplate.class, dto));
   }

   private static class MockVirtualMachineTemplate implements Function<VirtualMachineTemplate, Image> {
      Supplier<Map<Integer, Datacenter>> regionMap = mockRegionMap();

      Function<Datacenter, Location> dcToLocation = mockDatacenterToLocation();

      Function<VirtualMachineTemplate, LoginCredentials> vmtToLogin = mockTemplateToLogin();

      Function<VirtualMachineTemplate, OperatingSystem> vmtToOS = mockTemplateToOS();

      Function<VirtualMachineTemplate, Status> vmtToStatus = mockTemplateToStatus();

      @Override
      public Image apply(VirtualMachineTemplate input) {
         Image image = new VirtualMachineTemplateToImage(dcToLocation, regionMap, vmtToOS, vmtToLogin, vmtToStatus)
               .apply(input);
         verify(regionMap);
         verify(dcToLocation);
         verify(vmtToLogin);
         verify(vmtToOS);
         verify(vmtToStatus);
         return image;
      }
   }

   private static Function<Datacenter, Location> mockDatacenterToLocation() {
      @SuppressWarnings("unchecked")
      Function<Datacenter, Location> mock = EasyMock.createMock(Function.class);
      expect(mock.apply(anyObject(Datacenter.class))).andReturn(null);
      replay(mock);
      return mock;
   }

   private static Function<VirtualMachineTemplate, LoginCredentials> mockTemplateToLogin() {
      @SuppressWarnings("unchecked")
      Function<VirtualMachineTemplate, LoginCredentials> mock = EasyMock.createMock(Function.class);
      expect(mock.apply(anyObject(VirtualMachineTemplate.class))).andDelegateTo(
            new VirtualMachineTemplateToLoginCredentials());
      replay(mock);
      return mock;
   }

   private static Function<VirtualMachineTemplate, OperatingSystem> mockTemplateToOS() {
      @SuppressWarnings("unchecked")
      Function<VirtualMachineTemplate, OperatingSystem> mock = EasyMock.createMock(Function.class);
      expect(mock.apply(anyObject(VirtualMachineTemplate.class))).andDelegateTo(
            new VirtualMachineTemplateToOperatingSystem());
      replay(mock);
      return mock;
   }

   private static Function<VirtualMachineTemplate, Status> mockTemplateToStatus() {
      @SuppressWarnings("unchecked")
      Function<VirtualMachineTemplate, Status> mock = EasyMock.createMock(Function.class);
      expect(mock.apply(anyObject(VirtualMachineTemplate.class))).andDelegateTo(new VirtualMachineTemplateToStatus());
      replay(mock);
      return mock;
   }

   @SuppressWarnings("unchecked")
   private static Supplier<Map<Integer, Datacenter>> mockRegionMap() {
      Supplier<Map<Integer, Datacenter>> mock = EasyMock.createMock(Supplier.class);
      expect(mock.get()).andReturn(Collections.EMPTY_MAP);
      replay(mock);
      return mock;
   }

   public static IMockBuilder<VirtualMachineTemplate> mockBuilderVirtualMachineTemplate(
         final VirtualMachineTemplateDto dto) {
      @SuppressWarnings("unchecked")
      RestContext<AbiquoApi, AbiquoAsyncApi> cntx = EasyMock.createMock(RestContext.class);
      return EasyMock.createMockBuilder(VirtualMachineTemplate.class).withConstructor(cntx, dto);
   }
}
