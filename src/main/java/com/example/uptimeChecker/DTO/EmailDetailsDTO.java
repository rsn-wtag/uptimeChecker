package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Annotations
@Data
@AllArgsConstructor
@NoArgsConstructor

// Class
public class EmailDetailsDTO {

    // Class data members
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
}
