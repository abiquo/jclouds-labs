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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.abiquo.reference.ValidationErrors;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseXMLWithJAXB;
import org.jclouds.rest.ApiContext;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.cloud.LayerDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualMachineWithNodeExtendedDto;
import com.abiquo.server.core.cloud.VirtualMachinesWithNodeExtendedDto;
import com.google.common.base.Strings;
import com.google.inject.TypeLiteral;

/**
 * Abiquo has added layer concept in order to offer Anti Host Affinity
 * allocation between virtual machines. That means that one virtual appliance
 * can have different layers, and a virtual machine could belong to a layer (it
 * is not mandatory). Virtual machines with same layer will deploy in different
 * host to assure appliance stability.
 * 
 * @author Susana Acedo
 */
public class Layer extends DomainWrapper<LayerDto> {

   /**
    * Constructor to be used only by the builder. This resource cannot be
    * created.
    */
   private Layer(final ApiContext<AbiquoApi> context, final LayerDto target) {
      super(context, target);
   }

   /**
    * Creates a new layer.
    */
   public void save() {
      // retrieve the virtual appliance based on the virtual machine link
      // current approach vm -> vapp required 2 additional HTTP requests
      RESTLink vmlink = target.searchLink(ParentLinkName.VIRTUAL_MACHINE);
      checkNotNull(vmlink, ValidationErrors.MISSING_REQUIRED_LINK + VirtualMachine.class);

      vmlink.setType(VirtualMachineWithNodeExtendedDto.MEDIA_TYPE);
      HttpResponse response = context.getApi().get(vmlink);

      VirtualMachine vm = wrap(context, VirtualMachine.class, new ParseXMLWithJAXB<VirtualMachineWithNodeExtendedDto>(
            context.utils().xml(), TypeLiteral.get(VirtualMachineWithNodeExtendedDto.class)).apply(response));
      VirtualApplianceDto vappDto = vm.getVirtualAppliance().unwrap();

      target = context.getApi().getCloudApi().createLayer(vappDto, target);
   }

   /**
    * Removes the layer.
    * */
   public void delete() {
      context.getApi().getCloudApi().deleteLayer(target);
      target = null;
   }

   /**
    * Updates the layer.
    */
   public void update() {
      target = context.getApi().getCloudApi().updateLayer(target);
   }

   // Builder

   public static Builder builder(final ApiContext<AbiquoApi> context) {
      return new Builder(context);
   }

   public static class Builder {
      private ApiContext<AbiquoApi> context;

      private String name;

      private VirtualMachine virtualMachine;

      public Builder(final ApiContext<AbiquoApi> context) {
         super();
         this.context = context;
      }

      public Builder name(final String name) {
         this.name = checkNotNull(name, "name must not be null");
         return this;
      }

      public Builder virtualMachine(final VirtualMachine virtualMachine) {
         this.virtualMachine = virtualMachine;
         return this;
      }

      public Layer build() {
         checkNotNull(virtualMachine, ValidationErrors.NULL_RESOURCE + VirtualMachine.class);
         checkArgument(!Strings.isNullOrEmpty(name), ValidationErrors.MISSING_REQUIRED_FIELD + "name");

         LayerDto dto = new LayerDto();
         dto.setName(name);
         dto.addLink(new RESTLink(ParentLinkName.VIRTUAL_MACHINE, virtualMachine.unwrap().getEditLink().getHref()));
         return new Layer(context, dto);
      }
   }

   public List<VirtualMachine> listVirtualMachines() {
      VirtualMachinesWithNodeExtendedDto vms = new VirtualMachinesWithNodeExtendedDto();

      List<RESTLink> vmLinks = target.searchLinks(ParentLinkName.VIRTUAL_MACHINE);
      for (RESTLink vmLink : vmLinks) {
         vmLink.setType(VirtualMachineWithNodeExtendedDto.MEDIA_TYPE);
         HttpResponse response = context.getApi().get(vmLink);

         vms.add(new ParseXMLWithJAXB<VirtualMachineWithNodeExtendedDto>(context.utils().xml(), TypeLiteral
               .get(VirtualMachineWithNodeExtendedDto.class)).apply(response));
      }

      return wrap(context, VirtualMachine.class, vms.getCollection());
   }

   public String getName() {
      return target.getName();
   }

   public void setName(final String name) {
      target.setName(name);
   }

   @Override
   public String toString() {
      return "Layer [name=" + getName() + "]";
   }
}
