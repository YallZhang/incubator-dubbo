package org.apache.dubbo.demo.provider;

import org.apache.dubbo.demo.ZynService;
import org.apache.dubbo.rpc.RpcContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author yananz_1@tujia.com
 * @Date 2020-03-15 19:22
 * @Description
 */
public class ZynServiceImpl implements ZynService {
    @Override
    public String makeMeiShi(String foodName) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] 做美食 " + foodName + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        return "做美食： " + foodName + ", response from provider: " + RpcContext.getContext().getLocalAddress();

    }

    @Override
    public String makeBook(String bookName) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] 出书 " + bookName + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        return "出书： " + bookName + ", response from provider: " + RpcContext.getContext().getLocalAddress();

    }
}
