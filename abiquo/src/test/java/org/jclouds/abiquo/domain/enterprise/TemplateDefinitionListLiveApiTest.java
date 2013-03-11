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

package org.jclouds.abiquo.domain.enterprise;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jclouds.abiquo.domain.cloud.TemplateDefinition;
import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.abiquo.domain.task.VirtualMachineTemplateTask;
import org.jclouds.abiquo.internal.BaseAbiquoApiLiveApiTest;
import org.jclouds.abiquo.predicates.cloud.VirtualMachineTemplatePredicates;
import org.jclouds.abiquo.predicates.enterprise.TemplateDefinitionListPredicates;
import org.jclouds.abiquo.util.Config;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.abiquo.model.enumerator.VMTemplateState;
import com.abiquo.server.core.task.TaskState;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;

/**
 * Live integration tests for the {@link TemplateDefinitionList} domain class.
 *
 * @author Francesc Montserrat
 */
@Test(groups = "api", testName = "TemplateDefinitionListLiveApiTest")
public class TemplateDefinitionListLiveApiTest extends BaseAbiquoApiLiveApiTest {
   private TemplateDefinitionList list;

   public void testUpdate() {
      list.setName(list.getName() + "Updated");
      list.update();

      List<TemplateDefinitionList> lists = env.enterprise.listTemplateDefinitionLists(TemplateDefinitionListPredicates
            .name("myListUpdated"));

      assertEquals(lists.size(), 1);
   }

   public void testListStates() {
      List<TemplateState> states = list.listStatus(env.datacenter);
      assertNotNull(states);
   }

   @BeforeClass
   public void setup() {
      list = TemplateDefinitionList.builder(env.context.getApiContext(), env.enterprise).name("myList")
            .url(Config.get("abiquo.template.repository", "http://template-repository.herokuapp.com/ovfindex.xml"))
            .build();

      list.save();

      assertNotNull(list.getId());
   }

   @AfterClass
   public void tearDown() {
      Integer idTemplateList = list.getId();
      list.delete();
      assertNull(env.enterprise.getTemplateDefinitionList(idTemplateList));
   }

   @Test
   public void testDownload() {
      TemplateDefinition templateDef = templateBySize().min(list.listDefinitions());

      List<VirtualMachineTemplate> templates = env.enterprise.listTemplatesInRepository(env.datacenter,
            VirtualMachineTemplatePredicates.templateDefinition(templateDef));

      assertEquals(templates.size(), 0, "template present in datacenter");

      VirtualMachineTemplateTask task = templateDef.downloadToRepository(env.datacenter);
      env.context.getMonitoringService().getAsyncTaskMonitor().awaitCompletion(30l, TimeUnit.MINUTES, task);
      assertEquals(task.getState(), TaskState.FINISHED_SUCCESSFULLY);

      VirtualMachineTemplate vmt = task.getResult();
      try {
         assertVirtualMachineCreation(vmt, templateDef);

         templates = env.enterprise.listTemplatesInRepository(env.datacenter,
               VirtualMachineTemplatePredicates.templateDefinition(templateDef));
         assertEquals(templates.size(), 1, "template not present in datacenter");
      } finally {
         vmt.delete();
      }
   }

   private void assertVirtualMachineCreation(VirtualMachineTemplate template, TemplateDefinition templateDef) {
      assertEquals(template.getState(), VMTemplateState.DONE);
      assertEquals(template.getUrl().get(), templateDef.getUrl());
      assertEquals(template.getName(), templateDef.getName());
      assertEquals(template.getDescription(), templateDef.getDescription());
      assertEquals(template.getLoginUser(), templateDef.getLoginUser());
      assertEquals(template.getLoginPassword(), templateDef.getLoginPassword());
      assertEquals(template.getDiskFileSize() / 1048576, templateDef.getDiskFileSize());
      assertEquals(template.getDiskFormatType(), templateDef.getDiskFormatType());
      assertEquals(template.getEthernetDriverType(), templateDef.getEthernetDriverType());
      assertEquals(template.getDiskControllerType(), templateDef.getDiskControllerType());
      assertEquals(template.getIconUrl(), templateDef.getIconUrl());
      assertEquals(template.getOsType(), templateDef.getOsType());
      assertEquals(template.getOsVersion(), templateDef.getOsVersion());
   }

   private static Ordering<TemplateDefinition> templateBySize() {
      return new Ordering<TemplateDefinition>() {
         @Override
         public int compare(final TemplateDefinition left, final TemplateDefinition right) {
            return Longs.compare(left.getDiskFileSize(), right.getDiskFileSize());
         }
      };
   }

}
