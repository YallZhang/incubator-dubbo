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
package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.demo.ZynService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Consumer {
    protected static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"META-INF/spring/dubbo-demo-consumer.xml"});
        context.start();
        System.out.println("===============consumer端的spring容器加载完毕");
        System.out.println();
        System.out.println("===============开始context.getBean()的方式进行服务引用 :获取远程服务Invoker的代理");
        ZynService zynService = (ZynService) context.getBean("zynService");
        System.out.println("===============服务引用结束。 zynService代理对象的类型:" + zynService.getClass().getSimpleName());
        System.out.println();

//        while (true) {
        try {
            Thread.sleep(1000);
            String food = zynService.makeMeiShi("青虾");
            System.out.println(food);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
//        }
    }
}
