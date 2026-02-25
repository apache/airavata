/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.compute.resource.submission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.compute.provider.local.LocalOutputParser;
import org.apache.airavata.compute.provider.slurm.SlurmOutputParser;
import org.apache.airavata.core.model.StatusModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pure unit tests for job output parsing logic.
 *
 * <p>Covers:
 * <ul>
 *   <li>{@link JobStateParser} — status-code-to-enum mapping for PBS, SGE, LSF, SLURM, and unknown codes</li>
 *   <li>{@link SlurmOutputParser} — sbatch submission output, squeue status output, and job-ID lookup</li>
 *   <li>{@link LocalOutputParser} — in-process fork job ID generation and no-op status methods</li>
 * </ul>
 *
 * <p>No Spring context is loaded; all classes under test are stateless or use only JDK facilities.
 */
@DisplayName("Job Parsing Tests")
class JobParsingTest {

    // -------------------------------------------------------------------------
    // JobStateParser
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("JobStateParser")
    class JobStateParserTests {

        // -- COMPLETED --------------------------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to COMPLETED")
        @CsvSource({"C", "CD", "E", "CG", "DONE"})
        @DisplayName("Completed status codes")
        void completedCodes(String status) {
            assertEquals(JobState.COMPLETED, JobStateParser.getJobState(status),
                    "Expected COMPLETED for status code: " + status);
        }

        // -- QUEUED -----------------------------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to QUEUED")
        @CsvSource({"Q", "qw", "PEND", "W", "PD", "I"})
        @DisplayName("Queued status codes")
        void queuedCodes(String status) {
            assertEquals(JobState.QUEUED, JobStateParser.getJobState(status),
                    "Expected QUEUED for status code: " + status);
        }

        // -- ACTIVE -----------------------------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to ACTIVE")
        @CsvSource({"R", "CF", "r", "RUN"})
        @DisplayName("Active (running) status codes")
        void activeCodes(String status) {
            assertEquals(JobState.ACTIVE, JobStateParser.getJobState(status),
                    "Expected ACTIVE for status code: " + status);
        }

        // -- SUSPENDED --------------------------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to SUSPENDED")
        @CsvSource({"S", "PSUSP", "USUSP", "SSUSP"})
        @DisplayName("Suspended status codes")
        void suspendedCodes(String status) {
            assertEquals(JobState.SUSPENDED, JobStateParser.getJobState(status),
                    "Expected SUSPENDED for status code: " + status);
        }

        // -- CANCELED ---------------------------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to CANCELED")
        @CsvSource({"CA", "X"})
        @DisplayName("Canceled status codes")
        void canceledCodes(String status) {
            assertEquals(JobState.CANCELED, JobStateParser.getJobState(status),
                    "Expected CANCELED for status code: " + status);
        }

        // -- FAILED -----------------------------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to FAILED")
        @CsvSource({"F", "NF", "TO", "EXIT", "PR", "Er"})
        @DisplayName("Failed status codes")
        void failedCodes(String status) {
            assertEquals(JobState.FAILED, JobStateParser.getJobState(status),
                    "Expected FAILED for status code: " + status);
        }

        // -- UNKNOWN (explicit codes) -----------------------------------------

        @ParameterizedTest(name = "status ''{0}'' maps to UNKNOWN")
        @CsvSource({"U", "UNKWN"})
        @DisplayName("Explicit unknown status codes")
        void unknownCodes(String status) {
            assertEquals(JobState.UNKNOWN, JobStateParser.getJobState(status),
                    "Expected UNKNOWN for status code: " + status);
        }

        // -- UNKNOWN (null / blank / unrecognized) ----------------------------

        @Test
        @DisplayName("null input returns UNKNOWN")
        void nullInputReturnsUnknown() {
            assertEquals(JobState.UNKNOWN, JobStateParser.getJobState(null));
        }

