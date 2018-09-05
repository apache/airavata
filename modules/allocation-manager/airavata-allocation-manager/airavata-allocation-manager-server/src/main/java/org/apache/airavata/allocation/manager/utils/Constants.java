/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.airavata.allocation.manager.utils;

import org.apache.airavata.common.utils.DBEventService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author madrinathapa
 */
public class Constants {
  /**
     * List of publishers in which allocation manager service is interested.
     * Add publishers as required
     */
    public static final List<String> PUBLISHERS = new ArrayList<String>(){{add(DBEventService.USER_PROFILE.toString());};};
}
