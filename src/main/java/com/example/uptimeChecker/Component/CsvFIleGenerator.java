package com.example.uptimeChecker.Component;

import com.example.uptimeChecker.Entities.Downtime;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class CsvFIleGenerator {
    @Value("${downtime.file.name}")
    private String endOfDayDowntimeDatafileName;
    @Value("${downtime.file.path}")
    private String endOfDayDowntimeDatafilePath;
    @Value("${downtime.file.headers}")
    private String[] endOfDayDowntimeDatafileHeaders;
    public String writeDowntimeInfoToCSV(List<Downtime> downtimeSet, Date date) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fileName=endOfDayDowntimeDatafileName +sdf.format(date)+".csv";
        File tempFile = new File(endOfDayDowntimeDatafilePath+fileName);

        if(!tempFile.exists()) {

            Writer writer = new FileWriter(endOfDayDowntimeDatafilePath + fileName);
            try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(endOfDayDowntimeDatafileHeaders))) {
                downtimeSet.forEach(downtime -> {
                    try {
                        printer.printRecord(downtime.getDownTimeId(), downtime.getWebId(), downtime.getStartTime(), downtime.getEndTime(),
                                downtime.getDate(), downtime.getTotalFailCount());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return fileName;
    }
}
