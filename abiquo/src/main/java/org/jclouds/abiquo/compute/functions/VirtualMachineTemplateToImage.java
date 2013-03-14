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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.Image.Status;
import org.jclouds.compute.domain.ImageBuilder;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;

import com.abiquo.model.rest.RESTLink;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * Transforms a {@link VirtualMachineTemplate} into an {@link Image}.
 * <p>
 * Images are scoped to a region (physical datacenter).
 * 
 * @author Ignasi Barrera
 */
@Singleton
public class VirtualMachineTemplateToImage implements Function<VirtualMachineTemplate, Image> {
   private final Function<Datacenter, Location> datacenterToLocation;

   private final Supplier<Map<Integer, Datacenter>> regionMap;

   private final Function<VirtualMachineTemplate, OperatingSystem> operatingSystem;

   private final Function<VirtualMachineTemplate, LoginCredentials> loginCredentials;

   private final Function<VirtualMachineTemplate, Status> status;

   @Inject
   public VirtualMachineTemplateToImage(final Function<Datacenter, Location> datacenterToLocation,
         @Memoized final Supplier<Map<Integer, Datacenter>> regionMap,
         final Function<VirtualMachineTemplate, OperatingSystem> operatingSystem,
         final Function<VirtualMachineTemplate, LoginCredentials> loginCredentials,
         final Function<VirtualMachineTemplate, Status> status) {
      this.datacenterToLocation = checkNotNull(datacenterToLocation, "datacenterToLocation");
      this.regionMap = checkNotNull(regionMap, "regionMap");
      this.operatingSystem = checkNotNull(operatingSystem, "operatingSystem");
      this.loginCredentials = checkNotNull(loginCredentials, "loginCredentials");
      this.status = checkNotNull(status, "status");
   }

   @Override
   public Image apply(final VirtualMachineTemplate template) {
      ImageBuilder builder = new ImageBuilder();
      builder.ids(template.getId().toString());
      builder.name(template.getName());
      builder.description(template.getDescription());

      // Location information
      Datacenter region = regionMap.get().get(template.unwrap().getIdFromLink(ParentLinkName.DATACENTER));
      builder.location(datacenterToLocation.apply(region));
      builder.operatingSystem(operatingSystem.apply(template));
      builder.defaultCredentials(loginCredentials.apply(template));
      builder.status(status.apply(template));
      builder.backendStatus(template.getState().name());

      RESTLink downloadLink = template.unwrap().searchLink("diskfile");
      builder.uri(downloadLink == null ? null : URI.create(downloadLink.getHref()));

      return builder.build();
   }

}