        @ParameterizedTest(name = "unrecognized status ''{0}'' returns UNKNOWN")
        @ValueSource(strings = {"GARBAGE", "XYZ", "running", "complete", "  ", "cd", "done", "ca"})
        @DisplayName("Unrecognized or wrong-case status codes return UNKNOWN")
        void unrecognizedStatusReturnsUnknown(String status) {
            assertEquals(JobState.UNKNOWN, JobStateParser.getJobState(status),
                    "Expected UNKNOWN for unrecognized status: " + status);
        }

        @Test
        @DisplayName("Empty string returns UNKNOWN")
        void emptyStringReturnsUnknown() {
            assertEquals(JobState.UNKNOWN, JobStateParser.getJobState(""));
        }

        // -- Case sensitivity sanity check ------------------------------------

        @Test
        @DisplayName("Lowercase 'r' maps to ACTIVE but uppercase 'R' also maps to ACTIVE")
        void bothUpperAndLowerRMapToActive() {
            assertEquals(JobState.ACTIVE, JobStateParser.getJobState("r"));
            assertEquals(JobState.ACTIVE, JobStateParser.getJobState("R"));
        }

        @Test
        @DisplayName("Lowercase 'qw' maps to QUEUED (SGE-style code)")
        void sgeQueuedCode() {
            assertEquals(JobState.QUEUED, JobStateParser.getJobState("qw"));
        }
    }

    // -------------------------------------------------------------------------
    // SlurmOutputParser
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("SlurmOutputParser")
    class SlurmOutputParserTests {

        private SlurmOutputParser parser;

        @BeforeEach
        void setUp() {
            parser = new SlurmOutputParser();
        }

        // -- parseJobSubmission -----------------------------------------------

        @Test
        @DisplayName("parseJobSubmission extracts numeric job ID from standard sbatch output")
        void parseJobSubmission_validOutput_returnsJobId() throws Exception {
            String rawOutput = "Submitted batch job 12345";
            assertEquals("12345", parser.parseJobSubmission(rawOutput));
        }

        @Test
        @DisplayName("parseJobSubmission extracts job ID with surrounding whitespace/newlines")
        void parseJobSubmission_outputWithNewlines_returnsJobId() throws Exception {
            String rawOutput = "\nSubmitted batch job 98765\n";
            assertEquals("98765", parser.parseJobSubmission(rawOutput));
        }

        @Test
        @DisplayName("parseJobSubmission returns empty string when pattern is absent")
        void parseJobSubmission_noMatch_returnsEmptyString() throws Exception {
            String rawOutput = "Some other sbatch output without the expected line";
            assertEquals("", parser.parseJobSubmission(rawOutput));
        }

        @Test
        @DisplayName("parseJobSubmission returns empty string for empty input")
        void parseJobSubmission_emptyInput_returnsEmptyString() throws Exception {
            assertEquals("", parser.parseJobSubmission(""));
        }

        @Test
        @DisplayName("parseJobSubmission extracts non-numeric job ID (edge case)")
        void parseJobSubmission_nonNumericJobId_returnsToken() throws Exception {
            // SLURM can theoretically return non-numeric IDs on some cluster flavours
            String rawOutput = "Submitted batch job abc-123";
            assertEquals("abc-123", parser.parseJobSubmission(rawOutput));
        }

        // -- isJobSubmissionFailed --------------------------------------------

        @Test
        @DisplayName("isJobSubmissionFailed returns true when output contains FAILED")
        void isJobSubmissionFailed_containsFailed_returnsTrue() {
            assertTrue(parser.isJobSubmissionFailed("sbatch: error: FAILED to submit batch job"));
        }

        @Test
        @DisplayName("isJobSubmissionFailed returns false when output does not contain FAILED")
        void isJobSubmissionFailed_doesNotContainFailed_returnsFalse() {
            assertFalse(parser.isJobSubmissionFailed("Submitted batch job 12345"));
        }

        @Test
        @DisplayName("isJobSubmissionFailed returns false for empty output")
        void isJobSubmissionFailed_emptyOutput_returnsFalse() {
            assertFalse(parser.isJobSubmissionFailed(""));
        }

