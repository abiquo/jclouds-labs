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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.List;

import org.jclouds.abiquo.internal.BaseAbiquoApiLiveApiTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "api", testName = "LayersLiveApiTest")
public class LayersLiveApiTest extends BaseAbiquoApiLiveApiTest {

   private Layer layer1;

   private Layer layer2;

   private VirtualMachine vm1;

   private VirtualMachine vmOther;

   private VirtualMachine vm2;

   @BeforeClass
   public void setupLayers() {
      vm1 = VirtualMachine.Builder.fromVirtualMachine(env.virtualMachine).build();
      vm1.save();

      vm2 = VirtualMachine.Builder.fromVirtualMachine(env.virtualMachine).build();
      vm2.save();

      layer1 = Layer.builder(env.context.getApiContext()).name("layer1").virtualMachine(vm1).build();
      layer1.save();

      layer2 = Layer.builder(env.context.getApiContext()).name("layer2").virtualMachine(vm2).build();
      layer2.save();

      // Creates a new virtual machine inside the layer
      vmOther = VirtualMachine.Builder.fromVirtualMachine(env.virtualMachine).layer(layer1).build();
      vmOther.save();

      layer1.refresh();
   }

   @AfterClass
   public void tearDownLayers() {
      vmOther.delete();

      layer1.delete();
      layer2.delete();

      vm1.delete();
      vm2.delete();
   }

   public void testListLayers() {
      List<Layer> layers = env.virtualAppliance.listLayers();
      assertEquals(layers.size(), 2);
   }

   public void testGetLayer() {
      Layer layer = env.virtualAppliance.getLayer(layer1.getName());
      assertEquals(layer.getName(), layer1.getName());
   }

   public void testListVirtualMachinesInLayer() {
      assertEquals(layer1.listVirtualMachines().size(), 2);
      assertEquals(layer2.listVirtualMachines().size(), 1);
   }

   public void testUpdateLayer() {
      layer2.setName("updatedName");
      layer2.update();

      Layer updated = env.virtualAppliance.getLayer("updatedName");
      assertNotNull(updated);

      Layer old = env.virtualAppliance.getLayer("layer2");
      assertNull(old);
   }
}
