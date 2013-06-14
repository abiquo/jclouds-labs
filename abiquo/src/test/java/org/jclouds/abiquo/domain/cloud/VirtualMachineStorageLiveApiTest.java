/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.abiquo.domain.cloud;

import static org.jclouds.abiquo.reference.AbiquoTestConstants.PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jclouds.abiquo.domain.infrastructure.HypervisorType;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.domain.task.VirtualMachineTask;
import org.jclouds.abiquo.internal.BaseAbiquoApiLiveApiTest;
import org.jclouds.abiquo.predicates.infrastructure.HypervisorPredicates;
import org.jclouds.abiquo.predicates.infrastructure.TierPredicates;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * Live integration tests for the {@link VirtualMachine} storage operations.
 * 
 * @author Francesc Montserrat
 */
@Test(groups = "api", testName = "VirtualMachineStorageLiveApiTest")
public class VirtualMachineStorageLiveApiTest extends BaseAbiquoApiLiveApiTest {
   private Volume volume;

   private boolean hardDisksSupported;

   @BeforeClass
   public void setupVirtualDisks() {
      volume = createVolume();

      HypervisorType type = env.datacenter.findHypervisor(HypervisorPredicates.type(env.machine.getType()));
      hardDisksSupported = type.supportsExtraHardDisks();
   }

   @AfterClass
   public void tearDownVirtualDisks() {
      deleteVolume(volume);
   }

   public void testGetPrimaryDisk() {
      VirtualDisk<?> primaryDisk = env.virtualMachine.getPrimaryDisk();
      assertNotNull(primaryDisk);
      assertTrue(primaryDisk instanceof HardDisk);
   }

   public void testAttachVolume() {
      // Since the virtual machine is not deployed, this should not generate a
      // task
      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists.newArrayList(volume));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
      assertEquals(attached.size(), 1);
      assertEquals(attached.get(0).getId(), volume.getId());
   }

   @Test(dependsOnMethods = "testAttachVolume")
   public void testDetachVolume() {
      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
      assertTrue(attached.isEmpty());
   }

   @Test(dependsOnMethods = "testDetachVolume")
   public void testAttachHardDisk() {
      skipIfHardDisksNotSupported();

      HardDisk hardDisk = createHardDisk();

      // Since the virtual machine is not deployed, this should not generate a
      // task
      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists.newArrayList(hardDisk));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
      assertEquals(attached.size(), 1);
      assertEquals(attached.get(0).getId(), hardDisk.getId());
   }

   @Test(dependsOnMethods = "testAttachHardDisk")
   public void testDetachHardDisk() {
      skipIfHardDisksNotSupported();

      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
      assertTrue(attached.isEmpty());
   }

   @Test(dependsOnMethods = { "testDetachVolume", "testDetachHardDisk" })
   public void testAttachHardDiskAndVolume() {
      skipIfHardDisksNotSupported();

      HardDisk hardDisk = createHardDisk();

      // Since the virtual machine is not deployed, this should not generate a
      // task
      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists
            .<VirtualDisk<?>> newArrayList(hardDisk, volume));
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
      assertEquals(attached.size(), 2);
      assertEquals(attached.get(0).getId(), hardDisk.getId());
      assertEquals(attached.get(1).getId(), volume.getId());
   }

   @Test(dependsOnMethods = "testAttachHardDiskAndVolume")
   public void testReorderVirtualDisks() {
      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
      assertEquals(attached.size(), 2);
      Integer id0 = attached.get(0).getId();
      Integer id1 = attached.get(1).getId();

      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList(attached.get(1),
            attached.get(0)));
      assertNull(task);

      attached = env.virtualMachine.listAttachedVirtualDisks();
      assertEquals(attached.size(), 2);
      assertEquals(attached.get(0).getId(), id1);
      assertEquals(attached.get(1).getId(), id0);
   }

   @Test(dependsOnMethods = "testReorderVirtualDisks")
   public void testDetachAllVirtualDisks() {
      // Since the virtual machine is not deployed, this should not generate a
      // task
      VirtualMachineTask task = env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      assertNull(task);

      List<VirtualDisk<?>> attached = env.virtualMachine.listAttachedVirtualDisks();
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

   protected void skipIfHardDisksNotSupported() {
      if (!hardDisksSupported) {
         throw new SkipException(
               "Cannot perform this test because hard disk actions are not available for this hypervisor");
      }
   }
}
