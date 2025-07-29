package com.oncf.oncf.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "train_data")
public class TrainData {
    @Id
    private String id; // Date-based ID: YYYYMMDD format

    @Column(name = "uploaded_by")
    private String uploadedBy; // Email of the engineer who uploaded

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "original_file_name")
    private String originalFileName; // Original name of uploaded file

    @Column(name = "file_path")
    private String filePath; // Path where the file is stored locally

    @Column(name = "file_size")
    private Long fileSize; // Size of the file in bytes

    @Column(name = "row_count")
    private Integer rowCount; // Number of data rows in the Excel file

    @Column(name = "column_count")
    private Integer columnCount; // Number of columns in the Excel file

    // Constructors
    public TrainData() {}

    public TrainData(String uploadedBy, String originalFileName, String filePath, Long fileSize, Integer rowCount, Integer columnCount) {
        this.uploadDate = LocalDateTime.now();
        this.id = this.uploadDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        this.uploadedBy = uploadedBy;
        this.originalFileName = originalFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }
} 