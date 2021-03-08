package com.nebbo.registry.zookeeper;

import com.nebbo.common.tools.URIUtils;
import com.nebbo.registry.NotifyListener;
import com.nebbo.registry.RegistryService;
import com.nebbo.registry.zookeeper.serialize.ZkSerialization;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class ZookeeperRegistry implements RegistryService {
    URI address;
    ZkClient zkClient;
    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);

    // 服务消费者相关
    Map<String, Set<URI>> localCache = new ConcurrentHashMap<>();
    Map<String, NotifyListener> listenerMap = new ConcurrentHashMap<>();
    // 父节点 /nebbo/com.study.dubbo.sms.api.SmsService/privoders
    // 子节点 NebboProtocol%3A%2F%2F127.0.0.1%3A8065%2Fcom.study.dubbo.sms.api.SmsService%3Ftransporter%3DNetty4Transporter%26serialization%3DJsonSerialization

    @Override
    public void register(URI uri) throws UnsupportedEncodingException {
        String parentNode = "/nebbo" + uri.getPath() + "/privoders";
        if(!zkClient.exists(parentNode)){
            System.out.println("创建node:"+parentNode);
            zkClient.createPersistent(parentNode, true);
        }
        String encodeUri = null;
        encodeUri = URLEncoder.encode(uri.toString(), "UTF-8");
        String tempNode = parentNode + "/" +  encodeUri;
        if(zkClient.exists(tempNode)){
            zkClient.delete(tempNode);
        }
        zkClient.createEphemeral(tempNode, String.valueOf(System.currentTimeMillis()));
        zkListener(parentNode, false);

    }

    @Override
    public void subscribe(String service, NotifyListener notifyListener) {
        try {
            String parentNode = "/nebbo/" + service + "/privoders";
            if (localCache.get(service) == null) {
                System.out.println("第一次获取服务列表");
                localCache.putIfAbsent(service, new HashSet<>());
                listenerMap.putIfAbsent(service, notifyListener);
                // 第一次直接获取

                List<String> serviceInstances = zkClient.getChildren(parentNode);
                for (String instances : serviceInstances) {
                    URI instanceUri = new URI(URLDecoder.decode(instances, "UTF-8"));
                    localCache.get(service).add(instanceUri);
                }
                notifyListener.notify(localCache.get(service));
            }
            zkListener(parentNode, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(URI address) {
        this.address = address;
        zkClient = new ZkClient(address.getHost()+ ":"+address.getPort());
        zkClient.setZkSerializer(new ZkSerialization());

    }

    private void zkListener(String parentNode, Boolean refresh){
        // 监听服务变动, zookeeper中的临时节点

        try {
            zkClient.subscribeChildChanges(parentNode, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    System.out.println("服务实例有变化，开始刷新");
                    String serviceName = parentPath.split("/")[2];
                    if(refresh){
                        Set<URI> uris = new HashSet<>();
                        for(String s:currentChilds){
                            String decodeUri = URLDecoder.decode(s, "UTF-8");
                            System.out.println("最新服务:"+decodeUri);
                            URI serviceURI = new URI(decodeUri);
                            uris.add(serviceURI);
                        }
                        localCache.put(serviceName, uris);

                        NotifyListener notifyListener = listenerMap.get(serviceName);
                        if (notifyListener != null) {
                            notifyListener.notify(localCache.get(serviceName));
                        }
                    }
                    else{
                        for(String s:currentChilds){
                            String decodeUri = URLDecoder.decode(s, "UTF-8");
                            System.out.println("最新服务:"+decodeUri);
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
