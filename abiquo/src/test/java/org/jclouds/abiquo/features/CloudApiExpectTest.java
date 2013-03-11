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

package org.jclouds.abiquo.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.net.URI;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.server.core.cloud.LayerDto;
import com.abiquo.server.core.cloud.LayersDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineInstanceDto;
import com.abiquo.server.core.cloud.VirtualMachinesWithNodeExtendedDto;

/**
 * Expect tests for the {@link CloudApi} class.
 * 
 * @author Ignasi Barrera
 */
@Test(groups = "unit", testName = "CloudApiExpectTest")
public class CloudApiExpectTest extends BaseAbiquoRestApiExpectTest<CloudApi> {

   public void testListAllVirtualMachinesWhenResponseIs2xx() {
      CloudApi api = requestSendsResponse(
            HttpRequest.builder() //
                  .method("GET") //
                  .endpoint(URI.create("http://localhost/api/cloud/virtualmachines")) //
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(VirtualMachinesWithNodeExtendedDto.MEDIA_TYPE)) //
                  .build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/all-vms.xml",
                              normalize(VirtualMachinesWithNodeExtendedDto.MEDIA_TYPE))) //
                  .build());

      VirtualMachinesWithNodeExtendedDto vms = api.listAllVirtualMachines();
      assertEquals(vms.getCollection().size(), 1);
      assertEquals(vms.getCollection().get(0).getId(), Integer.valueOf(1));
      assertEquals(vms.getCollection().get(0).getName(), "VM");
      assertNotNull(vms.getCollection().get(0).getEditLink());
   }

   public void testListVirtualAppliancesLayersWhenResponseIs2xx() {
      CloudApi api = requestSendsResponse(
            HttpRequest.builder().method("GET")
                  .endpoint(URI.create("http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers")) //
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(LayersDto.MEDIA_TYPE)) //
                  .build(),
            HttpResponse.builder().statusCode(200)
                  .payload(payloadFromResourceWithContentType("/payloads/layers.xml", normalize(LayersDto.MEDIA_TYPE))) //
                  .build());

      VirtualApplianceDto vappDto = new VirtualApplianceDto();
      RESTLink link = new RESTLink("layers",
            "http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers");
      vappDto.addLink(link);

      LayersDto layers = api.listLayers(vappDto);

      assertEquals(layers.getCollection().size(), 1);
      assertEquals(layers.getCollection().get(0).getName(), "layer1");
      assertNotNull(layers.getCollection().get(0).searchLink("virtualmachine"));
   }

   public void testGetLayerWhenResponseIs2xx() {
      CloudApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("GET")
                  .endpoint(
                        URI.create("http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers/layer1"))
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(LayerDto.MEDIA_TYPE)) //
                  .build(),
            HttpResponse.builder().statusCode(200)
                  .payload(payloadFromResourceWithContentType("/payloads/layer.xml", normalize(LayerDto.MEDIA_TYPE))) //
                  .build());

      VirtualApplianceDto vappDto = new VirtualApplianceDto();
      RESTLink link = new RESTLink("layers",
            "http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers");
      vappDto.addLink(link);

      LayerDto layer = api.getLayer(vappDto, "layer1");
      assertEquals(layer.getName(), "layer1");
      assertNotNull(layer.searchLink("virtualmachine"));
   }

   public void testGetLayerWhenResponseIs404() {
      CloudApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("GET")
                  .endpoint(
                        URI.create("http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers/nonexistentlayer"))
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(LayerDto.MEDIA_TYPE)) //
                  .build(), HttpResponse.builder().statusCode(404).build());

      VirtualApplianceDto vappDto = new VirtualApplianceDto();
      RESTLink link = new RESTLink("layers",
            "http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers");
      vappDto.addLink(link);

      LayerDto layer = api.getLayer(vappDto, "nonexistentlayer");
      assertNull(layer);
   }

   public void testUpdateLayer() {
      CloudApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("PUT")
                  .endpoint(
                        URI.create("http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers/layer1"))
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(LayerDto.MEDIA_TYPE))
                  //
                  .payload(
                        payloadFromResourceWithContentType("/payloads/updatelayer-request.xml",
                              normalize(LayerDto.MEDIA_TYPE))) //
                  .build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/updatelayer-response.xml",
                              normalize(LayerDto.MEDIA_TYPE))) //
                  .build());

      LayerDto dto = new LayerDto();
      RESTLink link = new RESTLink("edit",
            "http://localhost/api/cloud/virtualdatacenters/1/virtualappliances/1/layers/layer1");
      link.setType(LayerDto.BASE_MEDIA_TYPE);
      dto.setName("updatedName");
      dto.addLink(link);

      LayerDto layer = api.updateLayer(dto);
      assertEquals(layer.getName(), "updatedName");
      assertNotNull(layer.searchLink("virtualmachine"));
   }

   public void testSnapshotVirtualMachineReturns2xx() {
      CloudApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("POST")
                  .endpoint(
                        URI.create("http://localhost/api/admin/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/instance")) //
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(AcceptedRequestDto.MEDIA_TYPE)) //
                  .payload(
                        payloadFromResourceWithContentType("/payloads/vm-snapshot.xml",
                              normalize(VirtualMachineInstanceDto.MEDIA_TYPE))) //
                  .build(), //
            HttpResponse
                  .builder()
                  .statusCode(202)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/vm-accepted-request.xml",
                              normalize(VirtualMachineInstanceDto.MEDIA_TYPE))).build());

      VirtualMachineDto vm = new VirtualMachineDto();
      vm.addLink(new RESTLink("instance",
            "http://localhost/api/admin/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/instance"));
      VirtualMachineInstanceDto snapshotConfig = new VirtualMachineInstanceDto();
      snapshotConfig.setInstanceName("foo");

      AcceptedRequestDto<String> taskRef = api.snapshotVirtualMachine(vm, snapshotConfig);
      assertNotNull(taskRef);
   }

   @Override
   protected CloudApi clientFrom(AbiquoApi api) {
      return api.getCloudApi();
   }

}
