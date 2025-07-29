package com.oncf.oncf.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oncf.oncf.entities.TrainData;
import com.oncf.oncf.repositories.TrainDataRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsService {

    @Autowired
    private TrainDataRepository trainDataRepository;
    
    @Value("${app.file.base-path}")
    private String basePath;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process and save XLSX file locally
     */
    public String processXlsxFile(MultipartFile file, String uploadedBy) throws IOException {
        // Generate date-based ID
        String dateId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Create directory structure if it doesn't exist
        Path uploadPath = Paths.get(basePath, uploadDir);
        Files.createDirectories(uploadPath);
        
        // Generate unique filename to avoid conflicts for same-day uploads
        String fileName = dateId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file locally
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(file.getBytes());
        }
        
        // Analyze the Excel file to get metadata
        int rowCount = 0;
        int columnCount = 0;
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet.getPhysicalNumberOfRows() == 0) {
                // Clean up file and throw error
                Files.deleteIfExists(filePath);
                throw new IllegalArgumentException("Excel file is empty");
            }
            
            // Get column count from header row
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                columnCount = headerRow.getLastCellNum();
            }
            
            // Count data rows (excluding header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && hasDataInRow(row)) {
                    rowCount++;
                }
            }
        }
        
        // Save metadata to database
        TrainData trainData = new TrainData(
            uploadedBy,
            file.getOriginalFilename(),
            filePath.toString(),
            file.getSize(),
            rowCount,
            columnCount
        );
        
        // Handle same-day uploads by appending timestamp to ID
        String finalId = dateId;
        int counter = 1;
        while (trainDataRepository.existsById(finalId)) {
            finalId = dateId + "_" + counter;
            counter++;
        }
        trainData.setId(finalId);
        
        trainDataRepository.save(trainData);
        
        return finalId;
    }
    
    /**
     * Check if a row has any data
     */
    private boolean hasDataInRow(Row row) {
        for (int j = 0; j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j);
            Object value = getCellValue(cell);
            if (value != null && !value.toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get cell value as appropriate Java type
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // If it's a whole number, return as integer
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    } else {
                        return numericValue;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }
    
    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
    
    /**
     * Find the best row to use as headers by analyzing row content
     */
    private Row findHeaderRow(Sheet sheet) {
        for (int i = 0; i <= Math.min(2, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            if (isLikelyHeaderRow(row)) {
                return row;
            }
        }
        // Fallback to first row
        return sheet.getRow(0);
    }
    
    /**
     * Check if a row looks like a header row
     */
    private boolean isLikelyHeaderRow(Row row) {
        int stringCells = 0;
        int totalCells = 0;
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                totalCells++;
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty() && 
                    !isDateString(value) && !isNumericOnly(value)) {
                    stringCells++;
                }
            }
        }
        
        // A header row should have mostly text values (not dates or pure numbers)
        return totalCells > 0 && (stringCells / (double) totalCells) > 0.5;
    }
    
    /**
     * Check if a string represents a date
     */
    private boolean isDateString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        // Check for common date patterns
        return value.contains("GMT") || 
               value.contains("UTC") || 
               value.matches(".*\\d{4}.*") && (value.contains("Jan") || value.contains("Feb") || 
               value.contains("Mar") || value.contains("Apr") || value.contains("May") || 
               value.contains("Jun") || value.contains("Jul") || value.contains("Aug") || 
               value.contains("Sep") || value.contains("Oct") || value.contains("Nov") || 
               value.contains("Dec")) ||
               value.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}") ||
               value.matches("\\d{2,4}-\\d{1,2}-\\d{1,2}");
    }
    
    /**
     * Check if a string contains only numbers
     */
    private boolean isNumericOnly(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Get intelligent column name based on position for train data
     */
    private String getIntelligentColumnName(int columnIndex) {
        String[] standardColumnNames = {
            "Train", "Remorque", "Materiel", "Type_Incident", 
            "Lieu_Incident", "Observation"
        };
        
        if (columnIndex < standardColumnNames.length) {
            return standardColumnNames[columnIndex];
        }
        
        return "Column_" + (columnIndex + 1);
    }

    /**
     * Get processed data for a file by reading from stored Excel file
     */
    public Map<String, Object> getFileData(String fileId) {
        Optional<TrainData> optionalTrainData = trainDataRepository.findById(fileId);
        
        if (optionalTrainData.isEmpty()) {
            return null;
        }
        
        TrainData trainData = optionalTrainData.get();
        
        // Read data from the stored Excel file
        List<Map<String, Object>> processedData = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        
        try {
            Path filePath = Paths.get(trainData.getFilePath());
            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + trainData.getFilePath());
            }
            
            try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(filePath))) {
                Sheet sheet = workbook.getSheetAt(0);
                
                // Smart header detection - look for the best header row
                Row headerRow = findHeaderRow(sheet);
                int headerRowIndex = 0;
                
                if (headerRow != null) {
                    // Find which row this is
                    for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                        if (sheet.getRow(r) == headerRow) {
                            headerRowIndex = r;
                            break;
                        }
                    }
                    
                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                        Cell cell = headerRow.getCell(i);
                        String header = getCellValueAsString(cell);
                        if (header != null && !header.trim().isEmpty() && !isDateString(header)) {
                            headers.add(header.trim());
                        } else {
                            // Use intelligent column names based on position
                            headers.add(getIntelligentColumnName(i));
                        }
                    }
                } else {
                    // Fallback: create default headers
                    int maxCols = 6; // Assume standard train data structure
                    for (int i = 0; i < maxCols; i++) {
                        headers.add(getIntelligentColumnName(i));
                    }
                }
                
                // Process data rows (skip header row)
                for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    Map<String, Object> rowData = new HashMap<>();
                    boolean hasData = false;
                    
                    for (int j = 0; j < headers.size(); j++) {
                        String header = headers.get(j);
                        Cell cell = row.getCell(j);
                        Object value = getCellValue(cell);
                        
                        if (value != null && !value.toString().trim().isEmpty()) {
                            hasData = true;
                        }
                        
                        rowData.put(header, value);
                    }
                    
                    if (hasData) {
                        processedData.add(rowData);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage(), e);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", fileId);
        result.put("fileName", trainData.getOriginalFileName());
        result.put("uploadDate", trainData.getUploadDate());
        result.put("headers", headers);
        result.put("data", processedData);
        result.put("rowCount", processedData.size());
        result.put("fileSize", trainData.getFileSize());
        result.put("columnCount", trainData.getColumnCount());
        
        return result;
    }

    /**
     * Get all uploaded files for a user
     */
    public List<Map<String, Object>> getUserFiles(String uploadedBy) {
        List<TrainData> files = trainDataRepository.findByUploadedByOrderByUploadDateDesc(uploadedBy);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TrainData file : files) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", file.getId());
            fileInfo.put("fileName", file.getOriginalFileName());
            fileInfo.put("uploadDate", file.getUploadDate());
            fileInfo.put("rowCount", file.getRowCount());
            fileInfo.put("columnCount", file.getColumnCount());
            fileInfo.put("fileSize", file.getFileSize());
            
            result.add(fileInfo);
        }
        
        return result;
    }

    /**
     * Delete a file and its data
     */
    public void deleteFile(String fileId) {
        Optional<TrainData> optionalTrainData = trainDataRepository.findById(fileId);
        
        if (optionalTrainData.isPresent()) {
            TrainData trainData = optionalTrainData.get();
            
            // Delete the physical file
            try {
                Path filePath = Paths.get(trainData.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but continue with database deletion
                System.err.println("Error deleting file: " + e.getMessage());
            }
            
            // Delete from database
            trainDataRepository.deleteById(fileId);
        }
    }

    /**
     * Get incident analysis data - counts incidents per train from actual Excel data
     */
    public Map<String, Object> getIncidentAnalysis(String fileId) {
        Map<String, Object> fileData = getFileData(fileId);
        
        if (fileData == null) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) fileData.get("data");
        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) fileData.get("headers");
        
        // Find the Train column
        String trainColumn = findTrainColumn(headers);
        System.out.println("DEBUG: Selected train column: " + trainColumn);
        System.out.println("DEBUG: Total data rows: " + data.size());
        
        if (trainColumn == null) {
            // Fallback: if no "Train" column found, return empty result
            Map<String, Object> result = new HashMap<>();
            result.put("trainNames", new ArrayList<>());
            result.put("incidentCounts", new ArrayList<>());
            result.put("totalIncidents", 0);
            result.put("totalTrains", 0);
            result.put("error", "No suitable column found for train data. Available columns: " + String.join(", ", headers));
            return result;
        }
        
        // Map to count train incidents
        Map<String, Integer> trainIncidents = new HashMap<>();
        
        // Count each row as one incident for the train in that row
        int processedRows = 0;
        for (Map<String, Object> rowData : data) {
            Object trainValue = rowData.get(trainColumn);
            if (trainValue != null) {
                String trainId = trainValue.toString().trim();
                if (!trainId.isEmpty()) {
                    trainIncidents.put(trainId, trainIncidents.getOrDefault(trainId, 0) + 1);
                    processedRows++;
                    if (processedRows <= 5) { // Log first 5 entries for debugging
                        System.out.println("DEBUG: Found train ID: " + trainId);
                    }
                }
            }
        }
        
        System.out.println("DEBUG: Processed " + processedRows + " rows with train data");
        System.out.println("DEBUG: Found " + trainIncidents.size() + " unique trains");
        System.out.println("DEBUG: Train incidents map: " + trainIncidents);
        
        // Convert to chart format
        List<String> trainNames = new ArrayList<>();
        List<Integer> incidentCounts = new ArrayList<>();
        
        // Sort by incident count (descending)
        trainIncidents.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                trainNames.add(entry.getKey());
                incidentCounts.add(entry.getValue());
            });
        
        Map<String, Object> result = new HashMap<>();
        result.put("trainNames", trainNames);
        result.put("incidentCounts", incidentCounts);
        result.put("totalIncidents", incidentCounts.stream().mapToInt(Integer::intValue).sum());
        result.put("totalTrains", trainNames.size());
        result.put("trainColumn", trainColumn); // Include which column was used
        
        return result;
    }
    
    /**
     * Find the Train column in the headers
     */
    private String findTrainColumn(List<String> headers) {
        System.out.println("DEBUG: Looking for Train column in headers: " + headers);
        
        // Look for exact matches first
        for (String header : headers) {
            if (header != null && header.equalsIgnoreCase("Train")) {
                System.out.println("DEBUG: Found exact match for Train column: " + header);
                return header;
            }
        }
        
        // Look for partial matches
        for (String header : headers) {
            if (header != null) {
                String lowerHeader = header.toLowerCase().trim();
                if (lowerHeader.contains("train") || lowerHeader.contains("locomotive") || lowerHeader.contains("rame")) {
                    System.out.println("DEBUG: Found partial match for Train column: " + header);
                    return header;
                }
            }
        }
        
        // If no train column found, let's try the first column as it might contain train IDs
        if (!headers.isEmpty() && headers.get(0) != null) {
            System.out.println("DEBUG: No train column found, using first column: " + headers.get(0));
            return headers.get(0);
        }
        
        System.out.println("DEBUG: No suitable column found for trains");
        return null;
    }
    

} 