        @Test
        @DisplayName("isJobSubmissionFailed is case-sensitive; 'failed' does not trigger true")
        void isJobSubmissionFailed_lowercaseFailed_returnsFalse() {
            assertFalse(parser.isJobSubmissionFailed("job submission failed due to resource limit"));
        }

        // -- parseJobStatus ---------------------------------------------------

        /**
         * Simulates a minimal squeue output line:
         *   JOBID PARTITION  NAME  USER ST TIME  NODES NODELIST
         *   12345 general    myjob usr  R  0:05  1     node01
         *
         * The parser pattern anchors on jobID followed by four non-whitespace tokens and then the status token.
         */
        @Test
        @DisplayName("parseJobStatus returns ACTIVE for status R in squeue output")
        void parseJobStatus_runningJob_returnsActive() throws Exception {
            String jobId = "12345";
            String rawOutput = "JOBID PARTITION NAME USER ST TIME NODES\n"
                    + "12345 general myjob usr R 0:05 1\n";
            StatusModel<JobState> result = parser.parseJobStatus(jobId, rawOutput);
            assertNotNull(result, "Result must not be null when the job ID is found");
            assertEquals(JobState.ACTIVE, result.getState());
        }

        @Test
        @DisplayName("parseJobStatus returns QUEUED for status PD in squeue output")
        void parseJobStatus_pendingJob_returnsQueued() throws Exception {
            String jobId = "55555";
            String rawOutput = "55555 debug myjob usr PD 0:00 1\n";
            StatusModel<JobState> result = parser.parseJobStatus(jobId, rawOutput);
            assertNotNull(result);
            assertEquals(JobState.QUEUED, result.getState());
        }

        @Test
        @DisplayName("parseJobStatus returns COMPLETED for status CD in squeue output")
        void parseJobStatus_completedJob_returnsCompleted() throws Exception {
            String jobId = "77777";
            String rawOutput = "77777 compute myjob usr CD 1:30 1\n";
            StatusModel<JobState> result = parser.parseJobStatus(jobId, rawOutput);
            assertNotNull(result);
            assertEquals(JobState.COMPLETED, result.getState());
        }

        @Test
        @DisplayName("parseJobStatus returns FAILED for status F in squeue output")
        void parseJobStatus_failedJob_returnsFailed() throws Exception {
            String jobId = "99999";
            String rawOutput = "99999 compute myjob usr F 0:01 1\n";
            StatusModel<JobState> result = parser.parseJobStatus(jobId, rawOutput);
            assertNotNull(result);
            assertEquals(JobState.FAILED, result.getState());
        }

        @Test
        @DisplayName("parseJobStatus returns CANCELED for status CA in squeue output")
        void parseJobStatus_canceledJob_returnsCanceled() throws Exception {
            String jobId = "11111";
            String rawOutput = "11111 compute myjob usr CA 0:00 1\n";
            StatusModel<JobState> result = parser.parseJobStatus(jobId, rawOutput);
            assertNotNull(result);
            assertEquals(JobState.CANCELED, result.getState());
        }

        @Test
        @DisplayName("parseJobStatus returns null when job ID is not present in output")
        void parseJobStatus_jobIdNotFound_returnsNull() throws Exception {
            String rawOutput = "99999 compute myjob usr R 0:05 1\n";
            assertNull(parser.parseJobStatus("00001", rawOutput));
        }

        @Test
        @DisplayName("parseJobStatus returns null for empty output")
        void parseJobStatus_emptyOutput_returnsNull() throws Exception {
            assertNull(parser.parseJobStatus("12345", ""));
        }

        @Test
        @DisplayName("parseJobStatus with multiple jobs returns status for the correct job")
        void parseJobStatus_multipleJobs_returnsCorrectStatus() throws Exception {
            String rawOutput = "11111 compute alpha usr R  0:01 1\n"
                    + "22222 compute beta  usr PD 0:00 1\n"
                    + "33333 compute gamma usr CD 2:10 1\n";
            StatusModel<JobState> result = parser.parseJobStatus("22222", rawOutput);
            assertNotNull(result);
            assertEquals(JobState.QUEUED, result.getState());
        }

