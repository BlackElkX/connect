/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.HashMap;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.util.SqlConfig;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public class DefaultDebugUsageController extends DebugUsageController {

    private Logger logger = Logger.getLogger(this.getClass());

    private DebugUsage debugUsage;

    // singleton pattern
    private static DebugUsageController instance = null;

    public DefaultDebugUsageController() {

    }

    public static DebugUsageController create() {
        synchronized (DefaultDebugUsageController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(DebugUsageController.class);
                if (instance == null) {
                    instance = new DefaultDebugUsageController();
                    ((DefaultDebugUsageController) instance).initialize();
                } else {
                    try {
                        instance.getClass().getMethod("initialize").invoke(instance);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultDebugUsageController.class).error("Error calling initialize method in DefaultConfigurationController", e);
                    }
                }
            }
            return instance;
        }
    }

    public void initialize() {

    }


    public HashMap<String, Object> getDebugUsageMap(DebugUsage debugUsage) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("serverId", debugUsage.getServerId());
        map.put("debugInvocationCount", debugUsage.getInvocationCount());
        map.put("postprocessorCount", debugUsage.getPostprocessorCount());
        map.put("preprocessorCount", debugUsage.getPostprocessorCount());
        map.put("deployCount", debugUsage.getDeployCount());
        map.put("undeployCount", debugUsage.getUndeployCount());
        map.put("lastSent", debugUsage.getLastSent());
        return map;
    }


    public synchronized void insertOrUpdatePersistedDebugUsageStats(DebugUsage debugUsage) throws ControllerException {

//            StatementLock.getInstance(VACUUM_LOCK_PERSON_STATEMENT_ID).readLock();
        try {

            DebugUsage persistedDebugUsage = getDebugUsage(debugUsage.getServerId());

            //if server id exists, update 
            if (persistedDebugUsage.getId() != null) {
                SqlConfig.getInstance().getSqlSessionManager().insert("DebugUsage.updateDebugUsageStatistics", getDebugUsageMap(debugUsage));
                
            //otherwise, insert 
            } else {
                logger.debug("inserting debug usage statistics for serverId" + debugUsage.getServerId());
                SqlConfig.getInstance().getSqlSessionManager().insert("DebugUsage.insertDebugUsageStatistics", getDebugUsageMap(debugUsage));

            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
//                StatementLock.getInstance(VACUUM_LOCK_PERSON_STATEMENT_ID).readUnlock();
        }
    }

    public DebugUsage getDebugUsage(String serverId) throws ControllerException {
        
        logger.debug("getting debug usage for serverId: " + serverId);

        if (serverId == null) {
            throw new ControllerException("Error getting usage for serverId: serverId cannot be null.");
        }

//        StatementLock.getInstance(VACUUM_LOCK_PERSON_STATEMENT_ID).readLock();
        try {
            DebugUsage debugUsage = new DebugUsage();
            debugUsage.setServerId(serverId);

            return SqlConfig.getInstance().getReadOnlySqlSessionManager().selectOne("DebugUsage.getDebugUsage", debugUsage);
            
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
//            StatementLock.getInstance(VACUUM_LOCK_PERSON_STATEMENT_ID).readUnlock();
        }
    }



}
