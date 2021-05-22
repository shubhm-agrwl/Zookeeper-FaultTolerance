package com.shubham.zookeeper.implementation.core;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import com.shubham.zookeeper.implemenation.conf.ZookeeperServConf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessNode {

  private String leaderElectionRootNode;
  private String processNodePrefix;
  private String id;
  private ZooKeeperService zooKeeperService;
  private String rootNodePath;
  private String processNodePath;
  private String watchedNodePath;
  private String zookeeperServers;
  private Boolean isLeader = false;
  private int sessionTimeout;

  public ProcessNode(ZookeeperServConf config) throws IOException {
    this.leaderElectionRootNode = "/" + config.getZookeeperConfig().getRootNode().trim();
    this.processNodePrefix = "/" + config.getZookeeperConfig().getProcessNodePrefix().trim() + "_";
    this.id = config.getZookeeperServerId().trim();
    this.zookeeperServers = config.getZookeeperConfig().getZookeeperServers();
    this.sessionTimeout = config.getZookeeperConfig().getSessionTimeout();
    zooKeeperService =
        new ZooKeeperService(zookeeperServers, sessionTimeout, new ProcessNodeWatcher());

    log.info("Process Node for Zookeeper Server: " + id + " has started!");

    rootNodePath = zooKeeperService.createNode(leaderElectionRootNode, false, false);
    if (rootNodePath == null) {
      log.error("fatal" + "Unable to create/access leader election root node with path: "
          + leaderElectionRootNode);
      System.exit(-1);
    }
    createProcessNode(rootNodePath);

  }

  private void createProcessNode(String rootNodePath) {
    processNodePath = zooKeeperService.createNode(rootNodePath + processNodePrefix, false, true);
    if (processNodePath == null) {
      throw new IllegalStateException(
          "Unable to create/access process node with path: " + leaderElectionRootNode);
    }

    if (log.isDebugEnabled()) {
      log.debug("[Process: " + id + "] Process node created with path: " + processNodePath);
    }

    attemptForLeaderPosition();
  }

  private void attemptForLeaderPosition() {

    final List<String> childNodePaths = zooKeeperService.getChildren(leaderElectionRootNode, false);

    Collections.sort(childNodePaths);

    // finding the position of the current processNode
    int index =
        childNodePaths.indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));

    // if the current processNode is first, making the leader
    if (index == 0) {
      log.info("[Zookeeper Server: " + id + "] is the new leader!");
      isLeader = true;
      toggleSchedulers(isLeader);

    } else {

      final String watchedNodeShortPath = childNodePaths.get(index - 1);
      watchedNodePath = leaderElectionRootNode + "/" + watchedNodeShortPath;
      log.info(
          "[Zookeeper Server: " + id + "] - Setting watch on node with path: " + watchedNodePath);
      zooKeeperService.watchNode(watchedNodePath, true);
    }
  }

  private void toggleSchedulers(Boolean isEnabled) {
    log.info("Toggling Schedulers - " + isEnabled);
  }

  public class ProcessNodeWatcher implements Watcher {

    public void process(WatchedEvent event) {
      if (log.isDebugEnabled()) {
        log.debug("[Process: " + id + "] Event Type: " + event.getType() + ", Keeper State: "
            + event.getState());
      }

      final EventType eventType = event.getType();
      final KeeperState keeperState = event.getState();

      switch (keeperState) {

        case SyncConnected:
          if (EventType.NodeDeleted.equals(eventType)) {
            if (event.getPath().equalsIgnoreCase(watchedNodePath)) {
              attemptForLeaderPosition();
            }
          }
          toggleSchedulers(isLeader);
          break;

        case Disconnected:
          log.error("Disconnected from ZooKeeper");
          toggleSchedulers(false);
          break;

        case Expired:
          isLeader = false;
          log.info("Re-established connection but Expired. Creating new ZooKeeper session.");
          try {
            zooKeeperService =
                new ZooKeeperService(zookeeperServers, sessionTimeout, new ProcessNodeWatcher());
            createProcessNode(rootNodePath);
          } catch (IOException e) {
            log.error("Unable to connect to ZooKeeper");
          }
          break;

        default:
          log.warn("State: " + keeperState + "not handled, continuing...");
          break;
      }

    }

  }

}
