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
package org.jclouds.abiquo.domain.task;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.rest.ApiContext;

import com.abiquo.server.core.task.TaskDto;

/**
 * A task that does not produce a result.
 * 
 * @author Ignasi Barrera
 */
public class NoResultTask extends BaseTask<Void> {
   protected NoResultTask(final ApiContext<AbiquoApi> context, final TaskDto target) {
      super(context, target);
   }

   @Override
   public Void getResult() {
      return null;
   }

   @Override
   public String toString() {
      return "NoResult" + super.toString();
   }

   public static class Builder {
      private ApiContext<AbiquoApi> context;

      private TaskDto target;

      public Builder(ApiContext<AbiquoApi> context, TaskDto target) {
         this.context = checkNotNull(context, "context");
         this.target = checkNotNull(target, "target");
      }

      public NoResultTask build() {
         return new NoResultTask(context, target);
      }
   }

   public static Builder builder(ApiContext<AbiquoApi> context, TaskDto target) {
      return new Builder(context, target);
   }
}
