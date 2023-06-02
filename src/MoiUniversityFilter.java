import java.io.File;  
import java.io.FileInputStream;  
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.hssf.usermodel.HSSFSheet;  
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;  
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class MoiUniversityFilter {
	
	public void getReport(int weekends,int holidays,String school) {
		HashMap<Employee,Integer[]> myHap = new HashMap<>();
		int monthDays = 30;
        DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        Date date;
		DataFormatter dataFormatter= new DataFormatter();
		try {
		    File file = new File("/home/lawrence/Music/MoiUniversity/Coast Campus April 2023.xls");
		    Workbook wb;

		    if (file.getName().endsWith(".xlsx")) {
		        // For .xlsx files
		        wb = new XSSFWorkbook(file);
		        
		    } else {
		        // For .xls files
		        wb = new HSSFWorkbook(new FileInputStream(file));
		    }

		    // Get the first sheet of the workbook
		    Sheet sheet = wb.getSheetAt(0);

		    //FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
			
			int firstRow = 0;//escape the first row
			for(Row row: sheet) {
				
				if(firstRow != 0) {
					String name = "";
					String pfNo = "";
					//Boolean present = true;
					//int lastColumn = Math.max(row.getLastCellNum(), 0);
						
						Cell cell1 = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						String value2 = dataFormatter.formatCellValue(cell1);
						if(!value2.isEmpty()) {//s/he was absent
							Cell cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
							String value = dataFormatter.formatCellValue(cell);
							
					        try {
					            date = dateFormat.parse(value);
					            Calendar calendar = Calendar.getInstance();
					            calendar.setTime(date);
					            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
					            boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
					            
					            if(!isWeekend) {
					            	
									Cell cell5 = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					            	pfNo = dataFormatter.formatCellValue(cell5);
					            	cell5 = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					            	name = dataFormatter.formatCellValue(cell5);
					            	
									Employee emp = new Employee(name,pfNo);
					                if (myHap.containsKey(emp)) {
					                	Integer[] empValues = myHap.get(emp);
					                	empValues[0] +=1; 
					                    myHap.put(emp, empValues);
					                } else {
					                    Integer[] empValues = new Integer[2];
					                    empValues[0] = 1; // Set the initial value at index 0 (e.g., starting weekends count)
					                    empValues[1] = 0; // Set the weekends count to 1
					                    myHap.put(emp, empValues);
					                }
					            }
					        } catch (ParseException e) {
					        	wb.close();
					            return;
					        }
						} else {//if reported but check only the weekends
							Cell cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
							String value = dataFormatter.formatCellValue(cell);
							
					        try {
					            date = dateFormat.parse(value);
					            Calendar calendar = Calendar.getInstance();
					            calendar.setTime(date);
					            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
					            boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
					            
					            if(isWeekend) {//was not absent and it must be a weekend
									Cell cell5 = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					            	pfNo = dataFormatter.formatCellValue(cell5);
					            	cell5 = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					            	name = dataFormatter.formatCellValue(cell5);
					            	
									Employee emp = new Employee(name,pfNo);
					                if (myHap.containsKey(emp)) {
					                	Integer[] empValues = myHap.get(emp);
					                	empValues[1] +=1; 
					                    myHap.put(emp, empValues);
					                } else {
					                    Integer[] empValues = new Integer[2];
					                    empValues[0] = 0;
					                    empValues[1] = 1; 
					                    myHap.put(emp, empValues);
					                }
							        
								}
					        } catch (ParseException e) {
					        	wb.close();
					            return;
					        }
						}
				
			}
				firstRow = 1;
		}
			
			//Sort the hashmap according to absentees that is ascending order
			myHap = sortByValue(myHap,monthDays,weekends);
			//Done processing
			// Create a PDF document
			// Create a PDF document


			PDDocument document = new PDDocument();
			PDPage page = new PDPage();
			document.addPage(page);

			// Set font and font size
			PDPageContentStream contentStream = new PDPageContentStream(document, page);
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

			// Define table parameters
			float margin = 50;
			float yStart = page.getMediaBox().getHeight() - margin - 100;
			float yPosition = yStart;
			float tableHeight = 20;
			
			File logoFile = new File("resources/moi.jpeg");
			PDImageXObject logoImage = PDImageXObject.createFromFileByExtension(logoFile, document);
			float logoX = 250; // Set the X-coordinate of the logo
			float logoY = yStart; // Set the Y-coordinate of the logo
			float logoWidth = 100; // Set the width of the logo
			float logoHeight = 120; // Set the height of the logo
			
			contentStream.drawImage(logoImage, logoX, logoY, logoWidth, logoHeight);
			PDFont boldFont = PDType1Font.TIMES_BOLD;
			float fontSize = 12;
			
			contentStream.beginText();
			contentStream.setFont(boldFont, fontSize);
			float textX = logoX + logoWidth + 10; // Set the X-coordinate of the text
			float textY = logoY + logoHeight / 2 - fontSize / 2; // Set the Y-coordinate of the text
			contentStream.newLineAtOffset(textX-110, textY - 60);
			contentStream.showText("MOI UNIVERSITY");
			contentStream.setFont(boldFont, 11);
			float secondTextX = textX - 55; // Set the X-coordinate of the second text same as the first text
			float secondTextY = textY - 10; // Set the Y-coordinate of the second text below the first text
			contentStream.moveTextPositionByAmount(secondTextX - textX, secondTextY - textY);
			contentStream.showText("OFFICE OF THE DEPUTY VICE-CHANCELLOR");
			
			float thirdTextX = textX - 60; // Set the X-coordinate of the third text same as the second text
			float thirdTextY = secondTextY - 10; // Set the Y-coordinate of the third text below the second text
			contentStream.moveTextPositionByAmount(thirdTextX - secondTextX, thirdTextY - secondTextY);
			contentStream.showText("ADMINISTRATION, PLANNING AND STRATEGY");
			contentStream.endText();
			
			contentStream.setFont(boldFont, fontSize);
			yStart -= 45;
			// Draw table header
			contentStream.setLineWidth(1.0f);
			contentStream.beginText();
			contentStream.newLineAtOffset(margin, yStart);
			contentStream.showText("PF-No.");
			contentStream.newLineAtOffset(50, 0);
			contentStream.showText("Name");
			contentStream.newLineAtOffset(130, 0);
			contentStream.showText("Present");
			contentStream.newLineAtOffset(80, 0);
			contentStream.showText("Weekends");
			contentStream.newLineAtOffset(70, 0);
			contentStream.showText("Absent");
			contentStream.newLineAtOffset(50, 0);
			contentStream.showText("Total Num");
			contentStream.newLineAtOffset(70, 0);
			contentStream.showText("Status");
			contentStream.endText();

			// Print the data to the PDF
			yStart -= 45;

			//PDPageContentStream contentStream = new PDPageContentStream(document, page);  // Use the contentStream for the current page
			List<PDPage> pages = new ArrayList<>();
			PDPageContentStream newContentStream = null;
			int counter = 1;
			for (Map.Entry<Employee, Integer[]> entry : myHap.entrySet()) {
			    if (counter % 29 == 0) {
			        // Create a new page and add it to the document
			        PDPage newPage = new PDPage();
			        pages.add(newPage);
			        document.addPage(newPage);

			        // Start a new content stream on the new page
			        if (newContentStream != null) {
			            newContentStream.close();
			        }
			        newContentStream = new PDPageContentStream(document, newPage);
			        yPosition = newPage.getMediaBox().getHeight() - margin - 55; 
			    }
			    
			    if (newContentStream != null) {
			        newContentStream.beginText();
			        newContentStream.setFont(PDType1Font.TIMES_ROMAN, 10);
			        newContentStream.newLineAtOffset(margin, yPosition);
			        newContentStream.showText(entry.getKey().getPfNo());
			        newContentStream.newLineAtOffset(50, 0);
			        newContentStream.showText(entry.getKey().getName());
			        newContentStream.newLineAtOffset(155, 0);
			        newContentStream.showText("" + (monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])));
			        newContentStream.newLineAtOffset(80, 0);
			        newContentStream.showText("" + (weekends + holidays));
			        newContentStream.newLineAtOffset(70, 0);
			        newContentStream.showText("" + ((monthDays -(weekends+holidays)) - (monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) ));
			        newContentStream.newLineAtOffset(50, 0);
			        newContentStream.showText("" + monthDays);
			        newContentStream.newLineAtOffset(45, 0);
			        int values = (Integer) entry.getValue()[0];
			        if ((monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) >= (monthDays - (weekends + holidays))) {
			        	newContentStream.showText("Present in required days");
			        } else {
			        	newContentStream.showText("Absent in "+((monthDays -(weekends+holidays)) - (monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) )+" days");
			        }
			        newContentStream.endText();

			        yPosition -= tableHeight;
			    } else {
			        // Continue writing to the current page's content stream
			        contentStream.beginText();
			        contentStream.setFont(PDType1Font.HELVETICA, 10);
			        contentStream.newLineAtOffset(margin, yPosition - 55);
			        contentStream.showText(entry.getKey().getPfNo());
			        contentStream.newLineAtOffset(50, 0);
			        contentStream.showText(entry.getKey().getName());
			        contentStream.newLineAtOffset(155, 0);
			        contentStream.showText((monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) + "");
			        contentStream.newLineAtOffset(80, 0);
			        contentStream.showText("" + (weekends + holidays));
			        contentStream.newLineAtOffset(70, 0);
			        contentStream.showText("" + ((monthDays -(weekends+holidays)) - (monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) ));//entry.getValue().toString()
			        contentStream.newLineAtOffset(50, 0);
			        contentStream.showText("" + monthDays);
			        contentStream.newLineAtOffset(45, 0);
			        
			        int values = (Integer) entry.getValue()[0];
			        if ((monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) >= (monthDays - (weekends + holidays))) {
			            contentStream.showText("Present in required days");
			        } else {
			            contentStream.showText("Absent in "+((monthDays -(weekends+holidays)) - (monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1])) )+" days");
			        }
			        contentStream.endText();

			        yPosition -= tableHeight;
			    }
			    
			    counter++;
			}
			
			// Add the signature on the last page
			if (!pages.isEmpty()) {
			    PDPage lastPage = pages.get(pages.size() - 1);
			    PDPageContentStream lastPageContentStream = new PDPageContentStream(document, lastPage, PDPageContentStream.AppendMode.APPEND, true, true);
			    lastPageContentStream.beginText();
			    lastPageContentStream.setFont(PDType1Font.TIMES_BOLD, 12);
			    lastPageContentStream.newLineAtOffset(margin, yPosition - 55);
			    lastPageContentStream.showText("PROF. ENG. KIRIMI H. KIRIAMITI");
			    lastPageContentStream.newLineAtOffset(0, -10);
			    lastPageContentStream.showText("DEPUTY  VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY");
			    //lastPageContentStream.newLineAtOffset(300, -5);
			    lastPageContentStream.newLineAtOffset(0, -40);
			    lastPageContentStream.showText("Staff attendance register for : "+school+" Campus,  Generated on " + dateFormat.format(new Date()));
			    lastPageContentStream.endText();

				float lineY = yPosition - 67; // Adjust the Y-coordinate of the line beneath the second text
				
				float lineWidth = PDType1Font.TIMES_ROMAN.getStringWidth("DEPUTY VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY") / 1000 * 10;
				
				lastPageContentStream.setLineWidth(0.5f); // Set the line width
				lastPageContentStream.moveTo(margin, lineY);
				lastPageContentStream.lineTo(margin + lineWidth + 95, lineY);
				lastPageContentStream.stroke();
				
				
			    lastPageContentStream.close();
			}else {
			    // Handle the case when there are no pages or only one page
			    /*PDPage newPage = new PDPage();
			    document.addPage(newPage);
			    
			    PDPageContentStream newPageContentStream = new PDPageContentStream(document, newPage);*/
			    contentStream.beginText();
			    contentStream.setFont(PDType1Font.TIMES_BOLD, 12);
			    contentStream.newLineAtOffset(margin, yPosition - 75);
			    contentStream.showText("PROF. ENG. KIRIMI H. KIRIAMITI");
			    contentStream.newLineAtOffset(0, -10);
			    contentStream.showText("DEPUTY VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY");
			    contentStream.newLineAtOffset(0, -40);
			    contentStream.showText("Staff attendance register for: " + school + " Campus, Generated on " + dateFormat.format(new Date()));
			    contentStream.endText();
			    
			    float lineY = yPosition - 90; // Adjust the Y-coordinate of the line beneath the second text
			    float lineWidth = PDType1Font.TIMES_ROMAN.getStringWidth("DEPUTY VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY") / 1000 * 10;
			    
			    contentStream.setLineWidth(0.5f); // Set the line width
			    contentStream.moveTo(margin, lineY);
			    contentStream.lineTo(margin + lineWidth + 95, lineY);
			    contentStream.stroke();
			    
			    contentStream.close();
			}

			// Close the content streams
			if (contentStream != null) {
			    contentStream.close();
			}
			if (newContentStream != null) {
			    newContentStream.close();
			}

			// Save and close the document
			File outputFile = new File("/home/lawrence/Desktop/coast.pdf");
			document.save(outputFile);
			document.close();

		} catch (IOException e) {
			
		    e.printStackTrace();
		} catch (InvalidFormatException e) {
		    e.printStackTrace();
		}
		
	}
	public void getReportClock(String school) {
		HashMap<Employee,Integer> myAbsentees = new HashMap<>();
		int monthDays = 30;
        DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        Date date;
		DataFormatter dataFormatter= new DataFormatter();
		try {
		    File file = new File("/home/lawrence/Music/MoiUniversity/Nairobi Campus April 2023 (1)(1).xls");
		    Workbook wb;

		    if (file.getName().endsWith(".xlsx")) {
		        // For .xlsx files
		        wb = new XSSFWorkbook(file);
		        
		    } else {
		        // For .xls files
		        wb = new HSSFWorkbook(new FileInputStream(file));
		    }

		    // Get the first sheet of the workbook
		    Sheet sheet = wb.getSheetAt(0);

		    //FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
			
			int firstRow = 0;//escape the first row
			for(Row row: sheet) {
				
				if(firstRow != 0) {
					String name = "";
					String pfNo = "";
					//Boolean present = true;
					//int lastColumn = Math.max(row.getLastCellNum(), 0);
						Cell cellIn = row.getCell(6, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						String cellInValue = dataFormatter.formatCellValue(cellIn);

						//start
						if(!cellInValue.isEmpty()) { //if clocked in
							String [] values = cellInValue.split(":");
							
							if(Integer.parseInt(values[0]) < 8) { //if stayed less than 9 hours
								
								Cell cell5 = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
				            	pfNo = dataFormatter.formatCellValue(cell5);
				            	cell5 = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
				            	name = dataFormatter.formatCellValue(cell5);
				            	Employee emp = new Employee(name,pfNo);
				            	
				                if (myAbsentees.containsKey(emp)) {
				                	//System.out.print(emp.getName() + "\t\t" + emp.getPfNo() + "\t\t " + myAbsentees.get(emp)+"\n");
				                	myAbsentees.put(emp, myAbsentees.get(emp) + 1);
				                } else {
				                	myAbsentees.put(emp, 1);
				                }
								
							}
							
						}
				
			}
				firstRow = 1;
		}
			
			//Sort the hashmap according to absentees that is ascending order
			myAbsentees = sortByValueAbsentees(myAbsentees);
			//Done processing
			// Create a PDF document
			// Create a PDF document


			PDDocument document = new PDDocument();
			PDPage page = new PDPage();
			document.addPage(page);

			// Set font and font size
			PDPageContentStream contentStream = new PDPageContentStream(document, page);
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

			// Define table parameters
			float margin = 50;
			float yStart = page.getMediaBox().getHeight() - margin - 100;
			float yPosition = yStart;
			float tableHeight = 20;
			
			File logoFile = new File("resources/moi.jpeg");
			PDImageXObject logoImage = PDImageXObject.createFromFileByExtension(logoFile, document);
			float logoX = 250; // Set the X-coordinate of the logo
			float logoY = yStart; // Set the Y-coordinate of the logo
			float logoWidth = 100; // Set the width of the logo
			float logoHeight = 120; // Set the height of the logo
			
			contentStream.drawImage(logoImage, logoX, logoY, logoWidth, logoHeight);
			PDFont boldFont = PDType1Font.TIMES_BOLD;
			float fontSize = 12;
			
			contentStream.beginText();
			contentStream.setFont(boldFont, fontSize);
			float textX = logoX + logoWidth + 10; // Set the X-coordinate of the text
			float textY = logoY + logoHeight / 2 - fontSize / 2; // Set the Y-coordinate of the text
			contentStream.newLineAtOffset(textX-110, textY - 60);
			contentStream.showText("MOI UNIVERSITY");
			contentStream.setFont(boldFont, 11);
			float secondTextX = textX - 55; // Set the X-coordinate of the second text same as the first text
			float secondTextY = textY - 10; // Set the Y-coordinate of the second text below the first text
			contentStream.moveTextPositionByAmount(secondTextX - textX, secondTextY - textY);
			contentStream.showText("OFFICE OF THE DEPUTY VICE-CHANCELLOR");
			
			float thirdTextX = textX - 60; // Set the X-coordinate of the third text same as the second text
			float thirdTextY = secondTextY - 10; // Set the Y-coordinate of the third text below the second text
			contentStream.moveTextPositionByAmount(thirdTextX - secondTextX, thirdTextY - secondTextY);
			contentStream.showText("ADMINISTRATION, PLANNING AND STRATEGY");
			contentStream.endText();
			
			contentStream.setFont(boldFont, fontSize);
			yStart -= 45;
			// Draw table header
			contentStream.setLineWidth(1.0f);
			contentStream.beginText();
			contentStream.newLineAtOffset(margin, yStart);
			contentStream.showText("PF-No.");
			contentStream.newLineAtOffset(50, 0);
			contentStream.showText("Name");
			contentStream.newLineAtOffset(130, 0);
			contentStream.showText("No. Of Days (Less than 8 hours)");
			contentStream.endText();

			// Print the data to the PDF
			yStart -= 45;

			//PDPageContentStream contentStream = new PDPageContentStream(document, page);  // Use the contentStream for the current page
			List<PDPage> pages = new ArrayList<>();
			PDPageContentStream newContentStream = null;
			int counter = 1;
			for (Map.Entry<Employee, Integer> entry : myAbsentees.entrySet()) {
			    if (counter % 29 == 0) {
			        // Create a new page and add it to the document
			        PDPage newPage = new PDPage();
			        pages.add(newPage);
			        document.addPage(newPage);

			        // Start a new content stream on the new page
			        if (newContentStream != null) {
			            newContentStream.close();
			        }
			        newContentStream = new PDPageContentStream(document, newPage);
			        yPosition = newPage.getMediaBox().getHeight() - margin - 55; 
			    }
			    
			    if (newContentStream != null) {
			        newContentStream.beginText();
			        newContentStream.setFont(PDType1Font.TIMES_ROMAN, 10);
			        newContentStream.newLineAtOffset(margin, yPosition);
			        newContentStream.showText(entry.getKey().getPfNo());
			        newContentStream.newLineAtOffset(50, 0);
			        newContentStream.showText(entry.getKey().getName());
			        newContentStream.newLineAtOffset(170, 0);
			        newContentStream.showText(entry.getValue()+"");
			        newContentStream.endText();

			        yPosition -= tableHeight;
			    } else {
			        // Continue writing to the current page's content stream
			        contentStream.beginText();
			        contentStream.setFont(PDType1Font.HELVETICA, 10);
			        contentStream.newLineAtOffset(margin, yPosition - 55);
			        contentStream.showText(entry.getKey().getPfNo());
			        contentStream.newLineAtOffset(50, 0);
			        contentStream.showText(entry.getKey().getName());
			        contentStream.newLineAtOffset(170, 0);
			        contentStream.showText(entry.getValue()+"");
			        contentStream.endText();
			        yPosition -= tableHeight;
			    }
			    
			    counter++;
			}
			
			// Add the signature on the last page
			if (!pages.isEmpty()) {
			    PDPage lastPage = pages.get(pages.size() - 1);
			    PDPageContentStream lastPageContentStream = new PDPageContentStream(document, lastPage, PDPageContentStream.AppendMode.APPEND, true, true);
			    lastPageContentStream.beginText();
			    lastPageContentStream.setFont(PDType1Font.TIMES_BOLD, 12);
			    lastPageContentStream.newLineAtOffset(margin, yPosition - 55);
			    lastPageContentStream.showText("PROF. ENG. KIRIMI H. KIRIAMITI");
			    lastPageContentStream.newLineAtOffset(0, -10);
			    lastPageContentStream.showText("DEPUTY  VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY");
			    //lastPageContentStream.newLineAtOffset(300, -5);
			    lastPageContentStream.newLineAtOffset(0, -40);
			    lastPageContentStream.showText("Staff attendance register for : "+school+" Campus,  Generated on " + dateFormat.format(new Date())+ " for April");
			    lastPageContentStream.endText();

				float lineY = yPosition - 67; // Adjust the Y-coordinate of the line beneath the second text
				
				float lineWidth = PDType1Font.TIMES_ROMAN.getStringWidth("DEPUTY VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY") / 1000 * 10;
				
				lastPageContentStream.setLineWidth(0.5f); // Set the line width
				lastPageContentStream.moveTo(margin, lineY);
				lastPageContentStream.lineTo(margin + lineWidth + 95, lineY);
				lastPageContentStream.stroke();
				
				
			    lastPageContentStream.close();
			}else {
			    // Handle the case when there are no pages or only one page
			    /*PDPage newPage = new PDPage();
			    document.addPage(newPage);
			    
			    PDPageContentStream newPageContentStream = new PDPageContentStream(document, newPage);*/
			    contentStream.beginText();
			    contentStream.setFont(PDType1Font.TIMES_BOLD, 12);
			    contentStream.newLineAtOffset(margin, yPosition - 75);
			    contentStream.showText("PROF. ENG. KIRIMI H. KIRIAMITI");
			    contentStream.newLineAtOffset(0, -10);
			    contentStream.showText("DEPUTY VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY");
			    contentStream.newLineAtOffset(0, -40);
			    contentStream.showText("Staff attendance register for: " + school + " Campus, Generated on " + dateFormat.format(new Date()) + " for April");
			    contentStream.endText();
			    
			    float lineY = yPosition - 90; // Adjust the Y-coordinate of the line beneath the second text
			    float lineWidth = PDType1Font.TIMES_ROMAN.getStringWidth("DEPUTY VICE-CHANCELLOR, ADMINISTRATION, PLANNING & STRATEGY") / 1000 * 10;
			    
			    contentStream.setLineWidth(0.5f); // Set the line width
			    contentStream.moveTo(margin, lineY);
			    contentStream.lineTo(margin + lineWidth + 95, lineY);
			    contentStream.stroke();
			    
			    contentStream.close();
			}

			// Close the content streams
			if (contentStream != null) {
			    contentStream.close();
			}
			if (newContentStream != null) {
			    newContentStream.close();
			}

			// Save and close the document
			File outputFile = new File("/home/lawrence/Desktop/nairobi.pdf");
			document.save(outputFile);
			document.close();

		} catch (IOException e) {
			
		    e.printStackTrace();
		} catch (InvalidFormatException e) {
		    e.printStackTrace();
		}
		
	}
	
    public static HashMap<Employee, Integer[]> sortByValue(HashMap<Employee, Integer[]> hm,final int monthDays, final int weekends)
    {
        List<Map.Entry<Employee, Integer[]>> entryList = new ArrayList<>(hm.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Employee, Integer[]>>() {
            public int compare(Map.Entry<Employee, Integer[]> entry1, Map.Entry<Employee, Integer[]> entry2) {
            	int value1 = monthDays - (entry1.getValue()[0] + weekends) + entry1.getValue()[1];
                int value2 = monthDays - (entry2.getValue()[0] + weekends) + entry2.getValue()[1];
                return Integer.compare(value2, value1);
                //(monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1]))
            }
        });

        // Create a new LinkedHashMap to store the sorted entries
        LinkedHashMap<Employee, Integer[]> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<Employee, Integer[]> entry : entryList) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedHashMap;
    }
    
    public static HashMap<Employee, Integer> sortByValueAbsentees(HashMap<Employee, Integer> hm) {
    	
        List<Map.Entry<Employee, Integer>> entryList = new ArrayList<>(hm.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Employee, Integer>>() {
            public int compare(Map.Entry<Employee, Integer> entry1, Map.Entry<Employee, Integer> entry2) {
                //return Integer.compare(value2, value1);
                return entry1.getValue().compareTo(entry2.getValue());
                //(monthDays - (entry.getValue()[0] + weekends) + (entry.getValue()[1]))
            }
        });

        // Create a new LinkedHashMap to store the sorted entries
        LinkedHashMap<Employee, Integer> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<Employee, Integer> entry : entryList) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedHashMap;
    }

	public static void main(String args[]) throws IOException {
		MoiUniversityFilter mu = new MoiUniversityFilter();
		Scanner sc = new Scanner(System.in);
        try {
            System.out.println("How many weekends were there:");
            int weekends = sc.nextInt();

            System.out.println("How many holidays were there:");
            int holidays = sc.nextInt();
            
            sc.nextLine();
            System.out.println("Which campus:");
            String campus = sc.nextLine();

            // Use the input values as needed
            //mu.getReport(weekends,holidays,campus);
            mu.getReportClock(campus);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter integer values.");
        }
		
	}
}
	

class Employee {
	private String pfNo;
	private String name;
	Employee(String name, String pf) {
		this.pfNo = pf;
		this.name = name;
	}
	public String getPfNo() {
		return pfNo;
	}
	public String getName() {
		return name;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Employee employee = (Employee) obj;
        return pfNo.equals(employee.pfNo) && name.equals(employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pfNo, name);
    }
	
}
