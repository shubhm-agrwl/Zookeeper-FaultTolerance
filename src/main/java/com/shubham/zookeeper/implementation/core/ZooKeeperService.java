package com.shubham.zookeeper.implementation.core;

import java.io.IOException;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import com.shubham.zookeeper.implementation.core.ProcessNode.ProcessNodeWatcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooKeeperService {

  private ZooKeeper zooKeeper;

  public ZooKeeperService(String zkURL, int sessionTimeout, ProcessNodeWatcher processNodeWatcher)
      throws IOException {
    log.info("Creating a new Zookeeper Session");
    zooKeeper = new ZooKeeper(zkURL, sessionTimeout, processNodeWatcher);
  }

  public String createNode(final String node, final boolean watch, final boolean ephimeral) {
    log.info("Creating Node - " + node);
    String createdNodePath = null;
    try {

      final Stat nodeStat = zooKeeper.exists(node, watch);

      if (nodeStat == null) {
        createdNodePath = zooKeeper.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE,
            (ephimeral ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
      } else {
        createdNodePath = node;
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return createdNodePath;
  }

  public boolean watchNode(final String node, final boolean watch) {

    boolean watched = false;
    try {
      final Stat nodeStat = zooKeeper.exists(node, watch);

      if (nodeStat != null) {
        watched = true;
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return watched;
  }

  public List<String> getChildren(final String node, final boolean watch) {

    List<String> childNodes = null;

    try {
      childNodes = zooKeeper.getChildren(node, watch);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return childNodes;
  }

}
