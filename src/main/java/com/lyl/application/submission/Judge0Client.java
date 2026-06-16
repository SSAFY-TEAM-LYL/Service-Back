package com.lyl.application.submission;

import java.util.List;

public interface Judge0Client {

    List<Judge0SubmissionToken> submitBatch(List<Judge0SubmissionRequest> requests);

    List<Judge0SubmissionResult> fetchBatchResults(List<String> tokens);
}
