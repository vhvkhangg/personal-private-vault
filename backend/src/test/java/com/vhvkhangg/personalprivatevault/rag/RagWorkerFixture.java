package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import java.sql.*;
import java.util.*;

/** Controlled synthetic output traverses the actual supervised Java/HTTP/Python/SQL boundary. */
final class RagWorkerFixture {
    static Connection connection() throws SQLException {
        AbstractEnabledRagIntegrationTest.ensureWorkerCredential();
        return AbstractEnabledRagIntegrationTest.workerConnection();
    }
    static void stage(RagOperations rag,RagJobAdmissionResult attempt) {
        stage(rag,attempt.jobId(),attempt.attemptId());
    }
    static void stage(RagOperations rag,UUID jobId,UUID attemptId) {
        rag.executeSyntheticStage(jobId,attemptId,"SYNTHETIC_TEST");
    }
}
