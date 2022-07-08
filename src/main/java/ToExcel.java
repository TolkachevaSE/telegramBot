import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ToExcel {
    static Logger LOGGER = LoggerFactory.getLogger(ToExcel.class);
    File saveFile = new File("temp.xlsx");

    public boolean createFile() throws IOException, SQLException, ClassNotFoundException, URISyntaxException {

        if (saveFile.exists()) {
            if (saveFile.delete()) {
                fileFill();
                return true;
            } else {
                return false;
            }
        } else {
            fileFill();
            return true;
        }
    }

    private void fileFill() throws SQLException, IOException, ClassNotFoundException, URISyntaxException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("My Finance");
        Row header = sheet.createRow(0);

        //------------------- create  header style

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        headerStyle.setFont(font);

        //------------------- create  header

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("income");
        headerCell.setCellStyle(headerStyle);


        headerCell = header.createCell(1);
        headerCell.setCellValue("Sum");
        headerCell.setCellStyle(headerStyle);
        int j = 2;
        for (Category cat : Category.values()) {
            headerCell = header.createCell(j);
            headerCell.setCellValue(cat.name());
            headerCell.setCellStyle(headerStyle);
            j++;
        }

        headerCell = header.createCell(j);
        headerCell.setCellValue("Comments");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(j + 1);
        headerCell.setCellValue("Date&Time");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(j + 2);
        headerCell.setCellValue("Person");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(j + 3);
        headerCell.setCellValue("Id");
        headerCell.setCellStyle(headerStyle);

        //-------------------------------- (j+4) = number of columns
        //-------------------------------fill the table

        SQLTable table = new SQLTable();
        String sql = "select * from finance";

        ResultSet rs;
        PreparedStatement prStatement = table.getConnectionBD().prepareStatement(sql);
        rs = prStatement.executeQuery();
        int m = 2;
        while (rs.next()) {
            Row row = sheet.createRow(m);

            for (int t = 1; t < j + 5; t++) {

                if (t == j + 3 || t == j + 1 || t == j + 2) {
                    Cell cell = row.createCell(t - 1);
                    cell.setCellValue(rs.getString(t));
                    LOGGER.debug("j= "+j+" t="+t+" cell= "+rs.getString(t));
                } else {
                    Cell cell = row.createCell(t - 1);
                    cell.setCellValue(rs.getDouble(t));
                }
            }
            m++;
        }
        for (int k = 0; k < j + 3; k++) {
            sheet.autoSizeColumn(k);
        }
        FileOutputStream outputStream = new FileOutputStream(saveFile.getAbsoluteFile());
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}



