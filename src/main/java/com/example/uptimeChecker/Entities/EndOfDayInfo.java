package com.example.uptimeChecker.Entities;

import com.example.uptimeChecker.Enums.ProcessDayStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "end_of_day_info")
public class EndOfDayInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ed_id")
    private BigInteger id;

    @NotNull
    @Column(name = "ed_precessed_date")
    private Date processedDate;

    @NotNull
    @Column(name = "ed_status")
    private ProcessDayStatus status;

    @Column(name = "ed_backup_file_name")
    private String backupFileName;

    public EndOfDayInfo(Date processedDate, ProcessDayStatus status, String backupFileName) {
        this.processedDate=processedDate;
        this.status=status;
        this.backupFileName=backupFileName;
    }
}
