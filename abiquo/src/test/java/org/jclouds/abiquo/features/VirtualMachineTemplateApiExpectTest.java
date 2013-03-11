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

import java.net.URI;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.cloud.options.VirtualMachineTemplateOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatesDto;

/**
 * Expect tests for the {@link VirtualMachineTemplateApi}.
 * 
 * @author Ignasi Barrera
 * 
 */
@Test(groups = "unit", testName = "VirtualMachineTemplateApiExpectTest")
public class VirtualMachineTemplateApiExpectTest extends BaseAbiquoRestApiExpectTest<VirtualMachineTemplateApi> {

   public void testCreateVirtualMachineTemplate_download() {
      VirtualMachineTemplateApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("POST")
                  .endpoint(
                        URI.create("http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(AcceptedRequestDto.MEDIA_TYPE))
                  .payload(
                        payloadFromResourceWithContentType("/payloads/virtualmachinetemplate-download-request.xml",
                              normalize(VirtualMachineTemplateRequestDto.MEDIA_TYPE))).build(),
            HttpResponse
                  .builder()
                  .statusCode(201)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/virtualmachinetemplate-create-response.xml",
                              normalize(AcceptedRequestDto.MEDIA_TYPE))).build());

      VirtualMachineTemplateRequestDto templateRequest = new VirtualMachineTemplateRequestDto();
      templateRequest.addLink(new RESTLink("templatedefinition",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"));

      assertAcceptedRequest(api.createVirtualMachineTemplate(1, 1, templateRequest));
   }

   public void testCreateVirtualMachineTemplate_promote() {
      VirtualMachineTemplateApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("POST")
                  .endpoint(
                        URI.create("http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(AcceptedRequestDto.MEDIA_TYPE))
                  .payload(
                        payloadFromResourceWithContentType("/payloads/virtualmachinetemplate-promote-request.xml",
                              normalize(VirtualMachineTemplateRequestDto.MEDIA_TYPE))).build(),
            HttpResponse
                  .builder()
                  .statusCode(201)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/virtualmachinetemplate-create-response.xml",
                              normalize(AcceptedRequestDto.MEDIA_TYPE))).build());

      VirtualMachineTemplateRequestDto templateRequest = new VirtualMachineTemplateRequestDto();
      templateRequest.setPromotedName("mypromoted");
      templateRequest.addLink(new RESTLink("virtualmachinetemplate",
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1"));

      assertAcceptedRequest(api.createVirtualMachineTemplate(1, 1, templateRequest));
   }

   private void assertAcceptedRequest(final AcceptedRequestDto<String> accepted) {
      assertNotNull(accepted.searchLink("status"));
      assertEquals(accepted.getStatusLink().getHref(),
            "http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/2/tasks");
   }

   public void testListVirtualMachineTemplatesWithPaginationOptions() {
      VirtualMachineTemplateApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("GET")
                  .endpoint(
                        URI.create("http://localhost/api/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates")) //
                  .addHeader("Authorization", basicAuth) //
                  .addHeader("Accept", normalize(VirtualMachineTemplatesDto.MEDIA_TYPE)) //
                  .addQueryParam("limit", "1") //
                  .addQueryParam("has", "text") //
                  .build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/templates-page.xml",
                              normalize(VirtualMachineTemplatesDto.MEDIA_TYPE))) //
                  .build());

      VirtualMachineTemplateOptions options = VirtualMachineTemplateOptions.builder().limit(1).has("text").build();
      VirtualMachineTemplatesDto result = api.listVirtualMachineTemplates(1, 1, options);

      assertEquals(result.getCollection().size(), 1);
      assertEquals(result.getTotalSize().intValue(), 2);
      assertEquals(result.getCollection().get(0).getId().intValue(), 151);
      assertNotNull(result.searchLink("first"));
      assertNotNull(result.searchLink("last"));
      assertNotNull(result.searchLink("next"));
   }

   @Override
   protected VirtualMachineTemplateApi clientFrom(AbiquoApi api) {
      return api.getVirtualMachineTemplateApi();
   }

}
