package com.vectracore.dto;

import java.util.List;

public class BatchInsertRequest {
    private List<InsertVectorRequest> records;

    public BatchInsertRequest() {}

    public BatchInsertRequest(List<InsertVectorRequest> records) {
        this.records = records;
    }

    public List<InsertVectorRequest> getRecords() { return records; }
    public void setRecords(List<InsertVectorRequest> records) { this.records = records; }
}