        // -- parseJobId -------------------------------------------------------

        /**
         * Simulates output typical of "squeue -u user":
         *   JOBID PARTITION  NAME     USER
         *   54321 general    myjobnam user
         */
        @Test
        @DisplayName("parseJobId finds job ID by exact job name in squeue listing")
        void parseJobId_exactJobName_returnsJobId() throws Exception {
            String rawOutput = "JOBID PARTITION NAME     USER\n"
                    + "54321 general   testjob  user\n";
            String result = parser.parseJobId("testjob", rawOutput);
            assertEquals("54321", result);
        }

        @Test
        @DisplayName("parseJobId truncates jobName to 8 chars when name is longer")
        void parseJobId_longJobName_truncatesAndMatchesFirst8Chars() throws Exception {
            // SLURM only shows the first 8 chars in the NAME column
            String rawOutput = "JOBID PARTITION NAME     USER\n"
                    + "67890 compute   abcdefgh user\n";
            // A 12-character job name that starts with "abcdefgh"
            String result = parser.parseJobId("abcdefghijkl", rawOutput);
            assertEquals("67890", result);
        }

        @Test
        @DisplayName("parseJobId returns null when jobName is null")
        void parseJobId_nullJobName_returnsNull() throws Exception {
            String rawOutput = "JOBID PARTITION NAME     USER\n"
                    + "54321 general   testjob  user\n";
            assertNull(parser.parseJobId(null, rawOutput));
        }

        @Test
        @DisplayName("parseJobId returns null when rawOutput is null")
        void parseJobId_nullRawOutput_returnsNull() throws Exception {
            assertNull(parser.parseJobId("testjob", null));
        }

        @Test
        @DisplayName("parseJobId returns null when job name is not present in output")
        void parseJobId_jobNameNotFound_returnsNull() throws Exception {
            String rawOutput = "JOBID PARTITION NAME     USER\n"
                    + "54321 general   otherjob user\n";
            assertNull(parser.parseJobId("testjob", rawOutput));
        }

        @Test
        @DisplayName("parseJobId returns null for empty rawOutput")
        void parseJobId_emptyRawOutput_returnsNull() throws Exception {
            assertNull(parser.parseJobId("testjob", ""));
        }

        @Test
        @DisplayName("parseJobId with exactly 8-char name does not truncate")
        void parseJobId_exactly8CharName_matchesWithoutTruncation() throws Exception {
            String rawOutput = "JOBID PARTITION NAME     USER\n"
                    + "13579 compute   exactly8 user\n";
            assertEquals("13579", parser.parseJobId("exactly8", rawOutput));
        }

        @Test
        @DisplayName("parseJobId with 9-char name truncates to 8 and still matches")
        void parseJobId_9CharName_truncatesTo8() throws Exception {
            String rawOutput = "JOBID PARTITION NAME     USER\n"
                    + "24680 compute   ninechrx user\n";
            // "ninechars" is 9 chars; parser truncates to "ninechrx" length 8 = "ninechrx"
            // Let us use a name whose first 8 chars match the column
            String jobName = "ninechrx9"; // 9 chars, first 8 = "ninechrx"
            assertEquals("24680", parser.parseJobId(jobName, rawOutput));
        }

        // -- JOB_NAME_OUTPUT_LENGTH constant ----------------------------------

        @Test
        @DisplayName("JOB_NAME_OUTPUT_LENGTH constant is 8")
        void jobNameOutputLengthConstant() {
            assertEquals(8, SlurmOutputParser.JOB_NAME_OUTPUT_LENGTH);
        }
    }

    // -------------------------------------------------------------------------
    // ForkOutputParser
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ForkOutputParser")
    class ForkOutputParserTests {

        private LocalOutputParser parser;

        @BeforeEach
        void setUp() {
            parser = new LocalOutputParser();
        }

        // -- parseJobSubmission -----------------------------------------------

        @Test
        @DisplayName("parseJobSubmission returns a non-null string")
        void parseJobSubmission_returnsNonNull() throws Exception {
            assertNotNull(parser.parseJobSubmission("any output"));
        }

