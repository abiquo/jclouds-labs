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

import static org.jclouds.abiquo.reference.AbiquoTestConstants.PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.domain.task.AsyncTask;
import org.jclouds.abiquo.internal.BaseAbiquoApiLiveApiTest;
import org.jclouds.abiquo.predicates.infrastructure.TierPredicates;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.abiquo.model.enumerator.HypervisorType;
import com.google.common.collect.Lists;

/**
 * Live integration tests for the {@link VirtualMachine} storage operations.
 * 
 * @author Francesc Montserrat
 */
@Test(groups = "api", testName = "VirtualMachineStorageLiveApiTest")
public class VirtualMachineStorageLiveApiTest extends BaseAbiquoApiLiveApiTest {
   private Volume volume;

   private HardDisk hardDisk;

   @BeforeClass
   public void setupVirtualDisks() {
      volume = createVolume();
      hardDisk = createHardDisk();
   }

   @AfterClass
   public void tearDownVirtualDisks() {
      deleteVolume(volume);
      deleteHardDisk(hardDisk);
   }

   public void testAttachVolume() {
      // Since the virtual machine is not deployed, this should not generate a
      // task
      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.newArrayList(volume));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertEquals(attached.size(), 1);
      assertEquals(attached.get(0).getId(), volume.getId());
   }

   @Test(dependsOnMethods = "testAttachVolume")
   public void testDetachVolume() {
      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertTrue(attached.isEmpty());
   }

   public void testAttachHardDisk() {
      skipIfHardDisksNotSupported();

      // Since the virtual machine is not deployed, this should not generate a
      // task
      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.newArrayList(hardDisk));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertEquals(attached.size(), 1);
      assertEquals(attached.get(0).getId(), hardDisk.getId());
   }

   @Test(dependsOnMethods = "testAttachHardDisk")
   public void testDetachHardDisk() {
      skipIfHardDisksNotSupported();

      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertTrue(attached.isEmpty());
   }

   @Test(dependsOnMethods = { "testDetachVolume", "testDetachHardDisk" })
   public void testAttachHardDiskAndVolume() {
      skipIfHardDisksNotSupported();

      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList(hardDisk, volume));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertEquals(attached.size(), 2);
      assertEquals(attached.get(0).getId(), hardDisk.getId());
      assertEquals(attached.get(1).getId(), volume.getId());
   }

   @Test(dependsOnMethods = "testAttachHardDiskAndVolume")
   public void testReorderVirtualDisks() {
      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList(volume, hardDisk));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertEquals(attached.size(), 2);
      assertEquals(attached.get(0).getId(), volume.getId());
      assertEquals(attached.get(1).getId(), hardDisk.getId());
   }

   @Test(dependsOnMethods = "testReorderVirtualDisks")
   public void testDetachAllVirtualDisks() {
      AsyncTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listVirtualDisks();
      assertTrue(attached.isEmpty());
   }

   private Volume createVolume() {
      Tier tier = env.virtualDatacenter.findStorageTier(TierPredicates.name(env.tier.getName()));

      Volume volume = Volume.builder(env.context.getApiContext(), env.virtualDatacenter, tier)
            .name(PREFIX + "Hawaian volume").sizeInMb(32).build();
      volume.save();

      assertNotNull(volume.getId());
      assertNotNull(env.virtualDatacenter.getVolume(volume.getId()));

      return volume;
   }

   private void deleteVolume(final Volume volume) {
      Integer id = volume.getId();
      volume.delete();
      assertNull(env.virtualDatacenter.getVolume(id));
   }

   private HardDisk createHardDisk() {
      HardDisk hardDisk = HardDisk.builder(env.context.getApiContext(), env.virtualDatacenter).sizeInMb(64L).build();
      hardDisk.save();

      assertNotNull(hardDisk.getId());
      assertNotNull(hardDisk.getSequence());

      return hardDisk;
   }

   private void deleteHardDisk(final HardDisk hardDisk) {
      Integer id = hardDisk.getId();
      hardDisk.delete();
      assertNull(env.virtualDatacenter.getHardDisk(id));
   }

   protected static void skipIfHardDisksNotSupported() {
      if (!env.machine.getType().equals(HypervisorType.VMX_04)) {
         throw new SkipException(
               "Cannot perform this test because hard disk actions are not available for this hypervisor");
      }
   }
}
