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

import java.io.IOException;
import java.net.URI;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

import com.abiquo.model.enumerator.DiskControllerType;
import com.abiquo.model.enumerator.EthernetDriverType;
import com.abiquo.model.enumerator.OSType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.appslibrary.TemplateDefinitionDto;
import com.abiquo.server.core.appslibrary.TemplateDefinitionListDto;
import com.abiquo.server.core.appslibrary.TemplateDefinitionsDto;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;

/**
 * Expect tests for the {@link EnterpriseApi} class.
 * 
 * @author Sergi Castro
 * @author Susana Acedo
 */
@Test(groups = "unit", testName = "EnterpriseApiExpectTest")
public class EnterpriseApiExpectTest extends BaseAbiquoRestApiExpectTest<EnterpriseApi> {

   public void testGetLimitReturns2xx() throws SecurityException, NoSuchMethodException, IOException {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest.builder().method("GET")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/limits/1"))
                  .addHeader("Authorization", basicAuth).addHeader("Accept", normalize(DatacenterLimitsDto.MEDIA_TYPE))
                  .build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/limit.xml",
                              normalize(DatacenterLimitsDto.MEDIA_TYPE))).build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("limits", "http://localhost/api/admin/enterprises/1/limits");
      enterprise.addLink(link);

      DatacenterLimitsDto limit = api.getLimit(enterprise, 1);
      RESTLink editLink = limit.getEditLink();
      assertNotNull(editLink);
      assertEquals(editLink.getId().intValue(), 1);
      assertEquals(limit.getId().intValue(), 1);
      RESTLink entLink = limit.searchLink("enterprise");
      assertNotNull(entLink);
      assertEquals(entLink.getId().intValue(), 1);
   }

   public void testGetLimitReturns4xx() throws SecurityException, NoSuchMethodException, IOException {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest.builder().method("GET")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/limits/1"))
                  .addHeader("Authorization", basicAuth).addHeader("Accept", normalize(DatacenterLimitsDto.MEDIA_TYPE))
                  .build(), HttpResponse.builder().statusCode(404).build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("limits", "http://localhost/api/admin/enterprises/1/limits");
      enterprise.addLink(link);

      DatacenterLimitsDto limit = api.getLimit(enterprise, 1);
      assertNull(limit);
   }

   public void testListTemplateDefinitionsWhenResponseIs2xx() {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest.builder().method("GET")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/appslib/templateDefinitions"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(TemplateDefinitionsDto.MEDIA_TYPE)).build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/all-templatedefinitions.xml",
                              normalize(TemplateDefinitionsDto.MEDIA_TYPE))).build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("appslib/templateDefinitions",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions");
      enterprise.addLink(link);

      TemplateDefinitionsDto tmpdefinitions = api.listTemplateDefinitions(enterprise);
      assertEquals(tmpdefinitions.getCollection().size(), 1);
      assertEquals(tmpdefinitions.getCollection().get(0).getId(), Integer.valueOf(1));
      assertEquals(tmpdefinitions.getCollection().get(0).getName(), "Centos 5.6 x86_64");
      assertNotNull(tmpdefinitions.getCollection().get(0).getEditLink());
   }

   public void testRefreshTemplateDefinitionsWhenResponseIs2xx() {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest.builder().method("PUT")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(TemplateDefinitionListDto.MEDIA_TYPE)).build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/templatedefinitionlist.xml",
                              normalize(TemplateDefinitionListDto.MEDIA_TYPE))).build());

      TemplateDefinitionListDto list = new TemplateDefinitionListDto();
      list.addEditLink(new RESTLink("edit", "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"));

      TemplateDefinitionListDto tmpdefinitionlist = api.refreshTemplateDefinitionList(list);
      assertEquals(tmpdefinitionlist.getTemplateDefinitions().getCollection().size(), 1);
      assertEquals(tmpdefinitionlist.getTemplateDefinitions().getCollection().get(0).getId(), Integer.valueOf(1));
      assertEquals(tmpdefinitionlist.getTemplateDefinitions().getCollection().get(0).getName(), "Centos 5.6 x86_64");
      assertNotNull(tmpdefinitionlist.getTemplateDefinitions().getCollection().get(0).getEditLink());
   }

   public void testGetTemplateDefinitionWhenResponseIs2xx() {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest.builder().method("GET")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(TemplateDefinitionDto.MEDIA_TYPE)).build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/template_definition_response.xml",
                              normalize(TemplateDefinitionDto.MEDIA_TYPE))) //
                  .build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("appslib/templateDefinitions",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions");
      enterprise.addLink(link);

      TemplateDefinitionDto tmpdefinition = api.getTemplateDefinition(enterprise, 1);
      assertEquals(tmpdefinition.getId(), Integer.valueOf(1));
      assertEquals(tmpdefinition.getName(), "Centos 5.6 x86_64");
   }

   public void testGetTemplateDefinitionWhenResponseIs404() {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest.builder().method("GET")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(TemplateDefinitionDto.MEDIA_TYPE)).build(), HttpResponse.builder()
                  .statusCode(404).build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("appslib/templateDefinitions",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions");
      enterprise.addLink(link);

      TemplateDefinitionDto tmpdefinition = api.getTemplateDefinition(enterprise, 1);
      assertNull(tmpdefinition);
   }

   public void testCreateTemplateDefinition() {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("POST")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/appslib/templateDefinitions"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(TemplateDefinitionDto.MEDIA_TYPE))
                  .payload(
                        payloadFromResourceWithContentType("/payloads/template_definition_request.xml",
                              normalize(TemplateDefinitionDto.MEDIA_TYPE))) //
                  .build(),
            HttpResponse
                  .builder()
                  .statusCode(201)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/template_definition_response.xml",
                              normalize(TemplateDefinitionDto.MEDIA_TYPE))) //
                  .build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("appslib/templateDefinitions",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions");
      enterprise.addLink(link);

      TemplateDefinitionDto dto = new TemplateDefinitionDto();
      RESTLink categorylink = new RESTLink("category", "http://localhost:80/api/config/categories/6");
      categorylink.setTitle("OS");
      dto.addLink(categorylink);
      dto.setName("Centos 5.6 x86_64");
      dto.setUrl("http://abiquo-repository.abiquo.com/centos5/centos5.ovf");
      dto.setDescription("Centos 5.  Log in as 'root' with password 'abiquo'.");
      dto.setProductName("Centos 5.6 x86_64");
      dto.setDiskFormatType("QCOW2_SPARSE");
      dto.setDiskFileSize(614);
      dto.setOsType(OSType.CENTOS_64);
      dto.setEthernetDriverType(EthernetDriverType.E1000);
      dto.setDiskControllerType(DiskControllerType.IDE);
      dto.setIconUrl("http://abiquo-repository.abiquo.com/centos5/centos.png");

      TemplateDefinitionDto tmpdefinition = api.createTemplateDefinition(enterprise, dto);
      assertEquals(tmpdefinition.getName(), "Centos 5.6 x86_64");
      assertNotNull(tmpdefinition.searchLink("edit"));
   }

   public void testUpdateTemplateDefinition() {
      EnterpriseApi api = requestSendsResponse(
            HttpRequest
                  .builder()
                  .method("PUT")
                  .endpoint(URI.create("http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1"))
                  .addHeader("Authorization", basicAuth)
                  .addHeader("Accept", normalize(TemplateDefinitionDto.MEDIA_TYPE))
                  .payload(
                        payloadFromResourceWithContentType("/payloads/template_definition_update_request.xml",
                              normalize(TemplateDefinitionDto.MEDIA_TYPE))) //
                  .build(),
            HttpResponse
                  .builder()
                  .statusCode(200)
                  .payload(
                        payloadFromResourceWithContentType("/payloads/template_definition_update_response.xml",
                              normalize(TemplateDefinitionDto.MEDIA_TYPE))) //
                  .build());

      EnterpriseDto enterprise = new EnterpriseDto();
      RESTLink link = new RESTLink("appslib/templateDefinitions",
            "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions");
      enterprise.addLink(link);

      TemplateDefinitionDto dto = new TemplateDefinitionDto();
      RESTLink tmplink = new RESTLink("edit", "http://localhost/api/admin/enterprises/1/appslib/templateDefinitions/1");
      dto.addLink(tmplink);
      RESTLink categorylink = new RESTLink("category", "http://localhost:80/api/config/categories/6");
      categorylink.setTitle("OS");
      dto.addLink(categorylink);
      dto.setName("Centos 5.6 x86_64 updated");
      dto.setUrl("http://abiquo-repository.abiquo.com/centos5/centos5.ovf");
      dto.setDescription("Centos 5.  Log in as 'root' with password 'abiquo'.");
      dto.setProductName("Centos 5.6 x86_64");
      dto.setDiskFormatType("QCOW2_SPARSE");
      dto.setDiskFileSize(614);
      dto.setOsType(OSType.CENTOS_64);
      dto.setEthernetDriverType(EthernetDriverType.E1000);
      dto.setDiskControllerType(DiskControllerType.IDE);
      dto.setIconUrl("http://abiquo-repository.abiquo.com/centos5/centos.png");

      TemplateDefinitionDto tmpdefinition = api.updateTemplateDefinition(dto);
      assertEquals(tmpdefinition.getName(), "Centos 5.6 x86_64 updated");
      assertNotNull(tmpdefinition.searchLink("edit"));
   }

   @Override
   protected EnterpriseApi clientFrom(AbiquoApi api) {
      return api.getEnterpriseApi();
   }

}