        @Test
        @DisplayName("parseJobSubmission returns a string starting with 'JOB_ID_'")
        void parseJobSubmission_returnsPrefixedId() throws Exception {
            String id = parser.parseJobSubmission("irrelevant");
            assertTrue(id.startsWith("JOB_ID_"),
                    "Expected ID to start with 'JOB_ID_' but was: " + id);
        }

        @Test
        @DisplayName("parseJobSubmission returns different IDs on successive calls")
        void parseJobSubmission_uniqueIdsOnEachCall() throws Exception {
            String id1 = parser.parseJobSubmission("output");
            String id2 = parser.parseJobSubmission("output");
            assertFalse(id1.equals(id2),
                    "Expected unique IDs on successive calls, but both were: " + id1);
        }

        @Test
        @DisplayName("parseJobSubmission ignores rawOutput content (null-safe via IdGenerator)")
        void parseJobSubmission_nullOutputIgnored() throws Exception {
            // ForkOutputParser does not inspect rawOutput at all; still must not throw
            String id = parser.parseJobSubmission(null);
            assertNotNull(id);
            assertTrue(id.startsWith("JOB_ID_"));
        }

        // -- isJobSubmissionFailed --------------------------------------------

        @Test
        @DisplayName("isJobSubmissionFailed always returns false regardless of input")
        void isJobSubmissionFailed_alwaysFalse() {
            assertFalse(parser.isJobSubmissionFailed("FAILED"));
            assertFalse(parser.isJobSubmissionFailed("error: FAILED job submission"));
            assertFalse(parser.isJobSubmissionFailed(""));
            assertFalse(parser.isJobSubmissionFailed(null));
        }

        // -- parseJobStatus ---------------------------------------------------

        @Test
        @DisplayName("parseJobStatus always returns null")
        void parseJobStatus_alwaysNull() throws Exception {
            assertNull(parser.parseJobStatus("JOB_ID_xyz", "some output"));
            assertNull(parser.parseJobStatus(null, null));
            assertNull(parser.parseJobStatus("", ""));
        }

        // -- parseJobId -------------------------------------------------------

        @Test
        @DisplayName("parseJobId returns a non-null string")
        void parseJobId_returnsNonNull() throws Exception {
            assertNotNull(parser.parseJobId("myjob", "any output"));
        }

        @Test
        @DisplayName("parseJobId returns a string containing the job name prefix")
        void parseJobId_containsJobNamePrefix() throws Exception {
            // IdGenerator.getId(name) trims and replaces spaces/dots/slashes then appends UUID
            String id = parser.parseJobId("myjob", "irrelevant");
            assertTrue(id.startsWith("myjob_"),
                    "Expected ID to start with 'myjob_' but was: " + id);
        }

        @Test
        @DisplayName("parseJobId returns different IDs on successive calls (UUID suffix)")
        void parseJobId_uniqueIdsOnEachCall() throws Exception {
            String id1 = parser.parseJobId("job", "output");
            String id2 = parser.parseJobId("job", "output");
            assertFalse(id1.equals(id2),
                    "Expected unique IDs on successive calls, but both were: " + id1);
        }

        @Test
        @DisplayName("parseJobId normalises spaces and dots in job name via IdGenerator")
        void parseJobId_normalisesJobName() throws Exception {
            String id = parser.parseJobId("my job.name", "output");
            // IdGenerator replaces spaces, dots, slashes with underscores
            assertTrue(id.startsWith("my_job_name_"),
                    "Expected normalised prefix 'my_job_name_' but got: " + id);
        }

        @Test
        @DisplayName("parseJobId rawOutput is ignored — result depends only on jobName")
        void parseJobId_rawOutputIgnored() throws Exception {
            String idA = parser.parseJobId("testjob", "outputA");
            String idB = parser.parseJobId("testjob", "completely different output");
            // Both must start with the same prefix even though output differs
            assertTrue(idA.startsWith("testjob_"));
            assertTrue(idB.startsWith("testjob_"));
        }
    }
}
