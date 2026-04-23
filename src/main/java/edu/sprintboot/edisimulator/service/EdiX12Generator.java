package edu.sprintboot.edisimulator.service;

import edu.sprintboot.edisimulator.model.Product;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/**
 * Generates ANSI X12 850 Purchase Order EDI documents.
 * <p>
 * ANSI X12 850 is the industry-standard electronic data interchange (EDI) format
 * for purchase orders across healthcare, retail, and manufacturing supply chains.
 * <p>
 * Format Specifications:
 * - Segments: Data units beginning with a 2-3 character identifier.
 * - Delimiters: Fields are separated by '*' and segments are terminated by '~'.
 * <p>
 * EDI X12 850 Structure:
 * ISA - Interchange Control Header (Metadata: sender, receiver, timestamps)
 * GS  - Functional Group Header
 * ST  - Transaction Set Header (Start of a specific PO)
 * BEG - Beginning Segment (PO number, type, date)
 * REF - Reference Identification
 * PER - Administrative Communications Contact
 * N1  - Name (Buyer/Seller identification)
 * PO1 - Baseline Item Data (One per item)
 * CTT - Transaction Totals
 * SE  - Transaction Set Trailer
 * GE  - Functional Group Trailer
 * IEA - Interchange Control Trailer
 * <p>
 * Implementation Note:
 * In enterprise production environments, EDI generation is typically handled by
 * robust libraries (e.g., Smooks) or commercial middleware (e.g., IBM Sterling B2B, Cleo).
 * This manual implementation is intended for educational purposes—to demonstrate
 * a fundamental understanding of the underlying format, rather than relying on
 * high-level third-party abstractions.
 */
@Service
public class EdiX12Generator {

  private static final String SENDER_ID = "HOSPITAL123    ";
  private static final String RECEIVER_ID = "PHILIPSNA      ";

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmm");
  private static final DateTimeFormatter LONG_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

  public String generate850(Product product, String poNumber, String controlNumber) {
    LocalDateTime now = LocalDateTime.now();
    String date = now.format(DATE_FMT);
    String time = now.format(TIME_FMT);
    String longDate = now.format(LONG_DATE_FMT);

    StringBuilder edi = new StringBuilder();

    edi.append("ISA*00*          *00*          *ZZ*")
        .append(SENDER_ID).append("*ZZ*").append(RECEIVER_ID)
        .append("*").append(date)
        .append("*").append(time)
        .append("*U*00401*").append(controlNumber)
        .append("*0*T*>~\n");

    edi.append("GS*PO*").append(SENDER_ID.trim())
        .append("*").append(RECEIVER_ID.trim())
        .append("*").append(longDate)
        .append("*").append(time)
        .append("*1*X*004010~\n");

    edi.append("ST*850*0001~\n");


    edi.append("BEG*00*NE*").append(poNumber)
        .append("**").append(longDate).append("~\n");

    edi.append("REF*DP*HOSPITAL-PROCUREMENT~\n");

    edi.append("PER*BD*Procurement Department*TE*5551234567~\n");

    edi.append("N1*BY*GENERAL HOSPITAL*92*HOSP-001~\n");

    edi.append("N1*SE*PHILIPS HEALTHCARE NA*92*PHILIPS-NA~\n");

    edi.append("PO1*1*").append(product.getReorderQuantity())
        .append("*EA*").append(product.getUnitPrice())
        .append("*PE*VP*").append(product.getProductId()).append("~\n");

    edi.append("PID*F****").append(product.getProductName()).append("~\n");


    edi.append("CTT*1~\n");


    edi.append("SE*10*0001~\n");


    edi.append("GE*1*1~\n");


    edi.append("IEA*1*").append(controlNumber).append("~\n");

    return edi.toString();
  }
}