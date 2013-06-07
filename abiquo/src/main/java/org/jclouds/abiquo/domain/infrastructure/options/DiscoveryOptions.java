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
package org.jclouds.abiquo.domain.infrastructure.options;

import org.jclouds.http.options.BaseHttpRequestOptions;

/**
 * Available options to discover physical machines.
 * 
 * @author Ignasi Barrera
 */
public class DiscoveryOptions extends BaseHttpRequestOptions {
   public static Builder builder() {
      return new Builder();
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      DiscoveryOptions options = new DiscoveryOptions();
      options.queryParameters.putAll(queryParameters);
      return options;
   }

   public static class Builder {
      private String hypervisorType;
      private String ip;
      private Integer port;
      private String user;
      private String password;
      private String managerIp;
      private Integer managerPort;
      private String managerUser;
      private String managerPassword;
      private String agentIp;
      private Integer agentPort;
      private String agentUser;
      private String agentPassword;

      /**
       * The type of the hypervisor of the machines to discover.
       */
      public Builder hypervisorType(String hypervisorType) {
         this.hypervisorType = hypervisorType;
         return this;
      }

      /**
       * The ip address of the machine to discover.
       */
      public Builder ip(String ip) {
         this.ip = ip;
         return this;
      }

      /**
       * The port used to connect to the target machine.
       */
      public Builder port(Integer port) {
         this.port = port;
         return this;
      }

      /**
       * The credentials used to access the machine.
       */
      public Builder credentials(String user, String password) {
         this.user = user;
         this.password = password;
         return this;
      }

      /**
       * The ip address of the manager host used to access the machine to
       * discover.
       */
      public Builder managerIp(String managerIp) {
         this.managerIp = managerIp;
         return this;
      }

      /**
       * The port in the manager host used to access the machine to discover.
       */
      public Builder managerPort(Integer managerPort) {
         this.managerPort = managerPort;
         return this;
      }

      /**
       * The credentials used to access the host manager.
       */
      public Builder managerCredentials(String managerUser, String managerPassword) {
         this.managerUser = managerUser;
         this.managerPassword = managerPassword;
         return this;
      }

      /**
       * The ip address of the agent used to access the machine to discover.
       */
      public Builder agentIp(String agentIp) {
         this.agentIp = agentIp;
         return this;
      }

      /**
       * The port in the agent used to access the machine to discover.
       */
      public Builder agentPort(Integer agentPort) {
         this.agentPort = agentPort;
         return this;
      }

      /**
       * The credentials used to access the agent.
       */
      public Builder agentCredentials(String agentUser, String agentPassword) {
         this.agentUser = agentUser;
         this.agentPassword = agentPassword;
         return this;
      }

      public DiscoveryOptions build() {
         DiscoveryOptions options = new DiscoveryOptions();
         addIfPresent(options, "hypervisor", hypervisorType);
         addIfPresent(options, "ip", ip);
         addIfPresent(options, "port", port);
         addIfPresent(options, "user", user);
         addIfPresent(options, "password", password);
         addIfPresent(options, "managerip", managerIp);
         addIfPresent(options, "managerport", managerPort);
         addIfPresent(options, "manageruser", managerUser);
         addIfPresent(options, "managerpassword", managerPassword);
         addIfPresent(options, "agentip", agentIp);
         addIfPresent(options, "agentport", agentPort);
         addIfPresent(options, "agentuser", agentUser);
         addIfPresent(options, "agentpassword", agentPassword);
         return options;
      }

      private void addIfPresent(DiscoveryOptions options, String name, Object value) {
         if (value != null) {
            options.queryParameters.put(name, String.valueOf(value));
         }
      }
   }
}
