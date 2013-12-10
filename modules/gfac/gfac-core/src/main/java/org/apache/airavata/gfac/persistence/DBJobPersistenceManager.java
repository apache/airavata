/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.gfac.persistence;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.gfac.GFacException;
import org.apache.log4j.Logger;
import org.globus.gram.internal.GRAMConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 6/18/13
 * Time: 4:16 PM
 * Database based job persistence manager. Current default implementation.
 */

public class DBJobPersistenceManager implements JobPersistenceManager {

    private DBUtil dbUtil;

    private static final Logger log = Logger.getLogger(DBJobPersistenceManager.class);


    public DBJobPersistenceManager(DBUtil db) {
        this.dbUtil = db;
    }

    public synchronized void updateJobStatus(JobData jobData) throws GFacException {

        if (jobData.getState() == GRAMConstants.STATUS_UNSUBMITTED) {
            insertJob(jobData);
        } else {

            String sql = "update gram_job set status = ? where job_id = ?";

            Connection connection = null;
            PreparedStatement stmt = null;

            try {
                connection = getConnection();
                stmt = connection.prepareStatement(sql);
                stmt.setInt(1, jobData.getState());
                stmt.setString(2, jobData.getJobId());

                stmt.executeUpdate();
                connection.commit();

            } catch (SQLException e) {
                throw new GFacException(e);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }

                    if (connection != null) {
                        connection.close();
                    }

                } catch (SQLException e) {
                    log.error("Error closing streams", e);
                }
            }
        }
    }

    private void insertJob(JobData jobData) throws GFacException {

        String sql = "insert into gram_job values (?, ?)";

        PreparedStatement stmt = null;
        Connection connection = null;

        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, jobData.getJobId());
            stmt.setInt(2, jobData.getState());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GFacException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }

                if (connection != null) {
                    connection.close();
                }

            } catch (SQLException e) {
                log.error("Error closing streams", e);
            }
        }

    }

    public List<JobData> getRunningJobs() throws GFacException {

        String sql = "select * from gram_job where status not in (?, ?, ?)";

        int[] statuses = new int[3];
        statuses[0] = GRAMConstants.STATUS_UNSUBMITTED;
        statuses[1] = GRAMConstants.STATUS_DONE;
        statuses[2] = GRAMConstants.STATUS_FAILED;

        return getJobs(sql, statuses);
    }

    public List<JobData> getFailedJobs() throws GFacException {

        String sql = "select * from gram_job where status in (?)";

        int[] statuses = new int[1];
        statuses[0] = GRAMConstants.STATUS_FAILED;

        return getJobs(sql, statuses);
    }

    public List<JobData> getUnSubmittedJobs() throws GFacException {

        String sql = "select * from gram_job where status in (?)";

        int[] statuses = new int[1];
        statuses[0] = GRAMConstants.STATUS_UNSUBMITTED;

        return getJobs(sql, statuses);
    }

    public List<JobData> getSuccessfullyCompletedJobs() throws GFacException {

        String sql = "select * from gram_job where status in (?)";

        int[] statuses = new int[1];
        statuses[0] = GRAMConstants.STATUS_DONE;

        return getJobs(sql, statuses);

    }


    protected List<JobData> getJobs(String sql, int[] statuses) throws GFacException {

        List<JobData> jobs = new ArrayList<JobData>();

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);

            int index = 1;
            for (int status : statuses) {
                preparedStatement.setInt(index, status);
                ++index;
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String jobId = resultSet.getString("job_id");
                int state = resultSet.getInt("status");

                jobs.add(new JobData(jobId, state));
            }

        } catch (SQLException e) {
            throw new GFacException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }

                if (connection != null) {
                    connection.close();
                }

            } catch (SQLException e) {
                log.error("Error closing connection", e);
            }
        }

        return jobs;
    }

    private synchronized Connection getConnection() throws SQLException {
        Connection connection = dbUtil.getConnection();
        connection.setAutoCommit(true);

        return connection;
    }
}
